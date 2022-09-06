package xstandard.io.structs;

import xstandard.INamed;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.util.StringIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for writing various flavours of string tables.
 */
public class StringTable {

	private Map<String, List<TemporaryOffset>> registOffsets = new HashMap<>();

	private Map<String, Integer> writtenStringOffsets = new HashMap<>();

	private DataIOStream out;

	private boolean isOffsetU16 = false;
	private boolean isOffsRelToTable = false;

	private boolean writingFinished = false;
	
	public StringTable(DataIOStream out) {
		this(out, false, false);
	}

	/**
	 * Creates a string table handler over a DataIOStream.
	 *
	 * @param out
	 * @param isOffsetU16 Should TemporaryOffsetShort be used instead of
	 * TemporaryOffset.
	 * @param isOffsRelToTable Should offsets be relative to the table start
	 * instead of the data stream.
	 */
	public StringTable(DataIOStream out, boolean isOffsetU16, boolean isOffsRelToTable) {
		this.out = out;
		this.isOffsetU16 = isOffsetU16;
		this.isOffsRelToTable = isOffsRelToTable;
	}
	
	public void forbidFurtherWriting() {
		writingFinished = true;
	}

	/**
	 * Gets the current total of strings in the table.
	 */
	public int getStringCount() {
		return registOffsets.size() + writtenStringOffsets.size();
	}

	/**
	 * Gets the address of a string that was written when flushing this string
	 * table.
	 *
	 * @param str The string.
	 * @return Address of the string in the output stream. NullPointerException
	 * occurs if the string wasn't written.
	 */
	public int getAddrOfWrittenString(String str) {
		return writtenStringOffsets.get(str);
	}

	/**
	 * Puts an INamed's name to the table.
	 *
	 * @param in an INamed.
	 */
	public void putINamed(INamed in) {
		putString(in.getName());
	}

	/**
	 * Puts an INamed's name to the table and writes a temporary pointer to it
	 * to the data stream.
	 *
	 * @param in
	 */
	public void putINamedOffset(INamed in) {
		putStringOffset(in.getName());
	}

	/**
	 * Puts the names of all INamed elements in a list to the table.
	 *
	 * @param nameds List of INamed elements.
	 */
	public void putINamedList(List<? extends INamed> nameds) {
		for (INamed in : nameds) {
			if (in != null) {
				putString(in.getName());
			}
		}
	}

	/**
	 * Puts a String list to the table.
	 *
	 * @param strings A list of Strings.
	 */
	public void putStrings(List<String> strings) {
		for (String str : strings) {
			putString(str);
		}
	}

	/**
	 * Puts a String to the table.
	 *
	 * @param str A String.
	 */
	public void putString(String str) {
		if (str != null && (!(writtenStringOffsets.containsKey(str) || registOffsets.containsKey(str)))) {
			registOffsets.put(str, new ArrayList<>());
		}
	}

	/**
	 * Puts a String to the table and writes a temporary pointer to it to the
	 * data stream.
	 *
	 * @param str A String.
	 */
	public void putStringOffset(String str) {
		try {
			if (writtenStringOffsets.containsKey(str) || str == null) {
				int off = (str == null) ? 0 : writtenStringOffsets.get(str);
				if (isOffsetU16) {
					out.writeShort(off);
				} else {
					out.writeInt(off);
				}
			} else {
				if (writingFinished) {
					throw new RuntimeException("This string table is now read-only.");
				}
				List<TemporaryOffset> l = registOffsets.get(str);
				if (l == null) {
					l = new ArrayList<>();
					registOffsets.put(str, l);
				}
				if (isOffsetU16) {
					l.add(new TemporaryOffsetShort(out));
				} else {
					l.add(new TemporaryOffset(out));
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(StringTable.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Writes the table's strings to the data stream it was created with at the
	 * current position.
	 *
	 * @throws IOException
	 */
	public void writeTable() throws IOException {
		writeTable(out);
	}
	
	private List<Map.Entry<String, List<TemporaryOffset>>> getSortedEntries(){
		List<Map.Entry<String, List<TemporaryOffset>>> entries = new ArrayList<>(registOffsets.entrySet());
		entries.sort((Map.Entry<String, List<TemporaryOffset>> o1, Map.Entry<String, List<TemporaryOffset>> o2) -> {
			String s1 = o1.getKey();
			String s2 = o2.getKey();
			if (s1 == null){
				return -1;
			}
			if (s2 == null){
				return 1;
			}
			return s1.compareTo(s2);
		});
		return entries;
	}

	/**
	 * Writes the table's strings to a data stream at the current position.
	 *
	 * @param io The data stream to write into.
	 * @throws IOException
	 */
	public void writeTable(DataIOStream io) throws IOException {
		int baseOfs = isOffsRelToTable ? io.getPosition() : 0;
		if (isOffsRelToTable){
			//Null string at 0 ptr
			io.write(0);
		}
		for (Map.Entry<String, List<TemporaryOffset>> e : getSortedEntries()) {
			for (TemporaryValue o : e.getValue()) {
				o.set(io.getPosition() - baseOfs);
			}
			writtenStringOffsets.put(e.getKey(), io.getPosition() - baseOfs);
			StringIO.writeString(io, e.getKey());
		}
		registOffsets.clear();
	}
}
