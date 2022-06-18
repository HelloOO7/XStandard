package ctrmap.stdlib.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Methods for array operations.
 */
public class ArraysEx {

	/**
	 * Creates an array from varargs parameters.
	 * @param <T> Type of the array components.
	 * @param elems Varargs elements to convert to an array.
	 * @return An array containing the varargs elements in order.
	 */
	public static <T> T[] asArray(T... elems) {
		return elems;
	}
	
	/**
	 * Converts a List\<Integer\> to an int[].
	 * @param intList A List of Integer objects.
	 * @return An int[] primitive.
	 */
	public static int[] asArrayI(List<Integer> intList) {
		int[] arr = new int[intList.size()];
		for (int i = 0; i < arr.length; i++){
			arr[i] = intList.get(i);
		}
		return arr;
	}
	
	/**
	 * Converts a List\<Float\> to an int[].
	 * @param floatList A List of Float objects.
	 * @return A float[] primitive.
	 */
	public static float[] asArrayF(List<Float> floatList) {
		float[] arr = new float[floatList.size()];
		for (int i = 0; i < arr.length; i++){
			arr[i] = floatList.get(i);
		}
		return arr;
	}
	
	/**
	 * Converts a List\<Short\> to an int[].
	 * @param shortList A List of Short objects.
	 * @return A short[] primitive.
	 */
	public static short[] asArrayS(List<Short> shortList) {
		short[] arr = new short[shortList.size()];
		for (int i = 0; i < arr.length; i++){
			arr[i] = shortList.get(i);
		}
		return arr;
	}

	/**
	 * Converts an array or varargs to a List.
	 * @param <T> Component type of the source array and the output list.
	 * @param elems Elements to be added to the output List.
	 * @return A list containing all elements of 'elems'.
	 */
	public static <T> List<T> asList(T... elems) {
		List<T> r = new ArrayList<>();
		for (T t : elems) {
			r.add(t);
		}
		return r;
	}

	/**
	 * Adds an element into a Collection only if it is non-null and not already present in the Collection.
	 * @param <T> Component type.
	 * @param list List to be added into.
	 * @param elem Element to add to the collection.
	 * @return True if the collection has changed as a result of this operation.
	 */
	public static <T> boolean addIfNotNullOrContains(Collection<T> list, T elem) {
		if (elem != null && !list.contains(elem)) {
			return list.add(elem);
		}
		return false;
	}

	/**
	 * Adds the elements of a collection into another only if they are non-null and not already present in the Collection.
	 * @param <T> Component type.
	 * @param list Collection to be added into.
	 * @param toAdd Collection to add from.
	 * @return True if the collection has changed as a result of this operation.
	 */
	public static <T> boolean addAllIfNotNullOrContains(Collection<T> list, Collection<? extends T> toAdd) {
		boolean r = false;
		for (T e : toAdd) {
			r |= addIfNotNullOrContains(list, e);
		}
		return r;
	}
	
	public static <T> int elementCount(Iterable<T[]> arrays, boolean countNullAs1Size) {
		int count = 0;
		for (T[] arr : arrays) {
			if (arr == null && countNullAs1Size) {
				count++;
			} else {
				count += arr.length;
			}
		}
		return count;
	}
	
	public static <T> T[] join(Iterable<T[]> arrays, T[] out, boolean addNull) {
		int outIndex = 0;
		for (T[] arr : arrays) {
			if (arr == null && addNull) {
				out[outIndex++] = null;
			} else {
				System.arraycopy(arr, 0, out, outIndex, arr.length);
				outIndex += arr.length;
			}
		}
		return out;
	}

	/**
	 * Converts a list to a String with each element separated by ', '
	 * @param list The list to print.
	 * @return A String representation of the list.
	 */
	public static String toString(List list) {
		return toString(list.toArray(new Object[list.size()]));
	}

	/**
	 * Converts an array to a String with each element separated by ', '
	 * @param list The array to print.
	 * @return A String representation of the array.
	 */
	public static String toString(Object... list) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.length; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(list[i].toString());
		}
		return sb.toString();
	}
}
