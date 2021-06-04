package ctrmap.stdlib.fs;

import ctrmap.stdlib.fs.accessors.arc.DotArc;
import ctrmap.stdlib.fs.accessors.arc.ArcInput;
import ctrmap.stdlib.fs.accessors.arc.ArcFile;
import ctrmap.stdlib.fs.accessors.arc.ArcFileAccessor;
import ctrmap.stdlib.util.ProgressMonitor;
import java.util.ArrayList;
import java.util.List;

public class VFS {

	private VFSRootFile root;
	private VFSRootFile overlay;

	private boolean hasChangeBlacklist = false;
	private VFSChangeBlacklist blacklist;

	public void initVFS(VFSRootFile root, VFSRootFile location) {
		this.root = root;
		overlay = location;

		final VFS mInstance = this;

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (hasChangeBlacklist) {
					blacklist.doRemoveFiles(mInstance);
					blacklist.terminate();
				}
			}
		});
	}

	public void createChangeBlacklist(FSFile location) {
		blacklist = new VFSChangeBlacklist(location);
		hasChangeBlacklist = true;
	}

	public FSFile getBaseFSRoot() {
		return root;
	}

	public FSFile getOvFSRoot() {
		return overlay;
	}

	public void applyOvFS(String path, FSManager fs) {
		applyOvFS(path, fs, null);
	}

	public void applyOvFS(String path, FSManager fs, ProgressMonitor monitor) {
		ArcFileAccessor afa = fs.getArcFileAccessor();
		path = FSWildCard.getWildCardedPath(getRelativePath(path));
		FSFile ovFile = getFileFromRefPath(overlay, path, afa);
		FSFile target = getFileFromRefPath(root, path, afa);

		if (target.exists()) {
			if (ovFile.isDirectory()) {
				if (monitor != null) {
					monitor.setProgressTitle("Packing directory " + path);
					monitor.setProgressPercentage(0);
				}
				if (target instanceof ArcFile) {
					System.out.println("Applying arcfile..." + target.getPath());
					ArcFile arc = (ArcFile) target;
					if (monitor != null) {
						monitor.setProgressPercentage(0);
						monitor.setProgressSubTitle("Patching ArcFile...");
					}
					applyToArcFile(ovFile, ovFile, arc, afa, monitor);
				} else {
					for (FSFile sub : ovFile.listFiles()) {
						applyOvFS(sub.getPath(), fs, monitor);
					}
				}
			} else {
				if (target.isFile()) {
					if (monitor != null) {
						monitor.setProgressSubTitle("Writing " + target.getName());
						List<String> siblings = target.getParent().list();
						monitor.setProgressPercentage((int) (siblings.indexOf(target.getPath()) / (float) siblings.size() * 100));
					}
					if (!isFileChangeBlacklisted(path)) {
						System.out.println("Write " + path + " to " + target.getPath());
						FSUtil.writeBytesToFile(target, FSUtil.readFileToBytes(ovFile));
					}
				}
			}
		}
		else {
			if (ovFile instanceof ArcFile){
				ovFile = ((ArcFile)ovFile).getSource(); //If the ArcFile was kept, it would get extracted to the target because ArcFile is a directory
			}
			
			FSUtil.copy(ovFile, target);
		}
	}

	private void applyToArcFile(FSFile root, FSFile fsf, ArcFile arc, ArcFileAccessor afa, ProgressMonitor monitor) {
		List<ArcInput> inputs = getArcInputs(root, fsf);
		ensureDotArcExistence(inputs, root);
		afa.writeToArcFile(arc, monitor, inputs.toArray(new ArcInput[inputs.size()]));
	}

	private void ensureDotArcExistence(List<ArcInput> inputs, FSFile repackRoot) {
		for (ArcInput in : inputs) {
			if (in.targetPath.equals(DotArc.DOT_ARC_SIGNATURE)) {
				return;
			}
		}
		ArcInput dotArc = new ArcInput(DotArc.DOT_ARC_SIGNATURE, repackRoot.getChild(DotArc.DOT_ARC_SIGNATURE));
		inputs.add(dotArc);
	}

	private List<ArcInput> getArcInputs(FSFile root, FSFile fsf) {
		List<ArcInput> inputs = new ArrayList<>();
		if (fsf.isDirectory()) {
			for (FSFile sub : fsf.listFiles()) {
				inputs.addAll(getArcInputs(root, sub));
			}
		} else {
			ArcInput thisInput = new ArcInput(fsf.getPathRelativeTo(root), fsf);
			String fsPath = FSWildCard.getWildCardedPath(getRelativePath(fsf.getPath()));
			if (!isFileChangeBlacklisted(fsPath)) {
				System.out.println("Include ArcInput " + fsPath);
				inputs.add(thisInput);
			}
		}
		return inputs;
	}

	public void notifyFileChange(String changedPath) {
		if (hasChangeBlacklist) {
			blacklist.removePathFromBlacklist(changedPath);
		}
	}

	public void notifyOvFsNewFileInit(String path) {
		if (hasChangeBlacklist) {
			blacklist.putBlacklistPath(path);
		}
	}

	public void relocateBlackListFile(String oldPath, String newPath) {
		if (hasChangeBlacklist) {
			blacklist.relocatePaths(oldPath, newPath);
		}
	}

	public boolean isFileChangeBlacklisted(String path) {
		if (hasChangeBlacklist) {
			return blacklist.hasPath(path);
		}
		return false;
	}

	public FSFile getFile(String path, FSManager fs) {
		//System.out.println("Requested file " + path);
		ArcFileAccessor afa = fs.getArcFileAccessor();
		path = getRelativePath(path);
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		boolean isExistingBase = false;
		FSFile existing = getFileFromRefPath(overlay, path, afa);
		if (existing != null && !existing.exists()) {
			isExistingBase = true;
			existing = getFileFromRefPath(root, path, afa);
		} else {
			/*FSFile arcTest = getFileFromRefPath(root, path, afa);
			if (arcTest.exists() && afa.isArcFile(arcTest) && !(existing instanceof ArcFile)) {
				existing = new ArcFile(arcTest, afa);
			}*/
		}
		FSFile result;
		if (existing != null && existing.exists()) {
			if (isExistingBase) {
				result = new VFSFile(path, this, existing, afa);
			} else {
				result = new VFSFile(path, this, afa);
			}
		} else {
			//If it does not exist, we will create empty files on the overlay, also expand ArcFiles with an accessor
			String[] splitPath = path.split("/");
			result = overlay;
			for (int t = 0; t < splitPath.length; t++) {
				String token = splitPath[t];
				if (token.startsWith(":") && token.endsWith(":")) {
					result = getExistingRefFile(result, token);
				} else {
					result = result.getChild(token);
				}
				//If expandArcs is allowed, this takes into account ArcFiles in origin and casts them accordingly
				//The ArcFileAccessor will then deliver the extracted ArcFileMember with its implementation
				FSFile origin = root.getMatchingChild(result.getPathRelativeTo(overlay));
				if (origin != null && afa.isArcFile(origin)) {
					//System.out.println("Expanding ArcFile " + origin.getPath());
					ArcFile af = new ArcFile(origin, afa);
					result = af;

					StringBuilder pathInArc = new StringBuilder();
					for (int t2 = t + 1; t2 < splitPath.length; t2++) {
						pathInArc.append("/");
						pathInArc.append(splitPath[t2]);
					}
					if (pathInArc.length() > 0) {
						result = new VFSFile(path, this, af.getChild(pathInArc.toString()), afa);
						break;
					}
				}
			}
		}
		//System.out.println("Got file " + result + " for request " + path);
		return result;
	}

	public String getRelativePath(String path) {
		path = FSFile.getPathRelativeTo(path, overlay.getPath());
		path = FSFile.getPathRelativeTo(path, root.getPath());
		return path;
	}

	public static FSFile getFileFromRefPath(FSFile parent, String refPath, ArcFileAccessor afa) {
		FSFile currentParent = parent;
		for (int i = 0; i < refPath.length(); i++) {
			String thing = getTextUntilSlash(refPath, i);
			i += thing.length();
			if (thing.startsWith(":") && thing.endsWith(":")) {
				currentParent = getExistingRefFile(currentParent, thing);
			} else {
				currentParent = currentParent.getChild(thing);
			}
			if (afa != null && afa.isArcFile(currentParent)) {
				currentParent = new ArcFile(currentParent, afa);
				if (i + 1 < refPath.length()) {
					return currentParent.getChild(refPath.substring(i + 1));
				}
			}
		}
		return currentParent;
	}

	public static FSFile getExistingRefFile(FSFile parent, String ref) {
		String pattern = ref.replace(":", "");
		for (FSWildCard wc : FSWildCard.values()) {
			if (wc.ddotId.equals(pattern)) {
				return getFirstExistingFile(parent, wc.getFirstOption(), wc.getOtherOptions());
			}
		}
		return null;
	}

	private static FSFile getFirstExistingFile(FSFile parent, String defaultOption, String... otherOptions) {
		FSFile f = parent.getChild(defaultOption);
		for (String opt : otherOptions) {
			if (!f.exists()) {
				FSFile f2 = parent.getChild(opt);
				if (f2.exists()) {
					f = f2;
				}
			} else {
				break;
			}
		}
		return f;
	}

	private static String getTextUntilSlash(String str, int start) {
		int idx = str.indexOf('/', start);
		if (idx == -1) {
			idx = str.length();
		}
		return str.substring(start, idx);
	}
}
