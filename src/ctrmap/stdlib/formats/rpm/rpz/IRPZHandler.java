package ctrmap.stdlib.formats.rpm.rpz;

import ctrmap.stdlib.formats.rpm.RPM;
import ctrmap.stdlib.fs.FSFile;

public interface IRPZHandler {

	/**
	 * Returns the currently installed version of a product.
	 *
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
	 * Returns an FSFile root to which files from the content directory should
	 * be transferred.
	 *
	 * @return A writeable FSFile directory for the RPZ library.
	 */
	public FSFile getDestContentDirectory();

	/**
	 * Requests the handler to install a module to its subsystem.
	 *
	 * @param rpm An RPM file obtained from the `code` directory of the RPZ that
	 * is fed to the handler.
	 * @return `true` if the installation finished without problems.
	 */
	public boolean installRPM(RPM rpm);

	/**
	 * Raises an exception to the handler, which may attempt a request to ignore
	 * it.
	 *
	 * @param code The error code to specify the handler's behavior.
	 * @param message Additional message to help describing the cause of the
	 * error.
	 * @return Whether or not the RPZ library should abort execution.
	 */
	public boolean throwError(RPZErrorCode code, String message);

	public default boolean throwError(RPZErrorCode code) {
		return throwError(code, null);
	}

	public static enum RPZErrorCode {
		/**
		 * An unknown, unhandled error.
		 */
		UNKNOWN,
		/**
		 * The RPZ is corrupted.
		 */
		RPZ_MALFORMED,
		/**
		 * The exact version of a product is already installed on the target.
		 */
		ALREADY_INSTALLED,
		/**
		 * A higher version of a product is already installed on the target.
		 */
		HIGHER_VERSION_INSTALLED,
		/**
		 * The target is not supported by this program.
		 */
		TARGET_NOT_SUPPORTED,
		/**
		 * A program dependency could not be found.
		 */
		DEPENDENCY_NOT_RESOLVED,
		/**
		 * Installation of a module failed.
		 */
		RPM_INSTALL_FAILED,
		/**
		 * A generic file could not be found.
		 */
		FILE_NOT_FOUND
	}
}
