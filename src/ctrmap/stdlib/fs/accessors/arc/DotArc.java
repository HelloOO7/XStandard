package ctrmap.stdlib.fs.accessors.arc;

import ctrmap.stdlib.fs.FSFile;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class DotArc {

	public static final String DOT_ARC_SIGNATURE = ".arc";

	public CompressorBehavior defaultCompressionDirective = CompressorBehavior.AUTO;
	public Map<String, CompressorBehavior> compressionDirectives = new HashMap<>();

	private FSFile source;

	public DotArc(FSFile dotArcFile) {
		try {
			source = dotArcFile;
			if (dotArcFile == null || !dotArcFile.exists()) {
				return;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(dotArcFile.getInputStream().getInputStream()));
			if (reader.ready()) {
				String signature = reader.readLine();
				if (signature.equals(DOT_ARC_SIGNATURE)) {
					while (reader.ready()) {
						String line = reader.readLine();
						String[] cmds = line.split(" +");
						switch (cmds[0]) {
							case "compress":
								switch (cmds[1]) {
									case "default":
										defaultCompressionDirective = CompressorBehavior.getCBByBId(cmds[2]);
										break;
									default:
										compressionDirectives.put(cmds[1], CompressorBehavior.getCBByBId(cmds[2]));
										break;
								}
								break;
						}
					}
				}
			}
			reader.close();
		} catch (IOException ex) {
			Logger.getLogger(DotArc.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void updateAndWrite() {
		if (source != null) {
			try {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(source.getOutputStream().getOutputStream()));

				writer.write(DOT_ARC_SIGNATURE);
				writer.write(System.lineSeparator());

				writeCmprBehavior(defaultCompressionDirective, "default", writer);

				for (Map.Entry<String, CompressorBehavior> e : compressionDirectives.entrySet()) {
					writeCmprBehavior(e.getValue(), e.getKey(), writer);
				}

				writer.close();
			} catch (IOException ex) {
				Logger.getLogger(DotArc.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private void writeCmprBehavior(CompressorBehavior bhv, String key, Writer writer) throws IOException {
		writer.write("compress ");
		writer.write(key);
		writer.write(' ');
		writer.write(bhv.behaviorId);
		writer.write(System.lineSeparator());
	}

	public static enum CompressorBehavior {
		AUTO("auto"),
		COMPRESS("true"),
		DO_NOT_COMPRESS("false");

		public final String behaviorId;

		private CompressorBehavior(String bId) {
			behaviorId = bId;
		}

		public static CompressorBehavior getCBByBId(String bId) {
			for (CompressorBehavior c : values()) {
				if (c.behaviorId.equals(bId)) {
					return c;
				}
			}
			return AUTO;
		}
	}
}
