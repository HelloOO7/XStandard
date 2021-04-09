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

	private static final boolean REFLHASH_DEBUG = false;
	
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
		Class cls = o.getClass();
		boolean isPrimitiveOrEnum = cls.isPrimitive() || cls.isEnum();
		if (!isPrimitiveOrEnum) {
			if (!cache.contains(o)) {
				cache.add(o);
			} else {
				return 0;
			}
		}
		if (isPrimitiveOrEnum || o instanceof Number) {
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
				if (Modifier.isStatic(mods)) {
					continue;
				}
				if (f.isAnnotationPresent(ReflectionHashIgnore.class)) {
					continue;
				}
				if (f.getType().isAssignableFrom(ReflectionHash.class)) {
					continue;
				}
				
				if (f.getName().equals("modCount")){
					//HACK
					//Java AbstractList store the number of modifications in modCount. In theory, the list can remain unchanged even after modifications.
					//Disabling transient field serialization is not an option since elementData is transient as well for some stupid reason
					
					continue;
				}
				if (REFLHASH_DEBUG){
					System.out.println("Hashing field " + f);
				}
				
				f.setAccessible(true);
				hash = 37 * hash + hashObj(f.get(o), cache);
				
				if (REFLHASH_DEBUG){
					System.out.println(" -> " + hash);
				}
			}
			return hash;
		}
	}

	private static List<Field> getAllFields(Class cls) {
		List<Field> l = ArraysEx.asList(cls.getDeclaredFields());
		Class superc = cls.getSuperclass();
		if (superc != null) {
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
	
	public void forceSetChangedFlag(){
		changedFlag = true;
		hash = 0; //if the data was to be recalculated, the changed flag will remain set
	}

	public boolean getChangeFlagRecalcIfNeeded() {
		if (!changedFlag) {
			return recalculate();
		}
		return true;
	}

	public void resetChangedFlag() {
		changedFlag = false;
	}
}
