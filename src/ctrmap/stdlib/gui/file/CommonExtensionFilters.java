package ctrmap.stdlib.gui.file;

public class CommonExtensionFilters {
	public static final ExtensionFilter PNG = new ExtensionFilter("Portable Network Graphics", "*.png");
	public static final ExtensionFilter EXE = new ExtensionFilter("Win32 executable", "*.exe");
	public static final ExtensionFilter LINUX_EXE = new ExtensionFilter("Unix executable", "*");
	public static final ExtensionFilter ELF = new ExtensionFilter("Executable Linkable Format", "*.elf");
	public static final ExtensionFilter PLAIN_TEXT = new ExtensionFilter("Plain text", "*.txt");
	public static final ExtensionFilter LINKER_MAP = new ExtensionFilter("Linker address map", "*.map");
	public static final ExtensionFilter ALL = new ExtensionFilter("All files", "*.*");
}
