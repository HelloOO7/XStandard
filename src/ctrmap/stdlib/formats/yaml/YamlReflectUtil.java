package ctrmap.stdlib.formats.yaml;

import ctrmap.stdlib.text.FormattingUtils;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class YamlReflectUtil {

	public static <T> T deserialize(YamlNode n, Class<T> cls) {
		try {
			T obj = cls.newInstance();
			deserializeToObject(n, obj);
			return obj;
		} catch (InstantiationException | IllegalAccessException ex) {
			Logger.getLogger(YamlReflectUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static void deserializeToObject(YamlNode n, Object obj) {
		try {
			readNodeToFields(n, obj);
		} catch (IllegalAccessException | InstantiationException ex) {
			Logger.getLogger(YamlReflectUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static void readNodeToFields(YamlNode n, Object obj) throws IllegalAccessException, InstantiationException {
		for (Field field : getSortedFields(obj.getClass())) {
			YamlNode valueNode = n.getChildByNameIgnoreCase(field.getName());
			field.set(obj, readObject(valueNode, field.getType(), field));
		}
	}

	private static List<Field> getSortedFields(Class cls) {
		List<Field> l = new ArrayList<>();

		addFields(cls, l);

		return l;
	}

	private static void addFields(Class cls, List<Field> l) {
		if (cls == null) {
			return;
		}

		addFields(cls.getSuperclass(), l);

		for (Field fld : cls.getDeclaredFields()) {
			int mod = fld.getModifiers();
			if (!Modifier.isStatic(mod) && !Modifier.isTransient(mod)) {
				fld.setAccessible(true);

				l.add(fld);
			}
		}
	}

	private static Object readObject(YamlNode node, Class type, Field fld) throws InstantiationException, IllegalAccessException {
		if (node != null) {
			if (type.isPrimitive()) {
				if (type == Double.TYPE) {
					return Double.parseDouble(node.getValue());
				} else if (type == Float.TYPE) {
					return node.getValueFloat();
				} else if (type == Long.TYPE) {
					return node.getValueLong();
				} else if (type == Integer.TYPE) {
					return node.getValueInt();
				} else if (type == Short.TYPE) {
					return (short) node.getValueInt();
				} else if (type == Byte.TYPE) {
					return (byte) node.getValueInt();
				} else if (type == Boolean.TYPE) {
					return node.getValueBool();
				} else {
					throw new UnsupportedOperationException("Unsupported primitive: " + type);
				}
			} else if (type == String.class) {
				return node.getValue();
			} else if (type.isEnum()) {
				String str = node.getValue();
				for (Object enm : type.getEnumConstants()) {
					if (((Enum) enm).name().equalsIgnoreCase(str)) {
						return enm;
					}
				}
			} else if (type.isArray()) {
				Class componentType = type.getComponentType();
				Object arr = Array.newInstance(type.getComponentType(), node.children.size());
				for (int i = 0; i < node.children.size(); i++) {
					Array.set(arr, i, readObject(node.children.get(i), componentType, fld));
				}
				return arr;
			} else if (Collection.class.isAssignableFrom(type)) {
				if (type == List.class){
					type = ArrayList.class;
				}
				Collection coll = (Collection) type.newInstance();

				Class componentType = (Class) ((ParameterizedType) fld.getGenericType()).getActualTypeArguments()[0];

				for (YamlNode nd : node.children) {
					coll.add(readObject(nd, componentType, fld));
				}

				return coll;
			} else {
				Object obj = type.newInstance();
				readNodeToFields(node, obj);
				return obj;
			}
		}
		return null;
	}

	private static YamlNode createNodeForField(Field f, Object obj) {
		try {
			return serialize(f.getName(), f.get(obj));
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			Logger.getLogger(YamlReflectUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static YamlNode serialize(String nodeName, Object obj) {
		try {
			YamlNode n = new YamlNode(new Key(nodeName));
			addFieldsToNode(n, obj);
			return n;
		} catch (IllegalArgumentException ex) {
			Logger.getLogger(YamlReflectUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static void addFieldsToNode(YamlNode n, Object obj) {
		try {
			for (Field field : getSortedFields(obj.getClass())) {
				int mods = field.getModifiers();
				if (!Modifier.isStatic(mods) && !Modifier.isTransient(mods)) {
					field.setAccessible(true);
					addValueToNode(n, field.getName(), field.getType(), field, field.get(obj));
				}
			}
		} catch (IllegalAccessException ex) {
			Logger.getLogger(YamlReflectUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static void addValueToNode(YamlNode n, String key, Class type, Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
		if (value == null) {
			return; //will be set to null during deserialization anyway, pointless to write
		}
		if (type.isPrimitive() || type.isEnum() || value == null || type == String.class) {
			if (key != null) {
				n.addChild(FormattingUtils.camelToPascal(key), value);
			} else {
				n.addChild(new YamlNode(value));
			}
		} else if (type.isArray()) {
			YamlNode list = n.addChildKey(FormattingUtils.camelToPascal(key));

			int size = Array.getLength(value);
			for (int i = 0; i < size; i++) {
				YamlNode elem = new YamlNode(new YamlListElement());
				addValueToNode(elem, null, type.getComponentType(), field, Array.get(value, i));
				list.addChild(elem);
			}
		} else if (Collection.class.isAssignableFrom(type)) {
			YamlNode list = n.addChildKey(FormattingUtils.camelToPascal(key));

			for (Object o : (Collection) value) {
				YamlNode elem = new YamlNode(new YamlListElement());
				addValueToNode(elem, null, (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0], field, o);
				list.addChild(elem);
			}
		} else {
			if (key != null) {
				n = n.addChildKey(FormattingUtils.camelToPascal(key));
			}
			addFieldsToNode(n, value);
		}
	}
}
