
package xstandard.io;

/**
 * Thrown if a file version is not supported.
 */
public class UnsupportedVersionException extends RuntimeException {

	public UnsupportedVersionException(String cause) {
		super(cause);
	}

}
