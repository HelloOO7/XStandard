package ctrmap.stdlib.util;

import java.util.ArrayList;
import java.util.List;

public class ArraysEx {

	public static <T> T[] asArray(T... elems) {
		return elems;
	}
	
	public static int[] asArrayI(List<Integer> intList) {
		int[] arr = new int[intList.size()];
		for (int i = 0; i < arr.length; i++){
			arr[i] = intList.get(i);
		}
		return arr;
	}
	
	public static float[] asArrayF(List<Float> floatList) {
		float[] arr = new float[floatList.size()];
		for (int i = 0; i < arr.length; i++){
			arr[i] = floatList.get(i);
		}
		return arr;
	}
	
	public static short[] asArrayS(List<Short> shortList) {
		short[] arr = new short[shortList.size()];
		for (int i = 0; i < arr.length; i++){
			arr[i] = shortList.get(i);
		}
		return arr;
	}

	public static <T> List<T> asList(T... obj) {
		List<T> r = new ArrayList<>();
		for (T t : obj) {
			r.add(t);
		}
		return r;
	}

	public static <T> void addIfNotNullOrContains(List<T> list, T elem) {
		if (elem != null && !list.contains(elem)) {
			list.add(elem);
		}
	}

	public static <T> void addAllIfNotNullOrContains(List<T> list, List<T> toAdd) {
		for (T e : toAdd) {
			addIfNotNullOrContains(list, e);
		}
	}

	public static String toString(List list) {
		return toString(list.toArray(new Object[list.size()]));
	}

	public static String toString(Object... list) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.length; i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append(list[i].toString());
		}
		return sb.toString();
	}
}
