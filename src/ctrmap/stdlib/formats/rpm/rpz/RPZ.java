package ctrmap.stdlib.formats.rpm.rpz;

import ctrmap.stdlib.formats.rpm.RPM;
import ctrmap.stdlib.formats.rpm.RPMMetaData;
import ctrmap.stdlib.formats.zip.ZipArchive;
import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.accessors.FSFileAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Relocatable Program Zip
 *
 * Format handler.
 */
public class RPZ extends FSFileAdapter {

	public static final String RPM_MVK_RPZ_PRODUCT_ID = "ProductID";
	public static final String RPM_MVK_RPZ_PRODUCT_VERSION = "ProductVersion";

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

		if (!isRPZ(file)) {
			throw new IllegalArgumentException("File is not an RPZ!");
		}

		meta = file.getChild(RPZ_META_DIRNAME);
		code = file.getChild(RPZ_CODE_DIRNAME);
		content = file.getChild(RPZ_CONTENT_DIRNAME);

		index = new RPZIndex(meta.getChild(RPZ_INDEX_FILENAME));
	}

	public RPZ(FSFile file, String productId) {
		super(file);

		meta = file.getChild(RPZ_META_DIRNAME);
		code = file.getChild(RPZ_CODE_DIRNAME);
		content = file.getChild(RPZ_CONTENT_DIRNAME);

		meta.mkdir();
		code.mkdir();
		content.mkdir();

		index = new RPZIndex(meta.getChild(RPZ_INDEX_FILENAME), productId);
	}

	/**
	 * Gets the Index metadata of the RPZ.
	 *
	 * @return
	 */
	public RPZIndex getIndex() {
		return index;
	}

	/**
	 * Gets the unique Product ID of the RPZ, as per its index.
	 *
	 * @return
	 */
	public String getProductId() {
		return index.productId;
	}

	/**
	 * Gets the friendly name of the RPZ's program.
	 *
	 * @return
	 */
	public String getProductName() {
		return index.productId;
	}

	/**
	 * Gets the Product ID of an RPM installed from an RPZ. The Product ID is
	 * inserted as a reserved metadata value when installed. If the value is not
	 * found in the RPM's metadata, the function returns null.
	 *
	 * @param rpm The RPM to return the Product ID of.
	 * @return
	 */
	public static String getProductIDOfRPM(RPM rpm) {
		RPMMetaData.RPMMetaValue prodId = rpm.metaData.findValue(RPM_MVK_RPZ_PRODUCT_ID);
		if (prodId != null) {
			return prodId.stringValue();
		}
		return null;
	}

	/**
	 * Gets the Product Version of an RPM installed from an RPZ. The Product
	 * Version is inserted as a reserved metadata value when installed. If the
	 * value is not found in the RPM's metadata, the function returns -1.
	 *
	 * @param rpm The RPM to return the Product ID of.
	 * @return Product Version of the RPM, or -1 if none detected.
	 */
	public static int getProductVersionOfRPM(RPM rpm) {
		RPMMetaData.RPMMetaValue prodVer = rpm.metaData.findValue(RPM_MVK_RPZ_PRODUCT_VERSION);
		if (prodVer != null) {
			return prodVer.intValue();
		}
		return -1;
	}

	/**
	 * Gets the friendly description from this RPZ's metadata.
	 * @return 
	 */
	public String getDescription() {
		return index.description;
	}

	/**
	 * Gets the Product Version of the RPZ.
	 * @return 
	 */
	public RPZVersion getVersion() {
		return index.version;
	}

	/**
	 * Installs the RPZ using an IRPZHandler.
	 * @param handler The handler/installer to use.
	 * @return True if the installation succeeded.
	 */
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

			Map<String, List<RPZModuleInfo>> moduleInfos = new HashMap<>();

			for (RPZYmlBase.RPZYmlReference moduleRef : index.modules) {
				FSFile moduleInfoFile = meta.getChild(moduleRef.ymlPath);
				if (!moduleInfoFile.exists()) {
					if (handler.throwError(IRPZHandler.RPZErrorCode.FILE_NOT_FOUND, "Referenced module info " + moduleRef.ymlPath + " not present in RPZ!")) {
						return false;
					}
				}
				RPZModuleInfo moduleInfo = new RPZModuleInfo(moduleInfoFile);
				List<RPZModuleInfo> moduleInfoList = moduleInfos.get(moduleInfo.productId);
				if (moduleInfoList == null){
					moduleInfoList = new ArrayList<>();
					moduleInfos.put(moduleInfo.productId, moduleInfoList);
				}
				moduleInfoList.add(moduleInfo);
			}
			targetModuleInfo = findModuleByTarget(moduleInfos.get(getProductId()), desiredTarget);

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
	
	private RPZModuleInfo findModuleByTarget(List<RPZModuleInfo> l, String target){
		if (l != null){
			for (RPZModuleInfo i : l){
				if (i.supportsTarget(target)){
					return i;
				}
			}
		}
		return null;
	}

	private boolean installModule(RPZModuleInfo module, Map<String, List<RPZModuleInfo>> modules, IRPZHandler handler) {
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
				if (ref.destinationPath == null) {
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
				if (dep.hasBundledDepModule) {
					RPZModuleInfo depModuleInfo = findModuleByTarget(modules.get(dep.productId), handler.getTarget());
					if (depModuleInfo == null) {
						boolean errorResult;
						if (modules.containsKey(dep.productId)){
							errorResult = handler.throwError(IRPZHandler.RPZErrorCode.TARGET_NOT_SUPPORTED, "Dependency " + dep.productId + " does not support target " + handler.getTarget() + ".");
						}
						else {
							errorResult = handler.throwError(IRPZHandler.RPZErrorCode.DEPENDENCY_NOT_RESOLVED, "Module info for dependency " + dep.productId + " not found.");
						}
						if (errorResult) {
							return false;
						}
					} else {
						if (!depModuleInfo.isDependencyModule) {
							if (handler.throwError(IRPZHandler.RPZErrorCode.DEPENDENCY_NOT_RESOLVED, "Module info for dependency is not flagged as a dependency. (" + dep.productId + ")")) {
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
		execRpm.metaData.putValue(new RPMMetaData.RPMMetaValue(RPM_MVK_RPZ_PRODUCT_ID, getProductId()));
		execRpm.metaData.putValue(new RPMMetaData.RPMMetaValue(RPM_MVK_RPZ_PRODUCT_VERSION, getVersion().number));
		if (module.stripSymbolNames){
			execRpm.stripSymbolNames();
		}

		if (!handler.installRPM(execRpm)) {
			handler.throwError(IRPZHandler.RPZErrorCode.RPM_INSTALL_FAILED, "Could not install module RPM (" + execRpmFile + ").");
			return false;
		}
		return true;
	}

	/**
	 * Checks if a directory/archive can be read as an RPZ.
	 * @param fsf The directory or a ZIP archive to check.
	 * @return True if the directory/archive contains RPZ data.
	 */
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
