package ctrmap.stdlib.io.util.serialization;

import ctrmap.stdlib.io.LittleEndianDataInputStream;
import ctrmap.stdlib.io.iface.PositionedDataInput;
import ctrmap.stdlib.io.iface.SeekableDataInput;
import ctrmap.stdlib.io.util.StringUtils;
import java.io.DataInput;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ctrmap.stdlib.io.util.serialization.BinarySerializer.hasFieldAnnotation;
import static ctrmap.stdlib.io.util.serialization.BinarySerializer.getBaseType;
import static ctrmap.stdlib.io.util.serialization.BinarySerializer.writeObject;
import ctrmap.stdlib.io.util.serialization.typechoice.TypeChoice;
import ctrmap.stdlib.io.util.serialization.typechoice.TypeChoiceField;
import ctrmap.stdlib.io.util.serialization.typechoice.TypeChoices;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class BinaryDeserializer {

	public static void deserializeToObject(byte[] b, Object obj) {
		try {
			LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new ByteArrayInputStream(b));
			deserializeToObject(dis, obj);
			dis.close();
		} catch (IOException ex) {
			Logger.getLogger(BinaryDeserializer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void deserializeToObject(DataInput in, Object obj) throws IOException {
		deserializeToObject(in, obj, new SerializerSettings());
	}

	public static void deserializeToObject(DataInput in, Object obj, SerializerSettings settings) throws IOException {
		deserializeToObject(obj, new DeserializerState(in, settings));
	}

	public static void deserializeToObject(Object obj, DeserializerState state) throws IOException {
		try {
			Class clazz = obj.getClass();
			Field[] fields = clazz.getFields();
			for (Field f : fields) {
				if (!Modifier.isStatic(f.getModifiers())) {
					if (!f.isAnnotationPresent(SerializerIgnore.class)) {
						f.set(obj, readObject(f.getType(), obj, f, state));
					}
				}
			}
			if (obj instanceof DeserializerPostProcess){
				((DeserializerPostProcess)obj).deserialize(state);
			}
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException | NoSuchFieldException ex) {
			Logger.getLogger(BinaryDeserializer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static Object readObject(Type type, Object parentObject, Field field, DeserializerState state) throws IOException, InstantiationException, IllegalAccessException, NoSuchFieldException {
		//System.out.println("reading " + field);
		Class clazz;

		if (hasFieldAnnotation(TypeChoiceField.class, field)) {
			TypeChoice[] tcs = ((TypeChoices)field.getAnnotation(TypeChoices.class)).value();
			Field keyField = parentObject.getClass().getField(((TypeChoiceField)field.getAnnotation(TypeChoiceField.class)).fieldName());
			int tcKey;
			if (keyField.getType().isEnum()){
				tcKey = ((Enum)keyField.get(parentObject)).ordinal();
			}
			else {
				tcKey = keyField.getInt(parentObject);
			}
			System.out.println("tckey " + tcKey);
			for (TypeChoice tc : tcs){
				if (tc.key() == tcKey){
					type = tc.value();
					System.out.println("got tc " + type);
					break;
				}
			}
		}
		if (type instanceof ParameterizedType) {
			clazz = (Class) ((ParameterizedType) type).getRawType();
		} else if (type instanceof Class) {
			clazz = (Class) type;
		} else if (type instanceof TypeVariable) {
			clazz = (Class) state.getGenericLookupForObj(state.fieldStack.peek()).get(type.getTypeName());
		} else {
			clazz = null;
		}

		clazz = getBaseType(clazz);
		DataInput in = state.in;
		SerializerSettings settings = state.settings;

		boolean isObject = !(clazz.isPrimitive() || clazz.isArray() || clazz.isEnum());
		int beforePtr = -1;

		Object ret = null;

		if (isObject && settings.pointerType != SerializerSettings.PointerType.INLINE && !hasFieldAnnotation(Inline.class, field) && !clazz.isAnnotationPresent(InlineClass.class)) {
			if (in instanceof PositionedDataInput) {
				beforePtr = ((PositionedDataInput) in).getPosition();

				int currentPtr = in.readInt();
				if (currentPtr != 0) {
					if (in instanceof SeekableDataInput) {
						int toSeek = -1;
						switch (settings.pointerType) {
							case ABSOLUTE:
								toSeek = currentPtr;
								break;
							case SELF_RELATIVE:
								toSeek = currentPtr + beforePtr;
								break;
						}
						((SeekableDataInput) in).seek(toSeek);
					} else {
						throw new UnsupportedOperationException("Can not have a non-inline object on a non-seekable stream.");
					}
				} else {
					return null;
				}
			} else {
				throw new UnsupportedOperationException("Can not have a non-inline object on a non-positioned stream.");
			}
		}

		Object fieldKey = field;

		if (field == null) {
			fieldKey = new Object();
		}

		if (!state.fieldStack.isEmpty()) {
			Map<String, Type> parentGenericLookup = state.getGenericLookupForObj(state.fieldStack.peek());
			Map<String, Type> genericLookup = state.getGenericLookupForObj(fieldKey);

			Type parentType = null;
			Type[] parentGenericTypes = new Type[0];
			String[] parentGenericsAssignments = new String[0];
			//f (field != null) {
			parentType = type;
			if (parentType instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) parentType;
				parentGenericTypes = pt.getActualTypeArguments();
				parentGenericsAssignments = new String[parentGenericTypes.length];
				Type[] ga = clazz.getTypeParameters();
				for (int i = 0; i < ga.length; i++) {
					parentGenericsAssignments[i] = ga[i].getTypeName();
					if (parentGenericTypes[i] instanceof TypeVariable) {
						parentGenericTypes[i] = parentGenericLookup.get(parentGenericsAssignments[i]);
					} else if (parentGenericTypes[i] instanceof ParameterizedType) {
						Type[] ga2 = ((Class) ((ParameterizedType) parentGenericTypes[i]).getRawType()).getTypeParameters();
						Type[] pgt2 = ((ParameterizedType) parentGenericTypes[i]).getActualTypeArguments();
						for (int j = 0; j < pgt2.length; j++) {
							if (pgt2[j] instanceof TypeVariable) {
								pgt2[j] = parentGenericLookup.get(ga2[j].getTypeName());
							}
							genericLookup.put(ga2[j].getTypeName(), (Class) pgt2[j]);
						}

						parentGenericTypes[i] = ((ParameterizedType) parentGenericTypes[i]).getRawType();
					}
					genericLookup.put(parentGenericsAssignments[i], (Class) parentGenericTypes[i]);
				}
			} else if (parentType instanceof TypeVariable) {
				clazz = (Class) parentGenericLookup.get(parentType.getTypeName());
			}
			//}
		}

		state.fieldStack.push(fieldKey);

		if (field != null) {
			if (type instanceof TypeVariable) {
				//clazz = state.currentGenericLookup.get(parentField.get)
			}
			if (type instanceof ParameterizedType) {

			}
		}

		if (clazz.isPrimitive()) {
			if (hasFieldAnnotation(UnsignedType.class, field)) {
				int size = ((UnsignedType) field.getAnnotation(UnsignedType.class)).sizeOf();
				int value = readNumber(size, in);
				ret = value;
			} else if (clazz == Integer.TYPE) {
				ret = in.readInt();
			} else if (clazz == Float.TYPE) {
				ret = in.readFloat();
			} else if (clazz == Long.TYPE) {
				ret = in.readLong();
			} else if (clazz == Double.TYPE) {
				ret = in.readDouble();
			} else if (clazz == Short.TYPE) {
				ret = in.readShort();
			} else if (clazz == Byte.TYPE) {
				ret = in.readByte();
			} else if (clazz == Boolean.TYPE) {
				int sizeOf = 1;
				if (hasFieldAnnotation(CustomSize.class, field)) {
					sizeOf = ((CustomSize) field.getAnnotation(CustomSize.class)).sizeOf();
				}
				ret = readNumber(sizeOf, in) != 0;
			}
		} else if (clazz == String.class) {
			if (hasFieldAnnotation(FixedLength.class, field)) {
				ret = StringUtils.readStringWithSize(in, ((FixedLength) field.getAnnotation(FixedLength.class)).fixedLength());
			} else {
				ret = StringUtils.readString(in);
			}
		} else if (clazz.isEnum()) {
			int size = 1;
			if (hasFieldAnnotation(CustomSize.class, field)) {
				size = ((CustomSize) field.getAnnotation(CustomSize.class)).sizeOf();
			}
			int ordinal = readNumber(size, in);
			Object[] enumValues = clazz.getEnumConstants();
			for (Object ev : enumValues) {
				if (((Enum) ev).ordinal() == ordinal) {
					ret = ev;
					break;
				}
			}
		} else if (Collection.class.isAssignableFrom(clazz)) {
			if (clazz == List.class) {
				clazz = ArrayList.class;
			}
			Collection collection = (Collection) clazz.newInstance();
			int size;
			if (hasFieldAnnotation(CustomLengthField.class, field)) {
				Class parentClass = parentObject.getClass();
				size = parentClass.getField(((CustomLengthField) field.getAnnotation(CustomLengthField.class)).fieldName()).getInt(parentObject);
			} else if (hasFieldAnnotation(CustomLengthSize.class, field)) {
				size = readNumber(((CustomLengthSize) field.getAnnotation(CustomLengthSize.class)).lengthSize(), in);
			} else {
				size = in.readInt();
			}
			//parent field will not be null since that could only happen inside arrays and Java does not support generic array creation
			Type tp = getTypeParameter(field, 0, state.getGenericLookupForObj(state.fieldStack.peek()));
			
			for (int i = 0; i < size; i++) {
				collection.add(readObject(tp, collection, null, state));
			}
			ret = collection;
		} else if (Map.class.isAssignableFrom(clazz)) {
			Map map = (Map) clazz.newInstance();
			int size = in.readInt();
			Type genericType = field.getGenericType();
			if (genericType instanceof ParameterizedType) {
				for (int i = 0; i < size; i++) {
					map.put(
							readObject((Class) ((ParameterizedType) genericType).getActualTypeArguments()[0], map, null, state),
							readObject((Class) ((ParameterizedType) genericType).getActualTypeArguments()[1], map, null, state)
					);
				}
			} else {
				throw new UnsupportedOperationException("Deserializable lists have to be parameterized.");
			}
			ret = map;
		} else if (clazz.isArray()) {
			int arraySize = 0;
			if (hasFieldAnnotation(FixedLength.class, field)) {
				arraySize = ((FixedLength) field.getAnnotation(FixedLength.class)).fixedLength();
			} else {
				int lengthSize = 4;
				if (hasFieldAnnotation(CustomLengthSize.class, field)) {
					lengthSize = ((CustomLengthSize) field.getAnnotation(CustomLengthSize.class)).lengthSize();
				}
				arraySize = readNumber(lengthSize, in);
			}
			Class compType = clazz.getComponentType();
			Object arr = Array.newInstance(compType, arraySize);
			for (int i = 0; i < arraySize; i++) {
				Array.set(arr, i, readObject(compType, arr, field, state));
			}
			ret = arr;
		} else {
			Field[] fields = clazz.getFields();
			Object obj = clazz.newInstance();
			for (Field f : fields) {
				if (!Modifier.isStatic(f.getModifiers())) {
					if (!f.isAnnotationPresent(SerializerIgnore.class)) {
						f.set(obj, readObject(f.getGenericType(), obj, f, state));
					}
				}
			}

			//clear global values
			for (Field f : fields) {
				if (hasFieldAnnotation(PutGlobal.class, f)) {
					state.globals.remove(f.getName());
				}
			}

			ret = obj;
		}
		
		if (ret instanceof DeserializerPostProcess){
			((DeserializerPostProcess)ret).deserialize(state);
		}

		if (isObject && beforePtr != -1) {
			((SeekableDataInput) in).seek(beforePtr + 4);
		}

		if (hasFieldAnnotation(PutGlobal.class, field)) {
			state.globals.put(field.getName(), ret);
		}

		state.fieldStack.pop();

		return ret;
	}

	private static Type getTypeParameter(Field field, int paramNum, Map<String, Type> parentGenericLookup) {
		Type genericType = field.getGenericType();
		Type tpType = ((ParameterizedType) genericType).getActualTypeArguments()[paramNum];
		if (tpType instanceof Class) {
			return (Class) tpType;
		} else if (tpType instanceof TypeVariable) {
			TypeVariable tv = (TypeVariable) tpType;
			return parentGenericLookup.get(tv.getTypeName());
		} else if (tpType instanceof ParameterizedType) {
			return ((ParameterizedType) tpType);
		}
		return null;
	}

	private static int readNumber(int sizeOf, DataInput in) throws IOException {
		switch (sizeOf) {
			case 4:
				return in.readInt();
			case 2:
				return in.readUnsignedShort();
			case 1:
				return in.readUnsignedByte();
		}
		return 0;
	}
}
