package ctrmap.stdlib.formats.rpm;

/**
 * Interface to resolve symbols outside of an RPM.
 */
public interface RPMExternalSymbolResolver {

	/**
	 * Callback queried when an RPM needs a symbol outside of it.
	 *
	 * @param namespace The namespace of the requested symbol.
	 * Implementation-specific.
	 * @param name Name of the symbol in the RPM looked up by namespace.
	 * @return The external RPMSymbol resolved, or null if it could not be
	 * found.
	 */
	public RPMSymbol resolveExSymbol(String namespace, String name);
}
