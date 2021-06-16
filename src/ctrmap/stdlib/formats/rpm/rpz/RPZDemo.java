package ctrmap.stdlib.formats.rpm.rpz;

import ctrmap.stdlib.formats.rpm.RPM;
import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.fs.accessors.DiskFile;
import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import ctrmap.stdlib.io.util.BitUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RPZDemo {

	private static final String RPZDEMO_DIR_ROOT = "D:\\_REWorkspace\\pokescript_genv\\rpz\\installer_emulator_dir";
	private static final String RPZDEMO_RPZ_ROOT = "D:\\_REWorkspace\\pokescript_genv\\rpz\\rpz_dir.zip";
	private static final String RPZDEMO_PLAF_TARGET = "IRDO";

	public static void main(String[] args) {
		FSFile root = new DiskFile(RPZDEMO_DIR_ROOT);

		FSFile fs = root.getChild("fs");
		FSFile code = root.getChild("code.bin");
		
		final Map<String, Integer> installedPatches = new HashMap<>();
		
		try {
			DataIOStream io = code.getDataIOStream();

			while (io.getPosition() < io.getLength()){
				int start = io.getPosition();
				BitUtils.SearchResult rsl = BitUtils.searchForBytes(io, io.getPosition(), io.getLength(), new BitUtils.SearchPattern(new byte[]{(byte)'R', (byte)'P', (byte)'M'}));
				if (rsl != null){
					int end = io.getPosition() + RPM.RPM_FOOTER_SIZE;
					byte[] rpmBytes = new byte[end - start];
					io.seek(start);
					io.read(rpmBytes);
					io.seek(end);
					RPM rpm = new RPM(rpmBytes);
					installedPatches.put(rpm.productId, rpm.productVersion);
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
