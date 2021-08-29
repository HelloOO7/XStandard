package ctrmap.stdlib.formats.rpm.rpz;

import ctrmap.stdlib.formats.yaml.YamlNodeName;
import ctrmap.stdlib.fs.FSFile;
import java.util.ArrayList;
import java.util.List;

/**
 * A descriptor for file copy operations of RPZ content.
 */
public class RPZContentInfo extends RPZYmlBase {

	@YamlNodeName("Content")
	public List<RPZContentReference> content;

	public RPZContentInfo(FSFile fsf) {
		super(fsf);
	}

	public RPZContentInfo() {
		content = new ArrayList<>();
	}

	/**
	 * Reference to a file in an RPZ's 'content' directory.
	 */
	public static class RPZContentReference {

		/**
		 * Path to the file/directory, relative to the 'content' folder.
		 */
		@YamlNodeName("SourcePath")
		public String sourcePath;
		/**
		 * Path in the installer's DestContentDirectory to which the content
		 * should be copied.
		 */
		@YamlNodeName("DestinationPath")
		public String destinationPath;
	}
}
