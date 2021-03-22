package ctrmap.stdlib.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReflectionHash {

	private int hash = 0;
	private Object object;
	private boolean changedFlag = false;

	public ReflectionHash(Object o) {
		object = o;
		recalculate();
		changedFlag = false;
	}

	private static int hashObj(Object o, List<Object> cache) throws IllegalArgumentException, IllegalAccessException {
		if (o == null) {
			return 0;
		}
		if (!cache.contains(o)) {
			cache.add(o);
		} else {
			return 0;
		}
		Class cls = o.getClass();
		if (cls.isPrimitive() || cls.isEnum()) {
			return Objects.hashCode(o);
		} else if (cls.isArray()) {
			int len = Array.getLength(o);
			int hash = 7;
			for (int i = 0; i < len; i++) {
				hash = 37 * hash + hashObj(Array.get(o, i), cache);
			}
			return hash;
		} else {
			int hash = 7;
			for (Field f : getAllFields(cls)) {
				int mods = f.getModifiers();
				if (Modifier.isStatic(mods)){
					continue;
				}
				if (f.isAnnotationPresent(ReflectionHashIgnore.class)){
					continue;
				}
				if (f.getType().isAssignableFrom(ReflectionHash.class)){
					continue;
				}
				boolean accessible = f.isAccessible();
				if (!accessible) {
					f.setAccessible(true);
				}
				hash = 37 * hash + hashObj(f.get(o), cache);
				f.setAccessible(accessible);
			}
			return hash;
		}
	}
	
	private static List<Field> getAllFields(Class cls){
		List<Field> l = ArraysEx.asList(cls.getDeclaredFields());
		Class superc = cls.getSuperclass();
		if (superc != null){
			l.addAll(getAllFields(superc));
		}
		return l;
	}

	public boolean recalculate() {
		try {
			int newHash = hashObj(object, new ArrayList<>());
			if (newHash != hash) {
				hash = newHash;
				changedFlag = true;
				return true;
			}
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			Logger.getLogger(ReflectionHash.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}

	public boolean hasChanged() {
		return changedFlag;
	}

	public void resetChangedFlag() {
		changedFlag = false;
	}
}
