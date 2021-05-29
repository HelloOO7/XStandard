package ctrmap.stdlib.io.serialization;

enum FieldTypeGroup {
    PRIMITIVE,
    ARRAY,
    ENUM,
    OBJECT;

    public static FieldTypeGroup getTypeGroup(Class cls){
        if (cls.isPrimitive()){
            return PRIMITIVE;
        }
        else if (cls.isArray()){
            return ARRAY;
        }
        else if (cls.isEnum()){
            return ENUM;
        }
        return OBJECT;
    }
}
