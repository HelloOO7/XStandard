package xstandard.util.collections;

import java.util.Arrays;

public class IntList extends AbstractPrimitiveList {

	private int[] array;

	public IntList(int capacity) {
		array = new int[capacity];
	}
	
	public IntList() {
		this(1);
	}
	
	public IntList(IntList source) {
		array = Arrays.copyOf(source.array, source.array.length);
		size = source.size;
	}
	
	public static IntList wrap(int... values) {
		IntList il = new IntList(0);
		il.array = values;
		il.size = values.length;
		return il;
	}

	public int get(int index) {
		rangeCheck(index);
		return array[index];
	}
	
	public void set(int index, int i) {
		if (index == size) {
			add(i);
		} else {
			rangeCheck(index);
			array[index] = i;
		}
	}
	
	public void add(int index, int i) {
		if (index == size) {
			add(i);
		} else {
			rangeCheckAdd(index);
			ensureCapacity(index + 1);
			System.arraycopy(array, index, array, index + 1, size - index);
			array[index] = i;
			size++;
		}
	}

	public void add(int i) {
		ensureCapacity(size + 1);
		array[size++] = i;
	}
	
	public void addAll(IntList intList) {
		addAllImpl(intList.array, intList.size);
	}
	
	public void addAll(int index, int[] array) {
		addAllImpl(index, array, array.length);
	}
	
	public void addAll(int... ints) {
		addAllImpl(ints, ints.length);
	}
	
	public int indexOf(int value) {
		for (int i = 0; i < size; i++) {
			if (array[i] == value) {
				return i;
			}
		}
		return -1;
	}
	
	public boolean contains(int value) {
		return indexOf(value) != -1;
	}
	
	public void removeValue(int val) {
		remove(indexOf(val));
	}

	@Override
	protected int getArrayLength() {
		return array.length;
	}

	@Override
	protected void growArray(int newSize) {
		array = Arrays.copyOf(array, newSize);
	}

	@Override
	protected Object getArray() {
		return array;
	}

	public int[] toArray() {
		return Arrays.copyOf(array, size);
	}
}
