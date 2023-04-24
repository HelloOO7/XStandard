package xstandard.util.collections;

import java.util.Arrays;

public class FloatList extends AbstractPrimitiveList {

	private float[] array;

	public FloatList(int capacity) {
		array = new float[capacity];
	}
	
	public FloatList() {
		this(1);
	}
	
	public FloatList(FloatList source) {
		array = Arrays.copyOf(source.array, source.array.length);
		size = source.size;
	}
	
	public static FloatList wrap(float... values) {
		FloatList il = new FloatList(0);
		il.array = values;
		il.size = values.length;
		return il;
	}

	public float get(int index) {
		rangeCheck(index);
		return array[index];
	}
	
	public void set(int index, float f) {
		if (index == size) {
			add(f);
		} else {
			rangeCheck(index);
			array[index] = f;
		}
	}
	
	public void add(int index, float f) {
		if (index == size) {
			add(f);
		} else {
			rangeCheckAdd(index);
			ensureCapacity(size + 1);
			System.arraycopy(array, index, array, index + 1, size - index);
			array[index] = f;
			size++;
		}
	}

	public void add(float f) {
		ensureCapacity(size + 1);
		array[size++] = f;
	}
	
	public void addAll(int index, float[] array) {
		addAllImpl(index, array, array.length);
	}
	
	public void addAll(FloatList list) {
		addAllImpl(list.array, list.size);
	}
	
	public void addAll(float... floats) {
		addAllImpl(floats, floats.length);
	}
	
	public int indexOf(float value) {
		for (int i = 0; i < size; i++) {
			if (array[i] == value) {
				return i;
			}
		}
		return -1;
	}
	
	public void remove(float f) {
		remove(indexOf(f));
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

	public float[] toArray() {
		return Arrays.copyOf(array, size);
	}
}
