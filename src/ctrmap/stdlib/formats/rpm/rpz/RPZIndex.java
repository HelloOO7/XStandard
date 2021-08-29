package ctrmap.stdlib.formats.rpm.rpz;

import ctrmap.stdlib.formats.yaml.YamlNodeName;
import ctrmap.stdlib.fs.FSFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Index metadata of an RPZ.
 */
public class RPZIndex extends RPZYmlBase {

	/**
	 * The RPZ's unique Product ID.
	 */
	@YamlNodeName("ProductId")
	public String productId;
	/**
	 * A user-friendly description of the main program contained in the RPZ.
	 */
	@YamlNodeName("Description")
	public String description;
	/**
	 * References to all modules in the RPZ.
	 */
	@YamlNodeName("Modules")
	public List<RPZYmlReference> modules;

	/**
	 * Loads an RPZ index from a file.
	 *
	 * @param fsf The file to load from.
	 */
	public RPZIndex(FSFile fsf) {
		super(fsf);
	}

	/**
	 * Initializes an RPZ index in the given location with a Product ID.
	 *
	 * @param fsf The file to initialize the index in.
	 * @param productId The Product ID to assign to the RPZ index.
	 */
	public RPZIndex(FSFile fsf, String productId) {
		modules = new ArrayList<>();
		this.productId = productId;
		writeToFile(fsf);
	}
}
