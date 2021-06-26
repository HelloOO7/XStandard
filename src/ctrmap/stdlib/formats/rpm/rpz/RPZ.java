package ctrmap.stdlib.formats.rpm.rpz;

import ctrmap.stdlib.formats.rpm.RPM;
import ctrmap.stdlib.formats.zip.ZipArchive;
import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.accessors.FSFileAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RPZ extends FSFileAdapter {

	private static final String RPZ_INDEX_FILENAME = "_Index.yml";

	private static final String RPZ_META_DIRNAME = "meta";
	private static final String RPZ_CODE_DIRNAME = "code";
	private static final String RPZ_CONTENT_DIRNAME = "content";

	private final FSFile meta;
	private final FSFile code;
	private final FSFile content;

	private final RPZIndex index;

	public RPZ(FSFile file) {
		super(file = getFsFileMaybeZip(file));
		
		if (!isRPZ(file)){
			throw new IllegalArgumentException("File is not an RPZ!");
		}
		
		meta = file.getChild(RPZ_META_DIRNAME);
		code = file.getChild(RPZ_CODE_DIRNAME);
		content = file.getChild(RPZ_CONTENT_DIRNAME);

		index = new RPZIndex(meta.getChild(RPZ_INDEX_FILENAME));
	}
	
	public RPZ(FSFile file, String productId){
		super(file);
		
		meta = file.getChild(RPZ_META_DIRNAME);
		code = file.getChild(RPZ_CODE_DIRNAME);
		content = file.getChild(RPZ_CONTENT_DIRNAME);

		meta.mkdir();
		code.mkdir();
		content.mkdir();
		
		index = new RPZIndex(meta.getChild(RPZ_INDEX_FILENAME), productId);
	}
	
	public RPZIndex getIndex(){
		return index;
	}

	public String getProductId() {
		return index.productId;
	}

	public String getProductName() {
		return index.productId;
	}

	public String getDescription() {
		return index.description;
	}

	public RPZVersion getVersion() {
		return index.version;
	}

	public boolean install(IRPZHandler handler) {
		try {
			int presentVersion = handler.getInstalledProductVersion(getProductId());
			if (presentVersion != -1) {
				RPZVersion newVersion = getVersion();
				if (presentVersion == newVersion.number) {
					if (handler.throwError(IRPZHandler.RPZErrorCode.ALREADY_INSTALLED)) {
						return false;
					}
				} else if (presentVersion > newVersion.number) {
					if (handler.throwError(IRPZHandler.RPZErrorCode.HIGHER_VERSION_INSTALLED)) {
						return false;
					}
				}
			}

			if (index.modules.isEmpty()) {
				handler.throwError(IRPZHandler.RPZErrorCode.RPZ_MALFORMED, "RPZ index does not contain any modules!");
				return false;
			}

			String desiredTarget = handler.getTarget();
			RPZModuleInfo targetModuleInfo = null;

			Map<String, RPZModuleInfo> moduleInfos = new HashMap<>();

			for (RPZYmlBase.RPZYmlReference moduleRef : index.modules) {
				if (targetModuleInfo != null) {
					break;
				}

				FSFile moduleInfoFile = meta.getChild(moduleRef.ymlPath);
				if (!moduleInfoFile.exists()) {
					if (handler.throwError(IRPZHandler.RPZErrorCode.FILE_NOT_FOUND, "Referenced module info " + moduleRef.ymlPath + " not present in RPZ!")) {
						return false;
					}
				}
				RPZModuleInfo moduleInfo = new RPZModuleInfo(moduleInfoFile);
				moduleInfos.put(moduleInfo.name, moduleInfo);
				if (moduleInfo.isDependencyModule) {
					continue;
				}
				for (RPZModuleInfo.RPZTarget tgt : moduleInfo.targets) {
					if (Objects.equals(desiredTarget, tgt.targetName)) {
						targetModuleInfo = moduleInfo;
						break;
					}
				}
			}

			if (targetModuleInfo != null) {
				return installModule(targetModuleInfo, moduleInfos, handler);
			} else {
				handler.throwError(IRPZHandler.RPZErrorCode.TARGET_NOT_SUPPORTED);
				return false;
			}
		} catch (Exception ex) {
			handler.throwError(IRPZHandler.RPZErrorCode.UNKNOWN, ex.getMessage());
			ex.printStackTrace();
			return false;
		}
	}

	private boolean installModule(RPZModuleInfo module, Map<String, RPZModuleInfo> modules, IRPZHandler handler) {
		if (module.contentInfo != null) {
			FSFile contentInfoFile = meta.getChild(module.contentInfo.ymlPath);
			if (!contentInfoFile.exists()) {
				if (handler.throwError(IRPZHandler.RPZErrorCode.FILE_NOT_FOUND, "Content info " + module.contentInfo.ymlPath + " not found!")) {
					return false;
				}
			}
			RPZContentInfo contentInfo = new RPZContentInfo(contentInfoFile);

			FSFile contentDest = handler.getDestContentDirectory();

			for (RPZContentInfo.RPZContentReference ref : contentInfo.content) {
				if (ref.destinationPath == null){
					handler.throwError(IRPZHandler.RPZErrorCode.UNKNOWN, "Malicious content reference! Applying this would delete your whole FS root.");
					continue;
				}
				FSFile cntSrc = content.getChild(ref.sourcePath);
				FSFile cntTgt = contentDest.getChild(ref.destinationPath);

				if (cntTgt.exists() && !cntTgt.canWrite()) {
					if (handler.throwError(IRPZHandler.RPZErrorCode.FILE_NOT_FOUND, "Could not open file " + cntTgt + " for write access!")) {
						return false;
					}
					continue;
				}
				if (!cntSrc.exists()) {
					if (handler.throwError(IRPZHandler.RPZErrorCode.FILE_NOT_FOUND, "Could not open file " + cntTgt + " for write access!")) {
						return false;
					}
					continue;
				}
				if (!cntTgt.exists() && (cntSrc.isDirectory() != cntTgt.isDirectory())) {
					if (handler.throwError(IRPZHandler.RPZErrorCode.FILE_NOT_FOUND, "Source and target content files are not of the same file/directory class.")) {
						return false;
					}
					continue;
				}

				FSUtil.copy(cntSrc, cntTgt);
			}
		}

		for (RPZModuleInfo.RPZDependency dep : module.dependencies) {
			int presentDepVersion = handler.getInstalledProductVersion(dep.productId);

			if (presentDepVersion == -1) {
				if (dep.bundledDepModule != null) {
					RPZModuleInfo depModuleInfo = modules.get(dep.productId);
					if (depModuleInfo == null) {
						if (handler.throwError(IRPZHandler.RPZErrorCode.DEPENDENCY_NOT_RESOLVED, "Module info for dependency " + dep.productId + " not found.")) {
							return false;
						}
					} else {
						if (!depModuleInfo.isDependencyModule){
							if (handler.throwError(IRPZHandler.RPZErrorCode.DEPENDENCY_NOT_RESOLVED, "Module info for dependency is not flagged as a dependency. (" + dep.productId + ")")){
								return false;
							}
							continue;
						}
						if (!installModule(depModuleInfo, modules, handler)) {
							if (handler.throwError(IRPZHandler.RPZErrorCode.RPM_INSTALL_FAILED, "Could not install dependency " + dep.productId)) {
								return false;
							}
						}
					}
				}
			} else {
				boolean pass = true;
				if (dep.versionOperator != null) {
					switch (dep.versionOperator) {
						case EQUAL:
							pass = presentDepVersion == dep.version;
							break;
						case GEQUAL:
							pass = presentDepVersion >= dep.version;
							break;
						case LESS:
							pass = presentDepVersion < dep.version;
							break;
					}
				}

				if (!pass) {
					if (handler.throwError(IRPZHandler.RPZErrorCode.FILE_NOT_FOUND, "Source and target content files are not of the same file/directory class.")) {
						return false;
					}
				}
			}
		}

		FSFile execRpmFile = code.getChild(module.rpmName);
		if (!execRpmFile.exists() || !RPM.isRPM(execRpmFile)) {
			handler.throwError(IRPZHandler.RPZErrorCode.FILE_NOT_FOUND, "Module RPM " + execRpmFile + " not found or it is not an RPM.");
			return false;
		}

		RPM execRpm = new RPM(execRpmFile);
		execRpm.productId = getProductId();
		execRpm.productVersion = getVersion().number;
		if (!handler.installRPM(execRpm)) {
			handler.throwError(IRPZHandler.RPZErrorCode.RPM_INSTALL_FAILED, "Could not install module RPM (" + execRpmFile + ").");
			return false;
		}
		return true;
	}

	public static boolean isRPZ(FSFile fsf) {
		fsf = getFsFileMaybeZip(fsf);
		FSFile meta = fsf.getChild(RPZ_META_DIRNAME);
		if (meta.exists()) {
			return meta.getChild(RPZ_INDEX_FILENAME).exists() && fsf.getChild(RPZ_CODE_DIRNAME).exists() && fsf.getChild(RPZ_CONTENT_DIRNAME).exists();
		}
		return false;
	}

	private static FSFile getFsFileMaybeZip(FSFile fsf) {
		if (!(fsf instanceof ZipArchive) && ZipArchive.isZip(fsf)) {
			return new ZipArchive(fsf);
		}
		return fsf;
	}
}
