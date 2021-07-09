package ctrmap.stdlib.io.structs;

import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An I/O array of pointers to data.
 */
public class PointerTable {

	/**
	 * Creates a pointer table without an offset base and with its size written.
	 *
	 * @param size Count of pointers in the table.
	 * @param dos Stream to allocate on.
	 * @return List of temporary offsets representing the pointer table.
	 * @throws IOException
	 */
	public static List<TemporaryOffset> allocatePointerTable(int size, DataIOStream dos) throws IOException {
		return allocatePointerTable(size, dos, 0, true);
	}

	/**
	 * Creates a pointer table using a data stream.
	 *
	 * @param size Count of pointers in the table.
	 * @param dos Stream to allocate on.
	 * @param offsetBase An offset to be added to each written pointer.
	 * @param writeSize True if the pointer count should be written before the pointer table.
	 * @return List of temporary offsets representing the pointer table.
	 * @throws IOException
	 */
	public static List<TemporaryOffset> allocatePointerTable(int size, DataIOStream dos, int offsetBase, boolean writeSize) throws IOException {
		if (writeSize) {
			dos.writeInt(size); //table length
		}
		List<TemporaryOffset> table = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			table.add(new TemporaryOffset(dos, offsetBase)); //the temporary offset constructor allocates the memory space automatically
		}
		return table;
	}

	private int[] pointers;
	private int idx = 0;
	
	private final DataIOStream dis;

	/**
	 * Reads a pointer table from a stream.
	 * @param dis A data stream.
	 * @throws IOException 
	 */
	public PointerTable(DataIOStream dis) throws IOException {
		this.dis = dis;
		pointers = new int[dis.readInt()];

		for (int i = 0; i < pointers.length; i++) {
			pointers[i] = dis.readInt();
		}
	}

	/**
	 * Check if there are unhandled pointers remaining in the table.
	 * @return True if the end of table has not been reached.
	 * @throws IOException 
	 */
	public boolean hasNext() throws IOException {
		return idx < pointers.length;
	}

	/**
	 * Seeks to the next pointer in the table.
	 * @throws IOException 
	 */
	public void next() throws IOException {
		if (!hasNext()){
			throw new ArrayIndexOutOfBoundsException("End of the pointer table.");
		}
		dis.seek(pointers[idx]);
		idx++;
	}
}
