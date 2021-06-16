package ctrmap.stdlib.formats.rpm.rpz;

import ctrmap.stdlib.formats.rpm.RPM;
import ctrmap.stdlib.fs.FSFile;

public interface IRPZHandler {

	/**
	 * Returns the currently installed version of a product.
	 * @param productId A unique identifier of the product.
	 * @return The installed product version, or -1 if it is not installed.
	 */
	public int getInstalledProductVersion(String productId);

	/**
	 * Returns the target platform desired by the handler.
	 *
	 * @return An arbitrary string that defines the target.
	 */
	public String getTarget();

	/**
	 * Returns an FSFile root to which files from the content directory should be transferred.
	 *
	 * @return A writeable FSFile directory for the RPZ library.
	 */
	public FSFile getDestContentDirectory();

	/**
	 * Requests the handler to install a module to its subsystem.
	 *
	 * @param rpm An RPM file obtained from the `code` directory of the RPZ that is fed to the handler.
	 * @return `true` if the installation finished without problems.
	 */
	public boolean installRPM(RPM rpm);

	/**
	 * Raises an exception to the handler, which may attempt a request to ignore it.
	 *
	 * @param code The error code to specify the handler's behavior.
	 * @param message Additional message to help describing the cause of the error.
	 * @return Whether or not the RPZ library should abort execution.
	 */
	public boolean throwError(RPZErrorCode code, String message);

	public default boolean throwError(RPZErrorCode code) {
		return throwError(code, null);
	}

	public static enum RPZErrorCode {
		UNKNOWN,
		RPZ_MALFORMED,
		ALREADY_INSTALLED,
		HIGHER_VERSION_INSTALLED,
		TARGET_NOT_SUPPORTED,
		DEPENDENCY_NOT_RESOLVED,
		RPM_INSTALL_FAILED,
		FILE_NOT_FOUND
	}
}
