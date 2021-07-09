package ctrmap.stdlib.io.serialization;

import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import ctrmap.stdlib.io.base.iface.IOStream;
import ctrmap.stdlib.io.util.StringIO;
import ctrmap.stdlib.io.serialization.annotations.typechoice.TypeChoiceInt;
import ctrmap.stdlib.io.serialization.annotations.typechoice.TypeChoiceStr;
import ctrmap.stdlib.io.serialization.annotations.typechoice.TypeChoicesInt;
import ctrmap.stdlib.io.serialization.annotations.typechoice.TypeChoicesStr;

import java.io.IOException;
import java.lang.reflect.*;
import static ctrmap.stdlib.io.IOCommon.*;
import ctrmap.stdlib.io.base.impl.access.MemoryStream;
import ctrmap.stdlib.io.serialization.annotations.ArrayLengthSize;
import ctrmap.stdlib.io.serialization.annotations.ArraySize;
import ctrmap.stdlib.io.serialization.annotations.ByteOrderMark;
import ctrmap.stdlib.io.serialization.annotations.Define;
import ctrmap.stdlib.io.serialization.annotations.DefinedArraySize;
import ctrmap.stdlib.io.serialization.annotations.IfVersion;
import ctrmap.stdlib.io.serialization.annotations.Ignore;
import ctrmap.stdlib.io.serialization.annotations.Inline;
import ctrmap.stdlib.io.serialization.annotations.LengthPos;
import ctrmap.stdlib.io.serialization.annotations.MagicStr;
import ctrmap.stdlib.io.serialization.annotations.MagicStrLE;
import ctrmap.stdlib.io.serialization.annotations.ObjSize;
import ctrmap.stdlib.io.serialization.annotations.PointerBase;
import ctrmap.stdlib.io.serialization.annotations.PointerSize;
import ctrmap.stdlib.io.serialization.annotations.Size;
import ctrmap.stdlib.io.serialization.annotations.Version;
import java.nio.ByteOrder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BinaryDeserializer extends BinarySerialization {

	private TypeParameterStack typeParameterStack = new TypeParameterStack();

	private Stack<Integer> pointerBaseStack = new Stack<>();

	private Map<String, Object> definitions = new HashMap<>();

	public BinaryDeserializer(IOStream baseStream, ByteOrder bo, ReferenceType referenceType) {
		this(baseStream, bo, referenceType, DecimalType.FLOATING_POINT);
	}

	public BinaryDeserializer(IOStream baseStream, ByteOrder bo, ReferenceType referenceType, DecimalType decimalType) {
		super(baseStream, referenceType, decimalType);
		pointerBaseStack.push(0);
	}

	public void loadStreamOntoMemory() {
		if (!(baseStream.getBaseStream() instanceof MemoryStream)) {
			try {
				DataIOStream newStream = new DataIOStream(baseStream.toByteArray());
				baseStream.close();
				baseStream = newStream;
			} catch (IOException ex) {
				Logger.getLogger(BinaryDeserializer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public <T> T deserialize(Class<T> cls) {
		try {
			T obj = (T) readValue(cls, null);
			return obj;
		} catch (InstantiationException | IllegalAccessException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <T> T deserializeDefault(Class<T> cls, IOStream io) {
		BinaryDeserializer deserializer = new BinaryDeserializer(io, ByteOrder.LITTLE_ENDIAN, ReferenceType.ABSOLUTE_POINTER);
		return deserializer.deserialize(cls);
	}

	public static void deserializeToObject(byte[] bytes, Object obj) {
		try {
			try (DataIOStream io = new DataIOStream(bytes)) {
				deserializeToObject(io, obj);
			}
		} catch (IOException ex) {

		}
	}

	public static void deserializeToObject(DataIOStream io, Object obj) {
		deserializeToObject(io, obj, ReferenceType.NONE);
	}

	public static void deserializeToObject(DataIOStream io, Object obj, ReferenceType refType) {
		try {
			BinaryDeserializer deserializer = new BinaryDeserializer(io, ByteOrder.LITTLE_ENDIAN, refType);
			deserializer.readObjectFields(obj, null, 0);
		} catch (InstantiationException | IllegalAccessException | IOException ex) {
			Logger.getLogger(BinaryDeserializer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void deserializeToObject(Object obj) {
		try {
			readObjectFields(obj, obj.getClass(), baseStream.getPosition());
		} catch (IOException | InstantiationException | IllegalAccessException ex) {
			Logger.getLogger(BinaryDeserializer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private boolean isIfVersionPass(IfVersion ifv) {
		int rhs = ifv.rhs();
		switch (ifv.op()) {
			case EQUAL:
				return fileVersion == rhs;
			case GEQUAL:
				return fileVersion >= rhs;
			case GREATER:
				return fileVersion > rhs;
			case LEQUAL:
				return fileVersion <= rhs;
			case LESS:
				return fileVersion < rhs;
			case NOTEQUAL:
				return fileVersion != rhs;
		}
		return false;
	}

	private void readObjectFields(Object obj, Class cls, int objStartAddress) throws InstantiationException, IllegalAccessException, IOException {
		List<Field> objSizeFields = new ArrayList<>();
		List<String> localDefinitions = new ArrayList<>();

		if (cls == null) {
			cls = obj.getClass();
		}
		for (Field fld : getSortedFields(cls)) {
			if (fld.isAnnotationPresent(IfVersion.class)) {
				if (!isIfVersionPass(fld.getAnnotation(IfVersion.class))) {
					continue;
				}
			}
			if (!fld.isAnnotationPresent(Ignore.class)) {
				Object value = readValue(fld.getGenericType(), fld);
				fld.set(obj, value);
				if (fld.isAnnotationPresent(ObjSize.class)) {
					objSizeFields.add(fld);
				}
				if (fld.isAnnotationPresent(Define.class)) {
					String defineName = fld.getAnnotation(Define.class).value();

					definitions.put(defineName, value);
					localDefinitions.add(defineName);
				}
			}
		}

		if (obj instanceof ICustomSerialization) {
			((ICustomSerialization) obj).deserialize(this);
		}

		int expectedObjSize = baseStream.getPosition() - objStartAddress;
		for (Field objSizeFld : objSizeFields) {
			Object fldValue = objSizeFld.get(obj);
			if (fldValue instanceof Number) {
				int objSize = ((Number) fldValue).intValue();
				if (objSize != expectedObjSize) {
					throw new RuntimeException(String.format("Object size does 0x%08X not match actual object size (0x%08X)! Object start: 0x%08X, Current stream position: 0x%08X.", objSize, expectedObjSize, objStartAddress, baseStream.getPosition()));
				}
			} else {
				throw new RuntimeException("ObjSize field is not a numeric primitive!");
			}
		}

		for (String def : localDefinitions) {
			definitions.remove(def);
		}
	}

	public Object getDefinition(String name) {
		return definitions.get(name);
	}

	private void updatePointerBase() throws IOException {
		pointerBaseStack.push(baseStream.getPosition());
	}

	private void resetPointerBase() {
		pointerBaseStack.pop();
	}

	private Object readValue(Type type, Field field) throws InstantiationException, IllegalAccessException, IOException {
		return readValue(type, field, false);
	}

	private Object readValue(Type type, Field field, boolean isListElem) throws InstantiationException, IllegalAccessException, IOException {
		debugPrint("Reading " + field + " at " + Integer.toHexString(baseStream.getPosition()));
		if (!isListElem) {
			typeParameterStack.pushTPS();
			typeParameterStack.importFieldType(field);
		}

		type = typeParameterStack.resolveType(type);

		Class cls;

		if (type instanceof ParameterizedType) {
			cls = (Class) ((ParameterizedType) type).getRawType();
		} else if (type instanceof Class) {
			cls = (Class) type;
		} else {
			throw new IllegalArgumentException("Unsupported Type class: " + (type == null ? "null" : type.getClass().toString()));
		}

		cls = getUnboxedClass(cls);

		if (cls.isAnnotationPresent(PointerBase.class)) {
			updatePointerBase();
		}

		Object value = null;

		switch (FieldTypeGroup.getTypeGroup(cls)) {
			case PRIMITIVE:
				ByteOrder bo = baseStream.order();
				boolean isLE = bo == ByteOrder.LITTLE_ENDIAN;
				boolean isBOM = hasAnnotation(ByteOrderMark.class, field);

				if (isBOM) {
					debugPrint("Field " + field + " is BOM");
					if (isLE) {
						baseStream.order(ByteOrder.BIG_ENDIAN);
					}
				}
				value = readPrimitive(cls, field);

				if (isBOM) {
					if (!(value instanceof Number)) {
						throw new RuntimeException("A ByteOrderMark can not be a non-numeric primitive!");
					}
					ByteOrderMark bom = field.getAnnotation(ByteOrderMark.class);
					int numValue = ((Number) value).intValue();
					if (numValue == bom.ifBE()) {
						debugPrint("Setting Big Endian order.");
						baseStream.order(ByteOrder.BIG_ENDIAN);
					} else if (numValue == bom.ifLE()) {
						debugPrint("Setting Little Endian order.");
						baseStream.order(ByteOrder.LITTLE_ENDIAN);
					} else {
						throw new RuntimeException(String.format("Unrecognized ByteOrderMark: 0x%08X, expected 0x%08X for BE and 0x%08X for LE respectively.", numValue, bom.ifBE(), bom.ifLE()));
					}
				}
				if (hasAnnotation(Version.class, field)) {
					fileVersion = ((Number) value).intValue();
				}

				break;
			case ENUM:
				value = readEnum(cls, field);
				break;
			case ARRAY:
				value = readArray(cls, field);
				break;
			case OBJECT:
				value = readObject(cls, field, isListElem);
				break;
		}

		if (!isListElem) {
			typeParameterStack.popTPS();
		}

		if (cls.isAnnotationPresent(PointerBase.class)) {
			resetPointerBase();
		}

		return value;
	}

	public int getBasedPointer(int ptr) {
		return ptr + pointerBaseStack.peek();
	}

	private Object readPrimitive(Class cls, Field field) throws IOException {
		if (cls == Integer.TYPE) {
			return readSizedInt(field);
		} else if (cls == Short.TYPE) {
			return baseStream.readShort();
		} else if (cls == Byte.TYPE) {
			return baseStream.readByte();
		} else if (cls == Boolean.TYPE) {
			return readSizedInt(field, 1) == 1;
		} else if (cls == Float.TYPE) {
			switch (decimalType) {
				case FLOATING_POINT:
					return baseStream.readFloat();
				case FIXED_POINT_NNFX:
					int intValue = readSizedInt(field);
					return intValue / 4096f;
			}
		} else if (cls == Double.TYPE) {
			return baseStream.readDouble();
		} else if (cls == Long.TYPE) {
			return baseStream.readLong();
		}
		throw new UnsupportedOperationException("Unsupported primitive: " + cls);
	}

	private Enum readEnum(Class cls, Field field) throws IOException {
		Object[] constants = cls.getEnumConstants();

		int defaultSize = constants.length <= 0x100 ? 1 : constants.length <= 0x10000 ? 2 : 4;

		int ordinal = readSizedInt(field, getIntSize(defaultSize, field, cls));

		if (ISerializableEnum.class.isAssignableFrom(cls)) {
			for (Object ev : constants) {
				if (((ISerializableEnum) ev).getOrdinal() == ordinal) {
					return (Enum) ev;
				}
			}
			return null;
		} else {
			if (ordinal < 0 || ordinal >= constants.length) {
				return null;
			}

			return (Enum) constants[ordinal];
		}
	}

	private Object readArray(Class cls, Field field) throws InstantiationException, IllegalAccessException, IOException {
		debugPrint("array " + field + " at " + Integer.toHexString(baseStream.getPosition()));
		int size = readArrayLength(field);

		Class componentType = cls.getComponentType();

		Object arr = Array.newInstance(componentType, size);
		debugPrint("array size " + size);
		for (int i = 0; i < size; i++) {
			Object value = readValue(componentType, field, true);
			Array.set(arr, i, value);
		}

		return arr;
	}

	public int readPointer() throws IOException {
		int posBeforePtr = baseStream.getPosition();
		int ptr = baseStream.readInt();

		if (ptr == 0) {
			return ptr;
		}

		if (refType == ReferenceType.SELF_RELATIVE_POINTER) {
			ptr += posBeforePtr;
		} else {
			ptr += pointerBaseStack.peek();
		}

		return ptr;
	}

	private int readPointer(Field field, int posBeforePtr, boolean isListElem, AnnotatedElement... ant) throws IOException {
		if ((field != null || isListElem) && !hasAnnotation(Inline.class, ant) && refType != ReferenceType.NONE) {
			debugPrint("Object " + field + " is noninline !!");
			int ptr = 0;

			if (hasAnnotation(PointerSize.class, field)) {
				ptr += readSizedInt(field.getAnnotation(PointerSize.class).value());
			} else {
				ptr += baseStream.readInt();
			}

			if (ptr == 0) {
				return 0;
			}

			if (refType == ReferenceType.SELF_RELATIVE_POINTER) {
				ptr += posBeforePtr;
			} else {
				ptr += pointerBaseStack.peek();
			}
			debugPrint("Final ptr " + Integer.toHexString(ptr));

			return ptr;
		}
		return -1;
	}

	private void seekPointer(int ptr) throws IOException {
		if (ptr != -1) {
			baseStream.seek(ptr);
		}
	}

	private static boolean getIsClassNeedsSize(Class cls, AnnotatedElement... ant) {
		boolean obj_NeedsSize = false;
		if (Collection.class.isAssignableFrom(cls)) {
			obj_NeedsSize = true;
		} else if (cls == String.class && hasAnnotation(LengthPos.class, ant)) {
			obj_NeedsSize = true;
		}
		return obj_NeedsSize;
	}

	private Object readObject(Class cls, Field field, boolean isListElem) throws InstantiationException, IllegalAccessException, IOException {
		AnnotatedElement[] ant = new AnnotatedElement[]{field, cls};

		int posBeforePtr = baseStream.getPosition();
		boolean obj_NeedsSize = getIsClassNeedsSize(cls, ant);

		int posAfterPtr = -1;
		int obj_Size = -1;

		int ptr = -1;

		if (obj_NeedsSize) {
			LengthPos.LengthPosType lengthPos = LengthPos.LengthPosType.BEFORE_PTR;
			if (hasAnnotation(LengthPos.class, ant)) {
				lengthPos = getAnnotation(LengthPos.class, ant).value();
			}

			if (lengthPos == LengthPos.LengthPosType.BEFORE_PTR) {
				obj_Size = readArrayLength(field);
				ptr = readPointer(field, posBeforePtr, isListElem, ant);
				posAfterPtr = baseStream.getPosition();
			} else {
				ptr = readPointer(field, posBeforePtr, isListElem, ant);
				obj_Size = readArrayLength(field);
				posAfterPtr = baseStream.getPosition();
			}
		} else {
			ptr = readPointer(field, posBeforePtr, isListElem, ant);
			posAfterPtr = baseStream.getPosition();
		}
		seekPointer(ptr);
		if (ptr == 0) {
			return null;
		}

		int posBeforeObj = baseStream.getPosition();

		if (hasAnnotation(TypeChoicesStr.class, ant) || hasAnnotation(TypeChoicesInt.class, ant)) {
			boolean found = false;
			int size = getIntSize(Integer.BYTES, ant);

			int intVal = readSizedInt(size);
			baseStream.seek(baseStream.getPosition() - size);
			String strVal = StringIO.readPaddedString(baseStream, size);
			debugPrint("Typechoice str " + strVal);

			if (hasAnnotation(MagicStrLE.class, ant)) {
				strVal = new StringBuilder(strVal).reverse().toString();
			}

			if (hasAnnotation(TypeChoicesInt.class, ant)) {
				for (TypeChoiceInt tci : getAnnotation(TypeChoicesInt.class, ant).value()) {
					if (intVal == tci.key()) {
						cls = tci.value();
						found = true;
						break;
					}
				}
			}
			if (!found && hasAnnotation(TypeChoicesStr.class, ant)) {
				for (TypeChoiceStr tcs : getAnnotation(TypeChoicesStr.class, ant).value()) {
					if (strVal.equals(tcs.key())) {
						cls = tcs.value();
						found = true;
						break;
					}
				}
			}

			if (!found) {
				System.err.println("Warning: Unknown type choice: " + strVal + "(0x" + Integer.toHexString(intVal) + "). Using base type " + cls + " of field " + field + ".");
			} else {
				debugPrint("Resolved TypeChoice " + cls);
			}
		}
		ant[1] = cls;

		Object obj = null;

		if (cls == String.class) {
			String str;

			String magic = null;
			if (hasAnnotation(MagicStr.class, field)) {
				magic = field.getAnnotation(MagicStr.class).value();
			}

			if (hasAnnotation(Size.class, field)) {
				str = StringIO.readPaddedString(baseStream, field.getAnnotation(Size.class).value());
			} else if (magic != null) {
				str = StringIO.readPaddedString(baseStream, magic.length());
			} else if (obj_Size != -1) {
				str = StringIO.readPaddedString(baseStream, obj_Size);
			} else {
				str = StringIO.readString(baseStream);
			}
			if (magic != null) {
				if (hasAnnotation(MagicStrLE.class, field)) {
					magic = new StringBuilder(magic).reverse().toString();
				}
				if (!Objects.equals(magic, str)) {
					throw new RuntimeException("Invalid magic - expected " + magic + ", got " + str + ".");
				}
			}

			obj = str;
		} else if (Collection.class.isAssignableFrom(cls)) {
			Collection collection = null;
			if (cls == List.class) {
				cls = ArrayList.class;
			}
			try {
				collection = (Collection) cls.newInstance();
			} catch (InstantiationException ex) {
				throw new InstantiationException("Could not instantiate collection of type " + cls + " (field " + field + ").");
			}

			Type componentType = typeParameterStack.resolveType(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);

			debugPrint("Resolved list component type to " + componentType);

			for (int i = 0; i < obj_Size; i++) {
				debugPrint("Reading list element " + i + " of " + obj_Size);
				collection.add(readValue(componentType, field, true));
			}

			obj = collection;
		} else {
			try {
				obj = cls.newInstance();
			} catch (InstantiationException ex) {
				System.err.println("Could not instantiate " + field + "!");
				throw ex;
			}

			if (Modifier.isAbstract(cls.getModifiers())) {
				throw new InstantiationException("Can not instantiate abstract class " + cls + ". Check for invalid TypeChoice?");
			}

			readObjectFields(obj, cls, posBeforeObj);
		}

		if (ptr != -1) {
			baseStream.seek(posAfterPtr);
		}

		return obj;
	}

	private int readSizedInt(Field field) throws IOException {
		return readSizedInt(field, Integer.BYTES);
	}

	private int readSizedInt(Field field, int defaultSize) throws IOException {
		return readSizedInt(getIntSize(defaultSize, field));
	}

	private int readArrayLength(Field field) throws IOException {
		if (hasAnnotation(ArraySize.class, field)) {
			return field.getAnnotation(ArraySize.class).value();
		}

		int size = Integer.BYTES;
		if (hasAnnotation(ArrayLengthSize.class, field)) {
			size = field.getAnnotation(ArrayLengthSize.class).value();
		} else if (hasAnnotation(DefinedArraySize.class, field)) {
			Object sizeObj = definitions.get(field.getAnnotation(DefinedArraySize.class).value());
			debugPrint("Defined array len " + sizeObj);
			if (sizeObj instanceof Number) {
				return ((Number) sizeObj).intValue();
			} else {
				throw new RuntimeException("Definition " + field.getAnnotation(DefinedArraySize.class).value() + " is not a Number!");
			}
		}

		return readSizedInt(size);
	}

	private int readSizedInt(int size) throws IOException {
		switch (size) {
			case Integer.BYTES:
				return baseStream.readInt();
			case Short.BYTES:
				return baseStream.readUnsignedShort();
			case Byte.BYTES:
				return baseStream.readUnsignedByte();
		}

		throw new RuntimeException("Unhandled integer size: " + size);
	}
}
