package xstandard.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A key-value dictionary that uses the equals(Object) method for matching keys. Slow.
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class EqualsMap<K, V> {

	private List<K> keys = new ArrayList<>();
	private List<V> values = new ArrayList<>();

	public boolean containsKey(K key) {
		return keys.contains(key);
	}

	public void put(K key, V value) {
		keys.add(key);
		values.add(value);
	}

	public V get(K key) {
		if (!containsKey(key)) {
			return null;
		}
		return values.get(keys.indexOf(key));
	}
	
	public void remove(K key){
		int idx = keys.indexOf(key);
		if (idx != -1){
			keys.remove(idx);
			values.remove(idx);
		}
	}
	
	public boolean isEmpty(){
		return keys.isEmpty();
	}
	
	public Set<Map.Entry> entrySet(){
		Set<Map.Entry> set = new HashSet<>();
		for (K key : keys){
			set.add(new AbstractMap.SimpleEntry(key, values.get(keys.indexOf(key))));
		}
		return set;
	}
}
