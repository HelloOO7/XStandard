package xstandard.io.serialization;

import xstandard.io.IOCommon;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.base.iface.IOStream;
import xstandard.io.util.StringIO;
import xstandard.math.BitMath;
import xstandard.io.serialization.annotations.*;
import xstandard.io.serialization.annotations.typechoice.*;
import java.io.DataOutput;

import javax.lang.model.type.NullType;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BinarySerializer extends BinarySerialization {

	private Stack<Integer> pointerBaseStack = new Stack<>();

	private Map<Object, RefValue> refValueCache = new HashMap<>();

	private Map<Field, BitFieldWriter> bitFieldStates = new HashMap<>();

	public BinarySerializer(IOStream baseStream, ByteOrder bo, ReferenceType referenceType) {
		this(baseStream, bo, referenceType, DecimalType.FLOATING_POINT);
	}

	public BinarySerializer(IOStream baseStream, ByteOrder bo, ReferenceType referenceType, DecimalType decimalType) {
		super(baseStream, referenceType, decimalType);
		this.baseStream = new SerializationIOStream(baseStream);
		this.baseStream.order(bo);
		pointerBaseStack.push(0);
	}

	private void updatePointerBase(int addend) throws IOException {
		pointerBaseStack.push(baseStream.getPosition() + addend);
	}

	private void resetPointerBase() {
		pointerBaseStack.pop();
	}

	public static byte[] serialize(Object obj, ReferenceType refType) {
		try (DataIOStream out = new DataIOStream()) {
			serialize(out, obj, refType);
			return out.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(BinarySerializer.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static void serialize(DataOutput out, Object obj, ReferenceType refType) throws IOException {
		DataIOStream io = new DataIOStream();
		serialize(io, obj, refType);
		io.close();
		out.write(io.toByteArray());
	}

	public static void serialize(DataIOStream io, Object obj, ReferenceType refType) {
		BinarySerializer serializer = new BinarySerializer(io, ByteOrder.LITTLE_ENDIAN, refType);
		serializer.serialize(obj);
	}

	public void serialize(Object obj) {
		try {
			writeValue(obj, null);
		} catch (InstantiationException | IllegalAccessException | IOException e) {
			e.printStackTrace();
		}
	}

	private RefValue writeValue(Object value, Field field) throws InstantiationException, IllegalAccessException, IOException {
		IOCommon.debugPrint("Writing " + field + " at " + Integer.toHexString(baseStream.getPosition()));
		if (value != null) {
			Class cls = value.getClass();

			cls = BinaryDeserializer.getUnboxedClass(cls);

			if (value instanceof ICustomSerialization) {
				((ICustomSerialization) value).preSerialize(this);
			}

			FieldTypeGroup typeGroup = FieldTypeGroup.getTypeGroup(cls);

			if (cls.isAnnotationPresent(PointerBase.class)) {
				updatePointerBase(((PointerBase) cls.getAnnotation(PointerBase.class)).addend());
			}

			RefValue refValue = null;

			switch (typeGroup) {
				case PRIMITIVE:
					ByteOrder order = baseStream.order();
					boolean isBOM = hasAnnotation(ByteOrderMark.class, field);

					if (isBOM) {
						ByteOrderMark bom = field.getAnnotation(ByteOrderMark.class);
						int bomValue = baseStream.order() == ByteOrder.BIG_ENDIAN ? bom.ifBE() : bom.ifLE();

						baseStream.order(ByteOrder.BIG_ENDIAN);

						if (cls == Integer.TYPE) {
							baseStream.writeInt(bomValue);
						} else if (cls == Short.TYPE) {
							baseStream.writeShort((short) bomValue);
						} else if (cls == Byte.TYPE) {
							baseStream.write(bomValue);
						} else {
							throw new RuntimeException("Unsupported BOM class: " + cls);
						}

						baseStream.order(order);
					} else {
						writePrimitive(value, cls, field);
					}
					break;
				case ENUM:
					writeEnum(value, cls, field);
					break;
				case ARRAY:
				case OBJECT:
					refValue = writeObjectReference(value, cls, field);
					break;
			}

			if (cls.isAnnotationPresent(PointerBase.class)) {
				resetPointerBase();
			}

			if (field == null) {
				if (refValue != null) {
					for (RefValue child : refValue.children) {
						writeRefValue(child);
					}
				}

				return null;
			}

			return refValue;
		} else {
			if (field.getType().isEnum()) {
				writeEnum(null, field.getType(), field);
				return null;
			} else {
				return writeObjectReference(null, NullType.class, field);
			}
		}
	}

	private void writePrimitive(Object value, Class cls, Field field) throws IOException {
		BitFieldWriter bfw = bitFieldStates.get(field);
		if (bfw != null) {
			BitField bf = field.getAnnotation(BitField.class);
			bfw.writeValue(value, bf.startBit(), bf.bitCount());
		} else {
			if (hasAnnotation(PointerValue.class, cls, field)) {
				writePointer(((Number) value).intValue(), field);
			} else if (cls == Integer.TYPE) {
				writeSizedInt(value, field);
			} else if (cls == Short.TYPE) {
				baseStream.writeShort(((Number)value).shortValue());
			} else if (cls == Byte.TYPE) {
				baseStream.write(((Number)value).byteValue());
			} else if (cls == Boolean.TYPE) {
				writeSizedInt(((Boolean) value) ? 1 : 0, field, 1);
			} else if (cls == Float.TYPE) {
				baseStream.writeFloat((Float) value);
			} else if (cls == Double.TYPE) {
				baseStream.writeDouble((Double) value);
			} else if (cls == Long.TYPE) {
				baseStream.writeLong((Long) value);
			} else {
				throw new UnsupportedOperationException("Unsupported primitive: " + cls);
			}
		}
	}

	private void writeEnum(Object value, Class cls, Field field) throws IOException {
		int defaultSize = getDefaultEnumSize(cls);

		int ordinal = 0;
		if ((value != null && value instanceof ISerializableEnum)) {
			ordinal = ((ISerializableEnum) value).getOrdinal();
		} else {
			if (value != null) {
				ordinal = ((Enum) value).ordinal();
			} else {
				value = null;
			}
		}

		int enumSize = getIntSize(defaultSize, field, cls);
		IOCommon.debugPrint("Writing enum " + value + " with size " + enumSize);
		writePrimitive(ordinal, getNumberClassForSize(enumSize), field);
	}

	private List<RefValue> writeInlineArray(Object value, Field field) throws InstantiationException, IllegalAccessException, IOException {
		List<RefValue> children = new ArrayList<>();

		int size = Array.getLength(value);
		if (hasAnnotation(ArraySize.class, field)) {
			size = field.getAnnotation(ArraySize.class).value();
		}
		if (getLengthPos(field) == LengthPos.LengthPosType.AS_FIELD && getIsClassNeedsSize(value.getClass(), value.getClass(), field)) {
			writeArrayLength(size, field);
		}

		if (value instanceof byte[]) {
			//faster
			baseStream.write((byte[]) value, 0, size);
		} else {
			for (int i = 0; i < size; i++) {
				children.add(writeValue(Array.get(value, i), field));
			}
		}

		return children;
	}

	private void writePointer(int ptr, Field parentField) throws IOException {
		if (refType == ReferenceType.SELF_RELATIVE_POINTER) {
			ptr -= baseStream.getPosition();
		} else {
			ptr -= pointerBaseStack.peek();
		}
		writeSizedInt(ptr, parentField, Integer.BYTES);
	}

	private RefValue writeFieldPointer(Object value, Field field) throws IOException {
		RefValue refValue = refValueCache.get(value);
		if (refValue == null) {
			refValue = new RefValue();
			refValue.value = value;
			refValue.field = field;
			refValueCache.put(value, refValue);
		}

		PointerFuture pf = new PointerFuture();

		pf.position = baseStream.getPosition();
		pf.base = pointerBaseStack.peek();

		int pointerSize = Integer.BYTES;

		if (field.isAnnotationPresent(PointerSize.class)) {
			pointerSize = field.getAnnotation(PointerSize.class).value();
		}
		pf.size = pointerSize;
		refValue.pointersToHere.add(pf);

		writeSizedInt(0, null, pointerSize);

		return refValue;
	}

	private List<RefValue> writeInlineObject(Object value, Field field) throws InstantiationException, IllegalAccessException, IOException {
		List<RefValue> fields = new ArrayList<>();

		if (value == null) {
			return fields;
		}

		Class cls = value.getClass();

		cls = BinaryDeserializer.getUnboxedClass(cls);

		AnnotatedElement[] ant = new AnnotatedElement[]{field, cls};

		int posBeforeObj = baseStream.getPositionUnbased();

		baseStream.resetSeekTrace();

		if (hasAnnotation(TypeChoicesStr.class, ant) || hasAnnotation(TypeChoicesInt.class, ant)) {
			int size = getIntSize(Integer.BYTES, ant);

			boolean success = false;

			for (TypeChoiceStr tcs : getAnnotation(TypeChoicesStr.class, ant).value()) {
				if (cls == tcs.value()) {
					String key = tcs.key();
					if (hasAnnotation(MagicStrLE.class, ant)) {
						key = new StringBuilder(key).reverse().toString();
					}
					StringIO.writePaddedString(baseStream, key, size);
					success = true;
					break;
				}
			}

			if (!success) {
				for (TypeChoiceInt tci : getAnnotation(TypeChoicesInt.class, ant).value()) {
					if (cls == tci.value()) {
						writeSizedInt(tci.value(), null, size);
						success = true;
						break;
					}
				}
			}

			if (!success) {
				throw new UnsupportedOperationException("Unhandled type choice class " + cls);
			}
		}

		if (value instanceof ICustomSerialization) {
			if (((ICustomSerialization) value).preSerialize(this)) {
				return fields;
			}
		}

		if (value instanceof Collection) {
			Collection col = (Collection) value;
			if (getLengthPos(ant) == LengthPos.LengthPosType.AS_FIELD) {
				writeArrayLength(col.size(), field);
			}
			for (Object element : col) {
				fields.add(writeValue(element, field));
			}
		} else if (cls.isArray()) {
			fields.addAll(writeInlineArray(value, field));
		} else if (value instanceof String) {
			if (hasAnnotation(MagicStr.class, ant)) {
				int size = getIntSize(Integer.BYTES, ant);
				String magic = getAnnotation(MagicStr.class, ant).value();
				if (hasAnnotation(MagicStrLE.class)) {
					magic = new StringBuilder(magic).reverse().toString();
				}
				IOCommon.debugPrint("Writing magic string of size " + size);
				StringIO.writePaddedString(baseStream, magic, size);
			} else {
				String str = (String) value;
				int size = getIntSize(str.length() + 1, ant);
				StringIO.writePaddedString(baseStream, str, size);
			}
		} else {
			Map<Integer, Field> objSizeFields = new HashMap<>();

			Field lastBitfieldStart = null;
			Field lastBitfield = null;
			for (Field memberField : getSortedFields(value.getClass())) {
				int mod = memberField.getModifiers();
				if (!Modifier.isStatic(mod) && !memberField.isAnnotationPresent(Ignore.class)) {
					if (memberField.isAnnotationPresent(ObjSize.class)) {
						objSizeFields.put(baseStream.getPosition(), memberField);
					}

					if (isBitfield(memberField)) {
						if (isBitfieldStart(memberField)) {
							commitBitfield(lastBitfieldStart, lastBitfield);
							lastBitfieldStart = memberField;
							lockBitfield(memberField);
						} else {
							relockBitfield(lastBitfield, memberField);
						}
						lastBitfield = memberField;
					} else {
						commitBitfield(lastBitfieldStart, lastBitfield);
						lastBitfield = null;
						lastBitfieldStart = null;
					}

					fields.add(writeValue(memberField.get(value), memberField));
				}
			}
			commitBitfield(lastBitfieldStart, lastBitfield);

			if (value instanceof ICustomSerialization) {
				((ICustomSerialization) value).postSerialize(this);
			}

			int posAfterObj = baseStream.getPosition();

			int objSizePos = posAfterObj - posBeforeObj;
			int objSizeAll = (int) (baseStream.getMaxSeekSinceTrace() - posBeforeObj);

			for (Map.Entry<Integer, Field> osf : objSizeFields.entrySet()) {
				baseStream.seek(osf.getKey());

				Field fld = osf.getValue();
				Class fieldClass = getUnboxedClass(fld.getType());

				int objSize;
				if (fld.getAnnotation(ObjSize.class).inclChildren()) {
					objSize = objSizeAll;
				} else {
					objSize = objSizePos;
				}

				if (fieldClass == Integer.TYPE) {
					fld.setInt(value, (int) objSize);
				} else if (fieldClass == Short.TYPE) {
					fld.setShort(value, (short) objSize);
				} else {
					throw new UnsupportedOperationException("ObjSize field " + fld + " is not an Integer nor a Short.");
				}

				writeValue(fld.get(value), fld);
			}

			baseStream.seek(posAfterObj);
		}

		return fields;
	}

	private void commitBitfield(Field startField, Field lockOwnerField) throws IOException {
		if (startField != null) {
			BitFieldWriter w = bitFieldStates.get(lockOwnerField);
			Number out = 0;
			Class type;
			if (startField.getType().isEnum()) {
				type = getNumberClassForSize(getIntSize(getDefaultEnumSize(startField.getType()), startField));
			} else {
				type = getUnboxedClass(startField.getType());
			}
			if (type == Boolean.TYPE) {
				type = getNumberClassForSize(getIntSize(1, type));
			}
			if (type == Long.TYPE) {
				out = w.getCombValue().longValue();
			} else if (type == Integer.TYPE) {
				out = w.getCombValue().intValue();
			} else if (type == Short.TYPE) {
				out = w.getCombValue().shortValue();
			} else if (type == Byte.TYPE) {
				out = w.getCombValue().byteValue();
			} else {
				throw new RuntimeException("Unhandled bitfield class: " + type);
			}
			unlockBitfield(lockOwnerField);
			writePrimitive(out, type, startField);
		}
	}

	private RefValue writeObjectReference(Object value, Class cls, Field field) throws InstantiationException, IllegalAccessException, IOException {
		boolean obj_NeedsSize = getIsClassNeedsSize(cls, cls, field);
		int size = -1;
		LengthPos.LengthPosType lp = getLengthPos(cls, field);

		if (obj_NeedsSize) {
			if (value == null) {
				size = 0;
			} else if (value instanceof String) {
				size = ((String) value).length();
			} else if (value instanceof Collection) {
				size = ((Collection) value).size();
			} else if (cls.isArray()) {
				size = Array.getLength(value);
			} else {
				throw new RuntimeException("Class is not valid for object size!");
			}
		}

		boolean isInline = 
			hasAnnotation(Inline.class, cls, field)
			|| refType == ReferenceType.NONE 
			|| field == null 
			|| hasAnnotation(MagicStr.class, cls, field)
			|| (hasAnnotation(InlineArray.class, field) && (cls.isArray() || Collection.class.isAssignableFrom(cls)));

		if ((lp == LengthPos.LengthPosType.BEFORE_PTR) && obj_NeedsSize) {
			writeArrayLength(size, field);
		}

		if (isInline) {
			RefValue val = new RefValue();
			val.children = writeInlineObject(value, field);

			return val;
		} else {
			RefValue val = writeFieldPointer(value, field);

			if (lp == LengthPos.LengthPosType.AFTER_PTR && obj_NeedsSize) {
				writeArrayLength(size, field);
			}

			return val;
		}
	}

	private void writeRefValue(RefValue value) throws InstantiationException, IllegalAccessException, IOException {
		if (value != null) {
			boolean notYetWritten = false;
			baseStream.align(4);
			int position = baseStream.getPosition();
			if (value.myPointer == -1) {
				value.myPointer = position;
				notYetWritten = true;
			}

			for (PointerFuture pf : value.pointersToHere) {
				if (pf.position != 0) {
					baseStream.seek(pf.position);

					int ptr = value.myPointer;

					if (value.value == null) {
						ptr = 0;
					} else {
						if (refType == ReferenceType.SELF_RELATIVE_POINTER) {
							ptr -= pf.position;
						} else {
							ptr -= pf.base;
						}
					}

					writeSizedInt(ptr, null, pf.size);
				} else {
					//Inline object - already written
				}
			}

			baseStream.seek(position);

			if (notYetWritten) {
				value.children.addAll(writeInlineObject(value.value, value.field));

				for (RefValue child : value.children) {
					writeRefValue(child);
				}
			}
		}
	}

	private void writeArrayLength(int length, Field field) throws IOException {
		int size = Integer.BYTES;
		if (hasAnnotation(ArrayLengthSize.class, field)) {
			size = field.getAnnotation(ArrayLengthSize.class).value();
		}

		writeSizedInt(length, null, size);
	}

	private void writeSizedInt(Object value, Field field) throws IOException {
		writeSizedInt(value, field, Integer.BYTES);
	}

	private void writeSizedInt(Object value, Field field, int defaultSize) throws IOException {
		int size = defaultSize;
		if (hasAnnotation(Size.class, field)) {
			size = getAnnotation(Size.class, field).value();
		}

		switch (size) {
			case Byte.BYTES:
				baseStream.write(((Number) value).byteValue());
				break;
			case Short.BYTES:
				baseStream.writeShort(((Number) value).shortValue());
				break;
			case Integer.BYTES:
				baseStream.writeInt(((Number) value).intValue());
				break;
			default:
				throw new RuntimeException("Unsupported integer size: " + size + ".");
		}
	}

	private void lockBitfield(Field field) {
		bitFieldStates.put(field, new BitFieldWriter());
	}

	private void relockBitfield(Field oldField, Field newField) {
		if (oldField == null) {
			throw new RuntimeException("Should not relock nonexistent bitfield!");
		}
		bitFieldStates.put(newField, bitFieldStates.remove(oldField));
	}

	private void unlockBitfield(Field field) {
		if (field != null) {
			bitFieldStates.remove(field);
		}
	}

	private static class BitFieldWriter {

		private int combValue;

		public void writeValue(Object value, int start, int length) {
			if (value instanceof Boolean) {
				value = ((Boolean) value) ? 1 : 0;
			} else if (value instanceof Enum) {
				value = ((Enum) value).ordinal();
			}
			BitMath.setIntegerBits(combValue, start, length, ((Number) value).intValue());
		}

		public Number getCombValue() {
			return combValue;
		}
	}

	static class RefValue {

		public int myPointer = -1;

		public List<PointerFuture> pointersToHere = new ArrayList<>();
		public Object value;
		public Field field;

		public List<RefValue> children = new ArrayList<>();
	}

	static class PointerFuture {

		public int position;
		public int base;
		public int size;
	}
}
