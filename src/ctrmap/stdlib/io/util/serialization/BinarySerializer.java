package ctrmap.stdlib.io.util.serialization;

import ctrmap.stdlib.io.LittleEndianDataOutputStream;
import ctrmap.stdlib.io.util.StringUtils;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BinarySerializer {

	public static byte[] serializeObject(Object obj) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(baos);
			serializeObject(dos, obj);
			dos.close();
			return baos.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(BinarySerializer.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static void serializeObject(DataOutput out, Object obj) throws IOException {
		try {
			writeObject(obj, null, out);
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException ex) {
			Logger.getLogger(BinarySerializer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void writeObject(Object obj, Field parentField, DataOutput out) throws IOException, InstantiationException, IllegalAccessException {
		Class type = getBaseType(obj.getClass());

		if (type.isPrimitive()) {
			if (hasFieldAnnotation(UnsignedType.class, parentField)) {
				int size = ((UnsignedType) parentField.getAnnotation(UnsignedType.class)).sizeOf();
				int value = (int) obj;
				switch (size) {
					case 1:
						out.write(value);
						break;
					case 2:
						out.writeShort(value);
						break;
				}
			} else if (type == Integer.TYPE) {
				out.writeInt((int) obj);
			} else if (type == Float.TYPE) {
				out.writeFloat((float) obj);
			} else if (type == Long.TYPE) {
				out.writeLong((long) obj);
			} else if (type == Double.TYPE) {
				out.writeDouble((double) obj);
			} else if (type == Short.TYPE) {
				out.writeShort((short) obj);
			} else if (type == Byte.TYPE) {
				out.writeByte((byte) obj);
			} else if (type == Boolean.TYPE) {
				int num = ((boolean)obj) ? 1 : 0;
				int size = 1;
				if (hasFieldAnnotation(CustomSize.class, parentField)){
					size = ((CustomSize)parentField.getAnnotation(CustomSize.class)).sizeOf();
				}
				writeNumber(num, size, out);
			}
		} else if (type == String.class) {
			if (hasFieldAnnotation(FixedLength.class, parentField)) {
				int len = ((FixedLength) parentField.getAnnotation(FixedLength.class)).fixedLength();
				String str = (String) obj;
				StringUtils.writeStringUnterminated(out, str.substring(0, Math.min(str.length(), len)));
				if (len > str.length()) {
					out.write(new byte[len - str.length()]);
				}
			} else {
				StringUtils.writeString(out, (String) obj);
			}
		} else if (type.isEnum()) {
			int size = 1;
			if (hasFieldAnnotation(CustomSize.class, parentField)) {
				size = ((CustomSize) parentField.getAnnotation(CustomSize.class)).sizeOf();
			}
			int ordinal = ((Enum) obj).ordinal();
			writeNumber(ordinal, size, out);
		} else if (type.isArray()) {
			int len = Array.getLength(obj);
			if (!hasFieldAnnotation(FixedLength.class, parentField)) {
				int lenSize = 4;
				if (hasFieldAnnotation(CustomLengthSize.class, parentField)) {
					lenSize = ((CustomLengthSize) parentField.getAnnotation(CustomLengthSize.class)).lengthSize();
				}
				writeNumber(len, lenSize, out);
			}
			for (int i = 0; i < len; i++) {
				writeObject(Array.get(obj, i), null, out);
			}
		} else if (Collection.class
				.isAssignableFrom(type)) {
			Collection collection = (Collection) obj;

			out.writeInt(collection.size());
			for (Object elem : collection) {
				writeObject(elem, null, out);
			}
		} else if (Map.class
				.isAssignableFrom(type)) {
			Map map = (Map) obj;

			out.writeInt(map.size());
			for (Object eo
					: map.entrySet()) {
				Map.Entry e = (Map.Entry) eo;
				writeObject(e.getKey(), null, out);
				writeObject(e.getValue(), null, out);
			}
		} else {
			Field[] fields = type.getFields();
			for (Field f : fields) {
				if (!Modifier.isStatic(f.getModifiers())) {
					if (!f.isAnnotationPresent(SerializerIgnore.class)) {
						writeObject(f.get(obj), f, out);
					}
				}
			}
		}
	}
	
	private static void writeNumber(int num, int size, DataOutput out) throws IOException {
		switch (size){
			case 1:
				out.write(num);
				break;
			case 2:
				out.writeShort(num);
				break;
			case 4:
				out.writeInt(num);
				break;
			case 8:
				out.writeInt(num);
				break;
			default:
				throw new IllegalArgumentException("Invalid data type size: " + size);
		}
	}

	public static Class getBaseType(Class clazz) {
		if (clazz == Integer.class) {
			return Integer.TYPE;
		} else if (clazz == Float.class) {
			return Float.TYPE;
		} else if (clazz == Long.class) {
			return Long.TYPE;
		} else if (clazz == Double.class) {
			return Double.TYPE;
		} else if (clazz == Short.class) {
			return Short.TYPE;
		} else if (clazz == Byte.class) {
			return Byte.TYPE;
		} else if (clazz == Boolean.class) {
			return Boolean.TYPE;
		}
		return clazz;
	}

	public static boolean hasFieldAnnotation(Class<? extends Annotation> annot, Field f) {
		return f != null && f.isAnnotationPresent(annot);
	}
}
