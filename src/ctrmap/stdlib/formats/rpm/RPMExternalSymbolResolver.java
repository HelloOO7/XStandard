
package ctrmap.stdlib.formats.rpm;

/**
 *
 */
public interface RPMExternalSymbolResolver {
	public RPMSymbol resolveExSymbol(String namespace, String name);
}
