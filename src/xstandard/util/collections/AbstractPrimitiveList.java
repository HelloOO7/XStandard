package xstandard.util.collections;

import java.lang.reflect.Array;
import java.util.Objects;

public abstract class AbstractPrimitiveList {

	protected int size = 0;

	protected abstract int getArrayLength();

	protected abstract void growArray(int newSize);

	protected abstract Object getArray();

	protected void ensureCapacity(int cap) {
		int nowsz = getArrayLength();
		if (cap >= nowsz) {
			int newcap = nowsz + (nowsz >> 1);
			if (cap > newcap) {
				newcap = cap;
			}
			growArray(newcap);
		}
	}

	public void trimToSize(int newSize) {
		if (newSize <= size) {
			size = newSize;
		} else {
			throw new ArrayIndexOutOfBoundsException(newSize);
		}
	}

	protected void rangeCheckAdd(int idx) {
		if (idx >= 0 && idx <= size) {
			return;
		}
		throw new ArrayIndexOutOfBoundsException("Add index " + idx + " out of bounds for size " + size);
	}

	protected void rangeCheck(int idx) {
		if (idx >= 0 && idx < size) {
			return;
		}
		throw new ArrayIndexOutOfBoundsException("Index " + idx + " out of bounds for size " + size);
	}

	public void clear() {
		size = 0;
		growArray(1);
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	protected void addAllImpl(int index, Object newArray, int newArrayLength) {
		rangeCheckAdd(index);
		int mincap = size + newArrayLength;
		ensureCapacity(mincap);
		Object array = getArray();
		System.arraycopy(array, index, array, index + newArrayLength, size - index);
		System.arraycopy(newArray, 0, array, index, newArrayLength);
		size += newArrayLength;
	}

	protected void addAllImpl(Object newArray, int newArrayLength) {
		ensureCapacity(size + newArrayLength);
		System.arraycopy(newArray, 0, getArray(), size, newArrayLength);
		size += newArrayLength;
	}

	public void remove(int index) {
		rangeCheck(index);
		Object array = getArray();
		int iplus1 = index + 1;
		if (iplus1 < size) {
			System.arraycopy(array, iplus1, array, index, size - iplus1);
		}
		size--;
	}

	@Override
	public String toString() {
		Object arr = getArray();
		int count = size;

		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < count; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(Array.get(arr, i));
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		int len = getArrayLength();
		Object arr = getArray();
		for (int i = 0; i < len; i++) {
			hash = 31 * hash + Objects.hashCode(Array.get(arr, i));
		}
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if (o != null) {
			if (o.getClass() == getClass()) {
				int len = getArrayLength();
				int len2 = ((AbstractPrimitiveList) o).getArrayLength();
				if (len == len2) {
					Object arr = getArray();
					Object arr2 = ((AbstractPrimitiveList) o).getArray();
					for (int i = 0; i < len; i++) {
						if (!Objects.equals(Array.get(arr, i), Array.get(arr2, i))) {
							return false;
						}
					}
					return true;
				}
			}
		}
		return false;
	}
}
