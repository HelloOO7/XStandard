package xstandard.io.serialization;

import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.base.iface.IOStream;
import xstandard.io.util.StringIO;
import xstandard.io.serialization.annotations.*;
import xstandard.io.serialization.annotations.typechoice.*;

import java.io.IOException;
import java.lang.reflect.*;
import static xstandard.io.IOCommon.*;
import xstandard.io.base.impl.access.MemoryStream;
import xstandard.math.BitMath;
import java.nio.ByteOrder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.fs.FSFile;

public class BinaryDeserializer extends BinarySerialization {

	private TypeParameterStack typeParameterStack = new TypeParameterStack();

	private Stack<Integer> pointerBaseStack = new Stack<>();

	private Map<String, Object> definitions = new HashMap<>();

	private Map<Integer, List<Object>> objectsAtAddresses = new HashMap<>();

	private Map<Field, BitFieldReader> bitFieldStates = new HashMap<>();

	public BinaryDeserializer() {
		this(DEFAULT_BYTE_ORDER);
	}

	public BinaryDeserializer(ByteOrder bo) {
		this(bo, ReferenceType.ABSOLUTE_POINTER);
	}

	public BinaryDeserializer(ReferenceType referenceType) {
		this(DEFAULT_BYTE_ORDER, referenceType);
	}

	public BinaryDeserializer(ByteOrder bo, ReferenceType referenceType) {
		this(bo, referenceType, DecimalType.FLOATING_POINT);
	}

	public BinaryDeserializer(ByteOrder bo, ReferenceType referenceType, DecimalType decimalType) {
		this(null, bo, referenceType, decimalType);
	}

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
				SerializationIOStream newStream = new SerializationIOStream(baseStream.toByteArray());
				baseStream.close();
				baseStream = newStream;
			} catch (IOException ex) {
				Logger.getLogger(BinaryDeserializer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public <T> T deserializeFile(Class<T> cls, FSFile fsf) {
		setStream(fsf.getIO());
		T retval = deserialize(cls);
		closeStream();
		setStream(null);
		return retval;
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
			readObjectFields(obj, obj.getClass(), baseStream.getPositionUnbased());
		} catch (IOException | InstantiationException | IllegalAccessException ex) {
			Logger.getLogger(BinaryDeserializer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void readObjectFields(Object obj, Class cls, int objStartAddress) throws InstantiationException, IllegalAccessException, IOException {
		baseStream.resetSeekTrace();

		List<Field> objSizeFields = new ArrayList<>();
		List<String> localDefinitions = new ArrayList<>();

		if (cls == null) {
			cls = obj.getClass();
		}

		if (cls.isAnnotationPresent(PointerBase.class)) {
			updatePointerBase(((PointerBase) cls.getAnnotation(PointerBase.class)).addend());
		}

		Field lastBitfieldLockField = null;
		for (Field fld : getSortedFields(cls)) {
			if (fld.isAnnotationPresent(IfVersion.class)) {
				if (!isIfVersionPass(fld.getAnnotation(IfVersion.class))) {
					continue;
				}
			}
			if (!fld.isAnnotationPresent(Ignore.class)) {
				if (isBitfield(fld)) {
					if (isBitfieldStart(fld)) {
						unlockBitfield(lastBitfieldLockField);
						lockBitfield(fld);
					} else {
						relockBitfield(lastBitfieldLockField, fld);
					}
					lastBitfieldLockField = fld;
				} else {
					lastBitfieldLockField = null;
					unlockBitfield(lastBitfieldLockField);
				}
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
		unlockBitfield(lastBitfieldLockField);

		if (obj instanceof ICustomSerialization) {
			((ICustomSerialization) obj).deserialize(this);
		}

		int expectedObjSizePos = baseStream.getPositionUnbased() - objStartAddress;
		int expectedObjSizeAll = (int) (baseStream.getMaxSeekSinceTrace() - objStartAddress);

		for (Field objSizeFld : objSizeFields) {
			Object fldValue = objSizeFld.get(obj);
			if (fldValue instanceof Number) {
				int objSize = ((Number) fldValue).intValue();

				int expectedObjSize;
				if (objSizeFld.getAnnotation(ObjSize.class).inclChildren()) {
					expectedObjSize = expectedObjSizeAll;
				} else {
					expectedObjSize = expectedObjSizePos;
				}

				if (objSize != expectedObjSize) {
					throw new RuntimeException(String.format("Object size does 0x%08X not match actual object size (0x%08X)! Object start: 0x%08X, Current stream position: 0x%08X, max seektrace: 0x%08X", objSize, expectedObjSize, objStartAddress, baseStream.getPositionUnbased(), baseStream.getMaxSeekSinceTrace()));
				}
			} else {
				throw new RuntimeException("ObjSize field is not a numeric primitive!");
			}
		}

		for (String def : localDefinitions) {
			definitions.remove(def);
		}

		if (cls.isAnnotationPresent(PointerBase.class)) {
			resetPointerBase();
		}
	}

	public Object getDefinition(String name) {
		return definitions.get(name);
	}

	private void updatePointerBase(int addend) throws IOException {
		pointerBaseStack.push(baseStream.getPosition() + addend);
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

		return value;
	}

	public int getBasedPointer(int ptr) {
		return ptr + pointerBaseStack.peek();
	}

	private Object readPrimitive(Class cls, Field field) throws IOException {
		BitFieldReader bfr = bitFieldStates.get(field);
		Object retval = null;

		if (bfr != null && bfr.isInitialized()) {
			retval = decodeBitfieldPrimitive(cls, field);
		} else {
			if (hasAnnotation(PointerValue.class, cls, field)) {
				retval = readPointer();
			}
			else if (cls == Integer.TYPE) {
				retval = readSizedInt(field);
			} else if (cls == Short.TYPE) {
				retval = baseStream.readShort();
			} else if (cls == Byte.TYPE) {
				retval = baseStream.readByte();
			} else if (cls == Boolean.TYPE) {
				retval = readSizedInt(field, 1) == 1;
			} else if (cls == Float.TYPE) {
				switch (decimalType) {
					case FLOATING_POINT:
						retval = baseStream.readFloat();
						break;
					case FIXED_POINT_NNFX:
						int intValue = readSizedSignedInt(field);
						retval = intValue / 4096f;
						break;
				}
			} else if (cls == Double.TYPE) {
				retval = baseStream.readDouble();
			} else if (cls == Long.TYPE) {
				retval = baseStream.readLong();
			}
			if (retval == null) {
				throw new UnsupportedOperationException("Unsupported primitive: " + cls);
			} else {
				if (bfr != null) {
					bfr.setSrcVal((Number) retval);
					retval = decodeBitfieldPrimitive(cls, field);
				}
			}
		}
		return retval;
	}

	private Object decodeBitfieldPrimitive(Class cls, Field field) {
		Object retval;
		BitFieldReader bfr = bitFieldStates.get(field);
		BitField bf = field.getAnnotation(BitField.class);
		int bfInt = bfr.read(bf.startBit(), bf.bitCount());
		if (cls == Integer.TYPE) {
			retval = bfInt;
		} else if (cls == Short.TYPE) {
			retval = (short) bfInt;
		} else if (cls == Byte.TYPE) {
			retval = (byte) bfInt;
		} else if (cls == Boolean.TYPE) {
			retval = bfInt == 1;
		} else {
			throw new UnsupportedOperationException("Can not use primitive type " + cls + " in a bitfield!");
		}
		return retval;
	}

	private Enum readEnum(Class cls, Field field) throws IOException {
		Object[] constants = cls.getEnumConstants();

		int defaultSize = getDefaultEnumSize(cls);

		int size = getIntSize(defaultSize, field, cls);
		int ordinal = ((Number) readPrimitive(getNumberClassForSize(size), field)).intValue();

		if (ISerializableEnum.class.isAssignableFrom(cls)) {
			for (Object ev : constants) {
				if (((ISerializableEnum) ev).getOrdinal() == ordinal) {
					return (Enum) ev;
				}
			}
			return null;
		} else {
			if (ordinal < 0 || ordinal >= constants.length) {
				debugPrint("Deserializer error: Could not resolve enum constant " + ordinal + " for field " + field);
				return null;
			}

			return (Enum) constants[ordinal];
		}
	}
	
	private void saveObjectAddress(int addr, Object o) {
		List<Object> l = objectsAtAddresses.get(addr);
		if (l == null) {
			l = new ArrayList<>();
			objectsAtAddresses.put(addr, l);
		}
		l.add(o);
	}
	
	private Object getExistObject(int addr, Class cls) {
		List<Object> l = objectsAtAddresses.get(addr);
		if (l != null) {
			for (Object o : l) {
				if (cls.isAssignableFrom(o.getClass())) {
					return o;
				}
			}
		}
		return null;
	}

	private Object readArray(Class cls, Field field) throws InstantiationException, IllegalAccessException, IOException {
		debugPrint("array " + field + " at " + Integer.toHexString(baseStream.getPosition()));
		LengthPos.LengthPosType lengthPos = getLengthPos(field);

		int size = 0;
		int ptr;

		switch (lengthPos) {
			case BEFORE_PTR:
				size = readArrayLength(field);
				ptr = readPointer(field, false, field);
				break;
			case AFTER_PTR:
				ptr = readPointer(field, false, field);
				size = readArrayLength(field);
				break;
			default:
				ptr = readPointer(field, false, field);
				break;
		}

		int posAfterPtr = baseStream.getPosition();

		seekPointer(ptr);
		int absPtr = baseStream.getPositionUnbased();
		Object existObj = getExistObject(absPtr, cls);
		Object arr;
		if (existObj != null) {
			arr = existObj;
		} else {
			Class componentType = cls.getComponentType();

			if (lengthPos == LengthPos.LengthPosType.AS_FIELD) {
				size = readArrayLength(field);
			}

			arr = Array.newInstance(componentType, size);
			debugPrint("array size " + size);
			if (arr instanceof byte[]) {
				baseStream.read((byte[]) arr); //faster
			} else {
				for (int i = 0; i < size; i++) {
					Object value = readValue(componentType, field, true);
					Array.set(arr, i, value);
				}
			}
			saveObjectAddress(absPtr, arr);
		}
		if (ptr != -1) {
			baseStream.seek(posAfterPtr);
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

	private int readPointer(Field field, boolean isListElem, AnnotatedElement... ant) throws IOException {
		int posBeforePtr = baseStream.getPosition();
		if (
			(field != null || isListElem) 
			&& refType != ReferenceType.NONE 
			&& !hasAnnotation(Inline.class, ant) 
			&& !hasAnnotation(MagicStr.class, ant)
			&& !(!isListElem && hasAnnotation(InlineArray.class, ant))
		) {
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
			if (hasAnnotation(PointerInv.class, ant)) {
				ptr = -ptr;
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

	private Object readObject(Class cls, Field field, boolean isListElem) throws InstantiationException, IllegalAccessException, IOException {
		AnnotatedElement[] ant = new AnnotatedElement[]{field, cls};

		boolean obj_NeedsSize = getIsClassNeedsSize(cls, ant);

		int posAfterPtr = -1;
		int obj_Size = -1;

		int ptr = -1;

		if (obj_NeedsSize) {
			debugPrint("Object needs size!");
			LengthPos.LengthPosType lengthPos = getLengthPos(ant);

			switch (lengthPos) {
				case BEFORE_PTR:
					obj_Size = readArrayLength(field);
					ptr = readPointer(field, isListElem, ant);
					posAfterPtr = baseStream.getPosition();
					break;
				case AFTER_PTR:
					ptr = readPointer(field, isListElem, ant);
					obj_Size = readArrayLength(field);
					posAfterPtr = baseStream.getPosition();
					break;
				default:
					ptr = readPointer(field, isListElem, ant);
					posAfterPtr = baseStream.getPosition();
					break;
			}
		} else {
			ptr = readPointer(field, isListElem, ant);
			posAfterPtr = baseStream.getPosition();
		}

		if (ptr == 0) {
			return null;
		}
		seekPointer(ptr);
		Object obj = null;

		int posBeforeObj = baseStream.getPositionUnbased();
		Object existObj = getExistObject(posBeforeObj, cls);
		if (existObj != null) {
			obj = existObj;
		} else {
			if (hasAnnotation(TypeChoicesStr.class, ant) || hasAnnotation(TypeChoicesInt.class, ant)) {
				boolean found = false;
				int size = getIntSize(Integer.BYTES, ant);

				int intVal = readSizedInt(size);
				baseStream.seek(baseStream.getPosition() - size);
				String strVal = StringIO.readPaddedString(baseStream, size);

				if (hasAnnotation(MagicStrLE.class, ant)) {
					strVal = new StringBuilder(strVal).reverse().toString();
				}

				if (hasAnnotation(TypeChoicesInt.class, ant)) {
					debugPrint("Typechoice int 0x" + Integer.toHexString(intVal));
					for (TypeChoiceInt tci : getAnnotation(TypeChoicesInt.class, ant).value()) {
						if (intVal == tci.key()) {
							cls = tci.value();
							found = true;
							break;
						}
					}
				}
				if (!found && hasAnnotation(TypeChoicesStr.class, ant)) {
					debugPrint("Typechoice str " + strVal);
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

				if (obj_Size == -1) {
					obj_Size = readArrayLength(field);
				}
				debugPrint("List size " + obj_Size);
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
			saveObjectAddress(posBeforeObj, obj);
		}

		if (ptr != -1) {
			baseStream.seek(posAfterPtr);
		}

		return obj;
	}
	
	private int readSizedSignedInt(Field field) throws IOException {
		return readSizedSignedInt(field, Integer.BYTES);
	}

	private int readSizedSignedInt(Field field, int defaultSize) throws IOException {
		return readSizedSignedInt(getIntSize(defaultSize, field));
	}

	private int readSizedInt(Field field) throws IOException {
		return readSizedInt(field, Integer.BYTES);
	}

	private int readSizedInt(Field field, int defaultSize) throws IOException {
		return readSizedInt(getIntSize(defaultSize, field));
	}

	protected int readArrayLength(Field field) throws IOException {
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
	
	private int readSizedSignedInt(int size) throws IOException {
		switch (size) {
			case Integer.BYTES:
				return baseStream.readInt();
			case Short.BYTES:
				return baseStream.readShort();
			case Byte.BYTES:
				return baseStream.readByte();
		}

		throw new RuntimeException("Unhandled integer size: " + size);
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

	private void lockBitfield(Field field) {
		bitFieldStates.put(field, new BitFieldReader());
	}

	private void relockBitfield(Field oldField, Field newField) {
		if (oldField == null) {
			throw new RuntimeException("Should not relock nonexistent bitfield! - newField = " + newField);
		}
		bitFieldStates.put(newField, bitFieldStates.remove(oldField));
	}

	private void unlockBitfield(Field field) {
		if (field != null) {
			bitFieldStates.remove(field);
		}
	}

	private static class BitFieldReader {

		private boolean initialized = false;
		private int sourceValue;

		public boolean isInitialized() {
			return initialized;
		}

		public void setSrcVal(Number number) {
			sourceValue = number.intValue();
			initialized = true;
		}

		public int read(int start, int length) {
			return BitMath.getIntegerBits(sourceValue, start, length);
		}
	}
}
