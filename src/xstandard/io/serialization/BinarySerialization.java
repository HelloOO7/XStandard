package xstandard.io.serialization;

import xstandard.io.base.iface.IOStream;
import xstandard.io.serialization.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BinarySerialization {

	public SerializationIOStream baseStream;

	protected final ReferenceType refType;

	protected final DecimalType decimalType;

	public int fileVersion;

	protected BinarySerialization(IOStream baseStream, ReferenceType refType, DecimalType decimalType) {
		this.baseStream = new SerializationIOStream(baseStream);
		this.refType = refType;
		this.decimalType = decimalType;
	}

	public static List<Field> getSortedFields(Class cls) {
		List<Field> l = new ArrayList<>();

		addFields(cls, l);

		return l;
	}

	protected static void addFields(Class cls, List<Field> l) {
		if (cls == null) {
			return;
		}

		addFields(cls.getSuperclass(), l);

		for (Field fld : cls.getDeclaredFields()) {
			int mod = fld.getModifiers();
			if (!Modifier.isStatic(mod)) {
				fld.setAccessible(true);

				l.add(fld);
			}
		}
	}

	protected static int getIntSize(int defaultSize, AnnotatedElement... elems) {
		int size = defaultSize;
		for (AnnotatedElement elem : elems) {
			if (hasAnnotation(Size.class, elem)) {
				size = getAnnotation(Size.class, elem).value();
				break;
			}
		}
		return size;
	}

	protected static Class getNumberClassForSize(int size) {
		switch (size) {
			case Long.BYTES:
				return Long.TYPE;
			case Integer.BYTES:
				return Integer.TYPE;
			case Short.BYTES:
				return Short.TYPE;
			case Byte.BYTES:
				return Byte.TYPE;
		}
		throw new RuntimeException("Invalid number type size: " + size);
	}

	protected static boolean getIsClassNeedsSize(Class cls, AnnotatedElement... ant) {
		boolean obj_NeedsSize = false;
		boolean allowArray = !(hasAnnotation(DefinedArraySize.class, ant) || hasAnnotation(ArraySize.class, ant));
		if (Collection.class.isAssignableFrom(cls) && allowArray) {
			obj_NeedsSize = true;
		} else if (cls == String.class && hasAnnotation(LengthPos.class, ant) && !hasAnnotation(Size.class, ant)) {
			obj_NeedsSize = true;
		} else if (cls.isArray() && allowArray) {
			obj_NeedsSize = true;
		}
		return obj_NeedsSize;
	}

	protected static LengthPos.LengthPosType getLengthPos(AnnotatedElement... ant) {
		LengthPos.LengthPosType lp = LengthPos.LengthPosType.AS_FIELD;

		if (hasAnnotation(LengthPos.class, ant)) {
			lp = getAnnotation(LengthPos.class, ant).value();
		}
		return lp;
	}

	protected static boolean hasAnnotation(Class<? extends Annotation> annot, AnnotatedElement... elems) {
		for (AnnotatedElement e : elems) {
			if (e != null) {
				if (e instanceof Field) {
					Field fld = (Field) e;
					if (fld.isAnnotationPresent(annot) || fld.getType().isAnnotationPresent(annot)) {
						return true;
					}
				} else {
					if (e.isAnnotationPresent(annot)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	protected static <T extends Annotation> T getAnnotation(Class<T> annot, AnnotatedElement... elems) {
		for (AnnotatedElement e : elems) {
			if (e != null) {
				if (e instanceof Field) {
					Field fld = (Field) e;
					if (fld.isAnnotationPresent(annot)) {
						return fld.getAnnotation(annot);
					} else if (fld.getType().isAnnotationPresent(annot)) {
						return fld.getType().getAnnotation(annot);
					}
				} else {
					if (e.isAnnotationPresent(annot)) {
						return e.getAnnotation(annot);
					}
				}
			}
		}
		return null;
	}

	public static Class getUnboxedClass(Class clazz) {
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
		} else if (clazz == Character.class) {
			return Character.TYPE;
		}
		return clazz;
	}
	
	protected static int getDefaultEnumSize(Class cls) {
		Object[] constants = cls.getEnumConstants();
		return constants.length <= 0x100 ? 1 : constants.length <= 0x10000 ? 2 : 4;
	}

	protected static boolean isBitfield(Field field) {
		return field.isAnnotationPresent(BitField.class);
	}

	protected static boolean isBitfieldStart(Field field) {
		return field.getAnnotation(BitField.class).startBit() == 0;
	}
}
