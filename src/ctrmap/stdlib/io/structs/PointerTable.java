package ctrmap.stdlib.io.structs;

import ctrmap.stdlib.io.iface.SeekableDataInput;
import ctrmap.stdlib.io.iface.SeekableDataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PointerTable {

	public static List<TemporaryOffset> allocatePointerTable(int size, SeekableDataOutput dos) throws IOException {
		return allocatePointerTable(size, dos, 0, true);
	}

	public static List<TemporaryOffset> allocatePointerTable(int size, SeekableDataOutput dos, int offsetBase, boolean writeSize) throws IOException {
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

	public PointerTable(SeekableDataInput dis) throws IOException {
		pointers = new int[dis.readInt()];

		for (int i = 0; i < pointers.length; i++) {
			pointers[i] = dis.readInt();
		}
	}

	public boolean hasNext() throws IOException {
		return idx < pointers.length;
	}

	public void next(SeekableDataInput dis) throws IOException {
		dis.seek(pointers[idx]);
		idx++;
	}
}
