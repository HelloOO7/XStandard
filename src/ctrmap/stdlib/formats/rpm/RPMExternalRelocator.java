package ctrmap.stdlib.formats.rpm;

/**
 * An interface for processing relocations from remote symbols.
 */
public interface RPMExternalRelocator {

	/**
	 * Callback to query the relocator to process a relocation on an external
	 * symbol.
	 *
	 * @param rpm The RPM of the relocation.
	 * @param rel The relocation to process.
	 */
	public void processExternalRelocation(RPM rpm, RPMRelocation rel);
}
