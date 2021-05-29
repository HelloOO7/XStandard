package ctrmap.stdlib.io.serialization;

import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import ctrmap.stdlib.io.base.iface.IOStream;
import ctrmap.stdlib.io.serialization.annotations.ArrayLengthSize;
import ctrmap.stdlib.io.serialization.annotations.ArraySize;
import ctrmap.stdlib.io.serialization.annotations.ByteOrderMark;
import ctrmap.stdlib.io.serialization.annotations.DefinedArraySize;
import ctrmap.stdlib.io.serialization.annotations.Ignore;
import ctrmap.stdlib.io.serialization.annotations.Inline;
import ctrmap.stdlib.io.serialization.annotations.MagicStr;
import ctrmap.stdlib.io.serialization.annotations.MagicStrLE;
import ctrmap.stdlib.io.serialization.annotations.ObjSize;
import ctrmap.stdlib.io.serialization.annotations.PointerBase;
import ctrmap.stdlib.io.serialization.annotations.PointerSize;
import ctrmap.stdlib.io.serialization.annotations.Size;
import ctrmap.stdlib.io.util.StringIO;
import ctrmap.stdlib.io.serialization.annotations.typechoice.TypeChoiceInt;
import ctrmap.stdlib.io.serialization.annotations.typechoice.TypeChoiceStr;
import ctrmap.stdlib.io.serialization.annotations.typechoice.TypeChoicesInt;
import ctrmap.stdlib.io.serialization.annotations.typechoice.TypeChoicesStr;
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
    public final DataIOStream baseStream;

    private final ReferenceType refType;

    private Stack<Integer> pointerBaseStack = new Stack<>();
    private RefValue refValue;

    public BinarySerializer(IOStream baseStream, ByteOrder bo, ReferenceType referenceType){
        refType = referenceType;
        this.baseStream = new DataIOStream(baseStream, bo);
        pointerBaseStack.push(0);
    }

    private void updatePointerBase() throws IOException  {
        pointerBaseStack.push(baseStream.getPosition());
    }

    private void resetPointerBase(){
        pointerBaseStack.pop();
    }
	
	public static byte[] serialize(Object obj, ReferenceType refType){
		try (DataIOStream out = new DataIOStream()){
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
	
	public static void serialize(DataIOStream io, Object obj, ReferenceType refType){
		BinarySerializer serializer = new BinarySerializer(io, ByteOrder.LITTLE_ENDIAN, refType);
		serializer.serialize(obj);
	}

    public void serialize(Object obj){
        try {
            writeValue(obj, null);
        } catch (InstantiationException | IllegalAccessException | IOException e) {
            e.printStackTrace();
        }
    }

    private RefValue writeValue(Object value, Field field) throws InstantiationException, IllegalAccessException, IOException {
        if (value != null) {
            Class cls = value.getClass();

            cls = BinaryDeserializer.getUnboxedClass(cls);

            if (value instanceof ICustomSerialization){
                ((ICustomSerialization)value).preSerialize(this);
            }

            FieldTypeGroup typeGroup = FieldTypeGroup.getTypeGroup(cls);

            if (cls.isAnnotationPresent(PointerBase.class)){
                updatePointerBase();
            }

            RefValue refValue = null;

            switch (typeGroup){
                case PRIMITIVE:
                    ByteOrder order = baseStream.order();
                    boolean isBOM = hasAnnotation(ByteOrderMark.class, field);

                    if (isBOM){
                        ByteOrderMark bom   = field.getAnnotation(ByteOrderMark.class);
                        int bomValue        = baseStream.order() == ByteOrder.BIG_ENDIAN ? bom.ifBE() : bom.ifLE();

                        baseStream.order(ByteOrder.BIG_ENDIAN);

                        if (cls == Integer.TYPE){
                            baseStream.writeInt(bomValue);
                        }
                        else if (cls == Short.TYPE){
                            baseStream.writeShort((short)bomValue);
                        }
                        else {
                            throw new RuntimeException("Unsupported BOM class: " + cls);
                        }

                        baseStream.order(order);
                    }
                    else {
                        writePrimitive(value, cls, field);
                    }
                    break;
                case ENUM:
                    writeEnum(value, cls, field);
                    break;
                case ARRAY:
                    refValue = writeArray(value, cls, field);
                    break;
                case OBJECT:
                    refValue = writeObjectReference(value, cls, field);
                    break;
            }

            if (cls.isAnnotationPresent(PointerBase.class)){
                resetPointerBase();
            }

            if (field == null){
                for (RefValue child : refValue.children){
                    writeRefValue(child);
                }

                return null;
            }

            return refValue;
        }
        else {
            return writeObjectReference(null, NullType.class, field);
        }
    }

    private void writePrimitive(Object value, Class cls, Field field) throws IOException {
        if (cls == Integer.TYPE){
            writeSizedInt(value, field);
        }
        else if (cls == Short.TYPE){
            baseStream.writeShort((Short)value);
        }
        else if (cls == Byte.TYPE){
            baseStream.write((Byte)value);
        }
        else if (cls == Boolean.TYPE){
            writeSizedInt(((Boolean)value) ? 1 : 0, field, 1);
        }
        else if (cls == Float.TYPE){
            baseStream.writeFloat((Float)value);
        }
        else if (cls == Double.TYPE){
            baseStream.writeDouble((Double)value);
        }
        else if (cls == Long.TYPE){
            baseStream.writeLong((Long)value);
        }
        else {
            throw new UnsupportedOperationException("Unsupported primitive: " + cls);
        }
    }

    private void writeEnum(Object value, Class cls, Field field) throws IOException {
        Object[] constants = cls.getEnumConstants();

        int defaultSize = constants.length <= 0x100 ? 1 : constants.length <= 0x10000 ? 2 : 4;

        writeSizedInt(((Enum)value).ordinal(), field, getIntSize(defaultSize, field, cls));
    }

    private RefValue writeArray(Object value, Class cls, Field field) throws InstantiationException, IllegalAccessException, IOException {
        RefValue refValue = new RefValue();
        refValue.field = field;

        int size = Array.getLength(value);
        if (!hasAnnotation(ArraySize.class, field) && !hasAnnotation(DefinedArraySize.class, field)){
            writeArrayLength(size, field);
        }

        for (int i = 0; i < size; i++){
            refValue.children.add(writeValue(Array.get(value, i), field));
        }

        return refValue;
    }

    private RefValue writeFieldPointer(Object value, Field field) throws IOException {
        refValue = new RefValue();
        refValue.pointerPosition    = baseStream.getPosition();
        refValue.pointerBase        = pointerBaseStack.peek();
        refValue.value              = value;
        refValue.field              = field;
        return refValue;
    }

    private List<RefValue> writeInlineObject(Object value, Field field) throws InstantiationException, IllegalAccessException, IOException {
        List<RefValue> fields = new ArrayList<>();

        if (value == null){
            return fields;
        }

        Class cls = value.getClass();

        cls = BinaryDeserializer.getUnboxedClass(cls);

        AnnotatedElement[] ant = new AnnotatedElement[]{field, cls};

        int posBeforeObj = baseStream.getPosition();

        AnnotatedElement[] antTc = new AnnotatedElement[]{field, cls.getSuperclass()};

        if (hasAnnotation(TypeChoicesStr.class, antTc) || hasAnnotation(TypeChoicesInt.class, antTc)){
            int size = getIntSize(Integer.BYTES, antTc);

            boolean success = false;

            for (TypeChoiceStr tcs : getAnnotation(TypeChoicesStr.class, antTc).value()){
                if (cls == tcs.value()){
                    String key = tcs.key();
                    if (hasAnnotation(MagicStrLE.class, antTc)){
                        key = new StringBuilder(key).reverse().toString();
                    }
                    StringIO.writePaddedString(baseStream, key, size);
                    success = true;
                    break;
                }
            }

            if (!success){
                for (TypeChoiceInt tci : getAnnotation(TypeChoicesInt.class, antTc).value()){
                    if (cls == tci.value()){
                        writeSizedInt(tci.value(), null, size);
                        success = true;
                        break;
                    }
                }
            }

            if (!success){
                throw new UnsupportedOperationException("Unhandled type choice class " + cls);
            }
        }

        if (value instanceof ICustomSerialization){
            if (((ICustomSerialization)value).preSerialize(this)){
                return fields;
            }
        }

        if (value instanceof Collection){
            Collection col = (Collection) value;
            if (!hasAnnotation(ArraySize.class, field) && !hasAnnotation(DefinedArraySize.class)){
                writeArrayLength(col.size(), field);
            }

            for (Object element : col){
                fields.add(writeValue(element, field));
            }
        }
        else if (value instanceof String){
            if (hasAnnotation(MagicStr.class)){
                int size = getIntSize(Integer.BYTES, ant);
                String magic = getAnnotation(MagicStr.class, ant).text();
                if (hasAnnotation(MagicStrLE.class)){
                    magic = new StringBuilder(magic).reverse().toString();
                }
                StringIO.writePaddedString(baseStream, magic, size);
            }
            else {
                String str = (String) value;
                int size = getIntSize(str.length(), ant);
                StringIO.writePaddedString(baseStream, str, size);
            }
        }
        else {
            Map<Integer, Field> objSizeFields = new HashMap<>();

            for (Field fld : getSortedFields(value.getClass())){
                int mod = fld.getModifiers();
                if (!Modifier.isStatic(mod) && !fld.isAnnotationPresent(Ignore.class)){
                    if (fld.isAnnotationPresent(ObjSize.class)){
                        objSizeFields.put(baseStream.getPosition(), fld);
                    }

                    fields.add(writeValue(fld.get(value), fld));
                }
            }

            if (value instanceof ICustomSerialization){
                ((ICustomSerialization)value).postSerialize(this);
            }

            int posAfterObj = baseStream.getPosition();

            int objSize = posAfterObj - posBeforeObj;

            for (Map.Entry<Integer, Field> osf : objSizeFields.entrySet()){
                baseStream.seek(osf.getKey());

                Field fld = osf.getValue();
                Class fieldClass = getUnboxedClass(fld.getType());

                if (fieldClass == Integer.TYPE){
                    fld.setShort(value, (short)objSize);
                }
                else if (fieldClass == Short.TYPE){
                    fld.setInt(value, (int)objSize);
                }
                else {
                    throw new UnsupportedOperationException("ObjSize field " + fld + " is not an Integer nor a Short.");
                }

                writeValue(fld.get(value), fld);
            }

            baseStream.seek(posAfterObj);
        }

        return fields;
    }

    private RefValue writeObjectReference(Object value, Class cls, Field field) throws InstantiationException, IllegalAccessException, IOException {
        if (hasAnnotation(Inline.class, cls, field) || refType == ReferenceType.NONE || field == null){
            RefValue val = new RefValue();

            val.children = writeInlineObject(value, field);

            return val;
        }
        else {
            RefValue val = writeFieldPointer(value, field);
            return val;
        }
    }

    private void writeRefValue(RefValue value) throws InstantiationException, IllegalAccessException, IOException {
        if (value != null){
            int position = baseStream.getPosition();

            if (value.pointerPosition != 0){
                baseStream.seek(value.pointerPosition);

                int ptr = position;

                if (value == null){
                    ptr = 0;
                }
                else {
                    if (refType == ReferenceType.SELF_RELATIVE_POINTER){
                        ptr -= value.pointerPosition;
                    }
                    else {
                        ptr -= value.pointerBase;
                    }
                }

                int pointerSize = Integer.BYTES;

                if (value.field.isAnnotationPresent(PointerSize.class)){
                    pointerSize = value.field.getAnnotation(PointerSize.class).bytes();
                }

                writeSizedInt(ptr, null, pointerSize);
            }
            else {
                //Inline object - already written
            }

            baseStream.seek(position);
            value.children.addAll(writeInlineObject(value.value, value.field));

            for (RefValue child : value.children){
                writeRefValue(child);
            }
        }
    }

    private void writeArrayLength(int length, Field field) throws IOException {
        int size = Integer.BYTES;
        if (hasAnnotation(ArrayLengthSize.class, field)){
            size = field.getAnnotation(ArrayLengthSize.class).bytes();
        }

        writeSizedInt(length, null, size);
    }

    private void writeSizedInt(Object value, Field field) throws IOException {
        writeSizedInt(value, field, Integer.BYTES);
    }

    private void writeSizedInt(Object value, Field field, int defaultSize) throws IOException {
        int size = defaultSize;
        if (hasAnnotation(Size.class, field)) {
            size = getAnnotation(Size.class, field).bytes();
        }

        switch (size){
            case Byte.BYTES:
                baseStream.write(((Number)value).byteValue());
                break;
            case Short.BYTES:
                baseStream.writeShort(((Number)value).shortValue());
                break;
            case Integer.BYTES:
                baseStream.writeInt(((Number)value).intValue());
                break;
            default:
                throw new RuntimeException("Unsupported integer size: " + size + ".");
        }
    }

    public static class RefValue {
        public int pointerPosition;
        public int pointerBase;
        public Field field;
        public Object value;

        public List<RefValue> children = new ArrayList<>();
    }
}
