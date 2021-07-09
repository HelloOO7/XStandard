package ctrmap.stdlib.formats.rpm.rpz;

import ctrmap.stdlib.formats.rpm.RPM;
import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.fs.accessors.DiskFile;
import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import ctrmap.stdlib.io.util.IOUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A demonstration run for the RPZ format.
 */
public class RPZDemo {

	/**
	 * The directory which the dummy installer should work with.
	 */
	private static final String RPZDEMO_DIR_ROOT = "D:\\_REWorkspace\\pokescript_genv\\rpz\\installer_emulator_dir";
	/**
	 * The RPZ to be installed.
	 */
	private static final String RPZDEMO_RPZ_ROOT = "D:\\_REWorkspace\\pokescript_genv\\rpz\\rpz_dir.zip";
	/**
	 * ID of the installation "target".
	 */
	private static final String RPZDEMO_PLAF_TARGET = "IRDO";

	/**
	 * Attempts to install an RPZ from RPZDEMO_RPZ_ROOT to a 'code.bin' binary
	 * file in RPZDEMO_DIR_ROOT.
	 * Content files will be copied to an 'fs' directory in RPZDEMO_DIR_ROOT.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		FSFile root = new DiskFile(RPZDEMO_DIR_ROOT);
		root.mkdirs();

		FSFile fs = root.getChild("fs");
		fs.mkdir();
		FSFile code = root.getChild("code.bin");

		final Map<String, Integer> installedPatches = new HashMap<>();

		try {
			DataIOStream io = code.getDataIOStream();

			while (io.getPosition() < io.getLength()) {
				int start = io.getPosition();
				IOUtils.SearchResult rsl = IOUtils.searchForBytes(io, io.getPosition(), io.getLength(), new IOUtils.SearchPattern(new byte[]{(byte) 'R', (byte) 'P', (byte) 'M'}));
				if (rsl != null) {
					int end = io.getPosition() + RPM.RPM_FOOTER_SIZE;
					byte[] rpmBytes = new byte[end - start];
					io.seek(start);
					io.read(rpmBytes);
					io.seek(end);
					RPM rpm = new RPM(rpmBytes);
					String prodId = RPZ.getProductIDOfRPM(rpm);
					int prodVer = RPZ.getProductVersionOfRPM(rpm);
					if (prodId != null && prodVer != -1) {
						installedPatches.put(prodId, prodVer);
					}
				}
			}
			io.close();
		} catch (IOException ex) {
			Logger.getLogger(RPZDemo.class.getName()).log(Level.SEVERE, null, ex);
		}

		IRPZHandler handler = new IRPZHandler() {
			@Override
			public int getInstalledProductVersion(String productId) {
				return installedPatches.getOrDefault(productId, -1);
			}

			@Override
			public String getTarget() {
				return RPZDEMO_PLAF_TARGET;
			}

			@Override
			public FSFile getDestContentDirectory() {
				return fs;
			}

			@Override
			public boolean installRPM(RPM rpm) {
				try {
					DataIOStream io = code.getDataIOStream();
					io.seek(io.getLength());
					io.write(rpm.getBytesForBaseOfs(io.getPosition()));
					io.close();
				} catch (IOException ex) {
					Logger.getLogger(RPZDemo.class.getName()).log(Level.SEVERE, null, ex);
				}
				return true;
			}

			@Override
			public boolean throwError(IRPZHandler.RPZErrorCode code, String message) {
				if (message != null) {
					System.err.println(code + ": " + message);
				} else {
					System.out.println(code);
				}
				return true;
			}
		};

		RPZ rpz = new RPZ(new DiskFile(RPZDEMO_RPZ_ROOT));
		rpz.install(handler);
	}
}
