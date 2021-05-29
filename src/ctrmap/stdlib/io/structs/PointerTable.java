package ctrmap.stdlib.io.structs;

import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PointerTable {

	public static List<TemporaryOffset> allocatePointerTable(int size, DataIOStream dos) throws IOException {
		return allocatePointerTable(size, dos, 0, true);
	}

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

	public int[] pointers;
	int idx = 0;

	public PointerTable(DataIOStream dis) throws IOException {
		pointers = new int[dis.readInt()];

		for (int i = 0; i < pointers.length; i++) {
			pointers[i] = dis.readInt();
		}
	}

	public boolean hasNext() throws IOException {
		return idx < pointers.length;
	}

	public void next(DataIOStream dis) throws IOException {
		dis.seek(pointers[idx]);
		idx++;
	}
}
