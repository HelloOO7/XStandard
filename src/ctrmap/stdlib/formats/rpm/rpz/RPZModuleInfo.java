package ctrmap.stdlib.formats.rpm.rpz;

import ctrmap.stdlib.formats.yaml.YamlNodeName;
import ctrmap.stdlib.fs.FSFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Information about an RPM module inside an RPZ.
 */
public class RPZModuleInfo extends RPZYmlBase {

	/**
	 * The module is a dependency of the main module or one of its dependencies.
	 */
	@YamlNodeName("IsDependencyModule")
	public boolean isDependencyModule;
	/**
	 * The module should be stripped of symbol names during installation.
	 */
	@YamlNodeName("StripSymbolNames")
	public boolean stripSymbolNames;
	/**
	 * The module's unique Product ID.
	 */
	@YamlNodeName("ProductId")
	public String productId;
	/**
	 * The name of the describes RPM, inside the 'code' directory.
	 */
	@YamlNodeName("RPMName")
	public String rpmName;
	/**
	 * The targets that this module supports.
	 */
	@YamlNodeName("Targets")
	public List<RPZTarget> targets;

	/**
	 * The dependencies required to install the module.
	 */
	@YamlNodeName("Dependencies")
	public List<RPZDependency> dependencies;

	/**
	 * Reference to the RPMContentInfo of the module.
	 */
	@YamlNodeName("ContentInfo")
	public RPZYmlReference contentInfo;

	/**
	 * Loads an RPZ module info from a file.
	 *
	 * @param fsf The file to load from.
	 */
	public RPZModuleInfo(FSFile fsf) {
		super(fsf);

		if (dependencies == null) {
			dependencies = new ArrayList<>();
		}
		if (targets == null) {
			targets = new ArrayList<>();
		}
	}

	public RPZModuleInfo() {
		targets = new ArrayList<>();
		dependencies = new ArrayList<>();
	}

	/**
	 * Checks if the module supports the given Target.
	 *
	 * @param target Name of the target.
	 * @return True if this module's targets contain the requested Target.
	 */
	public boolean supportsTarget(String target) {
		for (RPZModuleInfo.RPZTarget tgt : targets) {
			if (Objects.equals(target, tgt.targetName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * An installation target for an RPZ's module.
	 */
	public static class RPZTarget {

		/**
		 * Name of the target.
		 */
		@YamlNodeName("TargetName")
		public String targetName;
	}

	/**
	 * Information about an RPZ module's dependency.
	 */
	public static class RPZDependency {

		/**
		 * Unique Product ID of the dependency.
		 */
		@YamlNodeName("ProductId")
		public String productId;
		/**
		 * Required version of the dependency. See versionOperator.
		 */
		@YamlNodeName("Version")
		public int version;
		/**
		 * Orders how the product'sinstalled version on the target must be
		 * related to the 'version' field's value.
		 */
		@YamlNodeName("VersionOperator")
		public VersionCmpOp versionOperator;
		/**
		 * Indicates that the RPZ contains the required dependency bundled with
		 * the main program. The dependency will be searched for among the RPZ's
		 * module infos using the Product ID.
		 */
		@YamlNodeName("HasBundledDepModule")
		public boolean hasBundledDepModule;

		public static enum VersionCmpOp {
			LESS,
			EQUAL,
			GEQUAL
		}
	}
}
