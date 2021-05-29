package ctrmap.stdlib.io.util;

import ctrmap.stdlib.io.base.iface.IOStream;
import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import ctrmap.stdlib.io.structs.TemporaryOffset;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectWriter extends DataIOStream {
	
	private Map<TemporaryOffset, ObjectCallback> objects = new HashMap<>();
	
	public ObjectWriter(IOStream stream){
		super(stream);
	}
	
	public void writeObjectPointer(ObjectCallback callback) throws IOException {
		objects.put(new TemporaryOffset(this), callback);
	}
	
	public void flushObjects() throws IOException{
		while (!objects.isEmpty()){
			Map<TemporaryOffset, ObjectCallback> map = new HashMap<>(objects);
			
			for (Map.Entry<TemporaryOffset, ObjectCallback> e : map.entrySet()){
				e.getKey().setHere();
				e.getValue().write(this);
			}
			
			for (TemporaryOffset key : map.keySet()){
				objects.remove(key);
			}
		}
	}

	public static abstract class ObjectCallback<T> {		
		private final T obj;
	
		public ObjectCallback(T obj){
			this.obj = obj;
		}
		
		public void write(ObjectWriter writer) throws IOException{
			write(writer, obj);
		}
		
		public abstract void write(ObjectWriter writer, T obj) throws IOException;
	}
	
	public static class PrimitiveListObjectCallback extends ObjectCallback<List> {

		private final Type type;
		
		public PrimitiveListObjectCallback(List obj, Type destType) {
			super(obj);
			this.type = destType;
		}

		@Override
		public void write(ObjectWriter writer, List obj) throws IOException {
			for (Object elem : obj){
				Number n = (Number)elem;
				
				switch (type){
					case BYTE:
						writer.writeByte(n.byteValue());
						break;
					case INT:
						writer.writeInt(n.intValue());
						break;
					case SHORT:
						writer.writeShort(n.shortValue());
						break;
				}
			}
		}
		
		public static enum Type {
			INT,
			SHORT,
			BYTE
		}
	}
}
