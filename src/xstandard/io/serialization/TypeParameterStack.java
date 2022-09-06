package xstandard.io.serialization;

import xstandard.io.IOCommon;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class TypeParameterStack extends Stack<List<TypeParameterStack.Element>> {

	public void pushTPS() {
		push(new ArrayList<>());
	}

	public void popTPS() {
		pop();
	}

	private List<Element> getParentElements(List<Element> elems) {
		return get(Math.max(0, indexOf(elems) - 1));
	}

	private List<Element> getCurrentElements() {
		return peek();
	}

	public void importFieldType(Field field) {
		if (field == null) {
			return;
		}
		Type type = resolveType(field.getGenericType());

		importType(type);
	}

	private void importType(Type type) {
		if (type instanceof ParameterizedType) {
			IOCommon.debugPrint("Parameterized type " + type);
			List<Element> elems = getCurrentElements();

			ParameterizedType pt = (ParameterizedType) type;                     //List<String> l = ...

			Type[] typeArguments = pt.getActualTypeArguments();                  //{java.lang.String}
			Type[] typeParameters = ((Class) pt.getRawType()).getTypeParameters(); //class List<E>

			for (int i = 0; i < typeArguments.length; i++) {
				String name = typeParameters[i].getTypeName(); //E
				Type typeArg = typeArguments[i];                //java.lang.String

				Element elem = new Element();
				elem.parameterName = name;
				elem.type = typeArg;
				IOCommon.debugPrint("Imported parameterized type " + name + " as type " + typeArg);
				elems.add(elem);
				importType(typeArg);
			}
		}
	}

	@Override
	public int indexOf(Object obj, int startIndex) {
		for (int i = 0; i < size(); i++) {
			if (elementData[i] == obj) {
				return i;
			}
		}
		return -1;
	}

	public Type resolveType(Type gType) {
		if (gType instanceof TypeVariable) {
			String desiredName = gType.getTypeName();

			List<Element> elems = getParentElements(getCurrentElements());

			Type outType = gType;

			while (outType instanceof TypeVariable) {
				boolean found = false;

				for (Element e : elems) {
					if (e.parameterName.equals(desiredName)) {
						//IOCommon.debugPrint("Resolved TypeVariable " + desiredName + " to " + e.type);
						outType = e.type;
						found = true;
						break;
					}
				}

				if (!found) {
					System.err.println("---- ELEMENT DUMP ----");
					System.err.println("CURRENT LEVEL " + indexOf(peek()));
					for (int i = 0; i < size(); i++) {
						System.err.println("LEVEL " + i);
						for (Element e : get(i)) {
							System.err.println("Available elem " + e.parameterName + " type " + e.type);
						}
					}
					throw new RuntimeException("Could not resolve inherited TypeVariable " + desiredName + " of type " + gType);
				} else {
					elems = getParentElements(elems);
				}
			}
			//IOCommon.debugPrint("OutType " + outType);

			return outType;
		} else {
			return gType;
		}
	}

	public static class Element {

		public String parameterName;
		public Type type;
	}
}
