package ctrmap.stdlib.io.serialization;

import ctrmap.stdlib.io.serialization.annotations.Size;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class BinarySerialization {
	
	public int fileVersion;
	
    protected static List<Field> getSortedFields(Class cls){
        List<Field> l = new ArrayList<>();

        addFields(cls, l);

        return l;
    }

    protected static void addFields(Class cls, List<Field> l){
        if (cls == null){
            return;
        }

        addFields(cls.getSuperclass(), l);

        for (Field fld : cls.getDeclaredFields()){
            int mod = fld.getModifiers();
            if (!Modifier.isStatic(mod)) {
                fld.setAccessible(true);

                l.add(fld);
            }
        }
    }

    protected static int getIntSize(int defaultSize, AnnotatedElement... elems){
        int size = defaultSize;
        for (AnnotatedElement elem : elems) {
            if (hasAnnotation(Size.class, elem)) {
                size = getAnnotation(Size.class, elem).value();
                break;
            }
        }
        return size;
    }

    protected static boolean hasAnnotation(Class<? extends Annotation> annot, AnnotatedElement... elems){
        for (AnnotatedElement e : elems){
            if (e != null) {
                if (e instanceof Field) {
                    Field fld = (Field) e;
                    if (fld.isAnnotationPresent(annot) || fld.getType().isAnnotationPresent(annot)){
                        return true;
                    }
                }
                else {
                    if (e.isAnnotationPresent(annot)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected static <T extends Annotation> T getAnnotation(Class<T> annot, AnnotatedElement... elems){
        for (AnnotatedElement e : elems){
            if (e != null) {
                if (e instanceof Field) {
                    Field fld = (Field) e;
                    if (fld.isAnnotationPresent(annot)){
                        return fld.getAnnotation(annot);
                    }
                    else {
                        return fld.getType().getAnnotation(annot);
                    }
                }
                else {
                    if (e.isAnnotationPresent(annot)){
                        return e.getAnnotation(annot);
                    }
                }
            }
        }
        return null;
    }

    protected static Class getUnboxedClass(Class clazz) {
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
        } else if (clazz == Character.class){
            return Character.TYPE;
        }
        return clazz;
    }
}
