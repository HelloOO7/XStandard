package ctrmap.stdlib.io.util;

import ctrmap.stdlib.io.base.iface.IOStream;
import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import ctrmap.stdlib.io.structs.TemporaryOffset;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An output stream wrapper to facilitate writing objects using references.
 */
public class ObjectWriter extends DataIOStream {
	
	private Map<TemporaryOffset, ObjectCallback> objects = new HashMap<>();
	
	/**
	 * Creates an ObjectWriter wrapped around an IOStream.
	 * @param stream The IOStream to wrap.
	 */
	public ObjectWriter(IOStream stream){
		super(stream);
	}
	
	/**
	 * Adds an object reference to this stream.
	 * @param callback An ObjectCallback to call when the reference's value is written.
	 * @throws IOException 
	 */
	public void writeObjectPointer(ObjectCallback callback) throws IOException {
		objects.put(new TemporaryOffset(this), callback);
	}
	
	/**
	 * Set all object references and invoke their callbacks, writing the object data image starting at the current position.
	 * @throws IOException 
	 */
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

	/**
	 * Callback to customize writing object data to references.
	 * @param <T> Type of the object that is written.
	 */
	public static abstract class ObjectCallback<T> {		
		private final T obj;
	
		/**
		 * Creates an ObjectCallback for an object.
		 * @param obj An object.
		 */
		public ObjectCallback(T obj){
			this.obj = obj;
		}
		
		/**
		 * Writes the object to the ObjectWriter's stream using the customized method of this callback.
		 * @param writer An ObjectWriter
		 * @throws IOException 
		 */
		public void write(ObjectWriter writer) throws IOException{
			write(writer, obj);
		}
		
		protected abstract void write(ObjectWriter writer, T obj) throws IOException;
	}
	
	/**
	 * A default ObjectCallback for lists of boxed primitive types.
	 */
	public static class PrimitiveListObjectCallback extends ObjectCallback<List> {

		private final Type type;
		
		public PrimitiveListObjectCallback(List obj, Type destType) {
			super(obj);
			this.type = destType;
		}

		@Override
		protected void write(ObjectWriter writer, List obj) throws IOException {
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
