package ctrmap.stdlib.io.util.serialization;

import ctrmap.stdlib.io.base.IOStream;
import java.io.DataInput;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class DeserializerState {
	public DataInput in;
	public SerializerSettings settings;
	public Map<String, Object> globals = new HashMap<>();
	public Stack<Object> fieldStack = new Stack<>();
	
	private Map<Object, Map<String, Type>> currentGenericLookup = new HashMap<>();

	public DeserializerState(DataInput in, SerializerSettings settings){
		this.in = in;
		this.settings = settings;
	}
	
	public IOStream getIO(){
		if (in instanceof IOStream){
			return (IOStream)in;
		}
		throw new UnsupportedOperationException("Not an IO stream.");
	}
	
	public Map<String, Type> getGenericLookupForObj(Object o){
		Map<String, Type> m = currentGenericLookup.get(o);
		if (m != null){
			return m;
		}
		else {
			m = new HashMap<>();
			currentGenericLookup.put(o, m);
			return m;
		}
	}
	
	public Object getGlobal(String name){
		return globals.get(name);
	}
}
