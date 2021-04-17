package ctrmap.stdlib.arm.elf.rpmconv;

import ctrmap.stdlib.arm.elf.SymbInfo;
import ctrmap.stdlib.cli.ArgumentBuilder;
import ctrmap.stdlib.cli.ArgumentPattern;
import ctrmap.stdlib.cli.ArgumentType;
import ctrmap.stdlib.formats.yaml.Yaml;
import ctrmap.stdlib.fs.accessors.DiskFile;
import ctrmap.stdlib.util.StringEx;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class MAP2ESDB {

	private static final ArgumentPattern[] ARG_PTNS = new ArgumentPattern[]{
		new ArgumentPattern("input", "An input MAP file with function/field names and addresses.", ArgumentType.STRING, null, "-i", "--map", "--input"),
		new ArgumentPattern("output", "An output YML file.", ArgumentType.STRING, "esdb.yml", "-o", "--yml", "--output"),};

	public static void main(String[] args) {
		ArgumentBuilder bld = new ArgumentBuilder(ARG_PTNS);
		bld.parse(args);

		if (bld.getContent("input", true) != null) {
			File in = new File(bld.getContent("input").stringValue());
			File out = new File(bld.getContent("output").stringValue());
			createYML(in, out);
		} else {
			System.out.println("No input file specified.\n");

			printHelp(bld);
		}
	}

	private static void printHelp(ArgumentBuilder bld) {
		System.out.println("MAP2ESDB External symbol database converter 1.0.0\n");

		bld.print();
	}

	public static void createYML(File mapFile, File ymlFile) {
		try {
			Scanner s = new Scanner(mapFile);

			List<SymbInfo> funcInfos = new ArrayList<>();

			while (s.hasNextLine()) {
				String line = s.nextLine();
				int iddd = line.indexOf(":");
				if (iddd != -1) {
					int idWs = StringEx.indexOfFirstWhitespace(line, iddd + 1);
					if (idWs != -1) {
						int addr = Integer.parseUnsignedInt(line.substring(iddd + 1, idWs), 16);
						String name = line.substring(idWs).trim();
						funcInfos.add(new SymbInfo(name, addr));
					}
				}
			}

			Yaml yml = new Yaml();
			for (SymbInfo i : funcInfos) {
				yml.getEnsureRootNodeKeyNode(i.name).setValue("0x" + Integer.toHexString(i.absoluteAddress));
			}
			yml.writeToFile(new DiskFile(ymlFile));
		} catch (FileNotFoundException ex) {
			Logger.getLogger(MAP2ESDB.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
