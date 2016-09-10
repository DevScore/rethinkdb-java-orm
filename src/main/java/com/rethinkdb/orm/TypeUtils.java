package com.rethinkdb.orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypeUtils {

	/**
	 * Returns the erasure of the given type.
	 *
	 * @param type class type
	 * @return erased class
	 */
	public static Class<?> erase(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		} else if (type instanceof TypeVariable) {
			TypeVariable<?> tv = (TypeVariable<?>) type;
			if (tv.getBounds().length == 0)
				return Object.class;
			else
				return erase(tv.getBounds()[0]);
		} else {
			throw new RuntimeException("not supported: " + type.getClass());
		}
	}

	public static Type getMapKeyType(Field field) {
		Type[] mapTypes = getMapTypes(field);
		return mapTypes == null ? null : mapTypes[0];
	}

	public static Type getMapValueType(Field field) {
		Type[] mapTypes = getMapTypes(field);
		return mapTypes == null ? null : mapTypes[1];
	}

	public static Type[] getMapTypes(Field field) {
		Class type = field.getType();
		if (Map.class.isAssignableFrom(type)) {
			return getMapTypes(type, field.getGenericType());
		}
		return null;
	}

	public static Type getMapKeyType(Class type, Type annotatedType) {
		if (Map.class.isAssignableFrom(type)) {
			Type[] mapTypes = getMapTypes(type, annotatedType);
			return mapTypes == null ? null : mapTypes[0];
		} else {
			return null;
		}
	}

	public static Type getMapValueType(Class type, Type annotatedType) {
		if (Map.class.isAssignableFrom(type)) {
			Type[] mapTypes = getMapTypes(type, annotatedType);
			return mapTypes == null ? null : mapTypes[1];
		} else {
			return null;
		}
	}

	public static Type[] getMapTypes(Class type, Type annotatedType) {
		if (annotatedType instanceof ParameterizedType) {
			ParameterizedType mapType = (ParameterizedType) annotatedType;
			return mapType.getActualTypeArguments();
		}
		return null;
	}

	public static Type getListValueType(Field field) {
		Class type = field.getType();
		if (List.class.isAssignableFrom(type)) {
			return getCollectionValueType(type, field.getGenericType());
		} else {
			return null;
		}
	}

	public static Type getCollectionValueType(Field field) {
		Class type = field.getType();
		if (Collection.class.isAssignableFrom(type)) {
			return getCollectionValueType(type, field.getGenericType());
		} else {
			return null;
		}
	}

	public static Type getCollectionValueType(Class type, Type genericType) {
		if (genericType instanceof ParameterizedType) {
			return ((ParameterizedType) genericType).getActualTypeArguments()[0];
		}
		return null;
	}

	public static Annotation[] getMapKeyAnnotations(Class type, AnnotatedType annotatedType) {
		if (Map.class.isAssignableFrom(type)) {
			AnnotatedType[] mapTypes = getParamAnnotations(annotatedType);
			return mapTypes == null ? null : mapTypes[0].getAnnotations();
		} else {
			return null;
		}
	}

	public static Annotation[] getMapValueAnnotations(Class type, AnnotatedType annotatedType) {
		if (Map.class.isAssignableFrom(type)) {
			AnnotatedType[] mapTypes = getParamAnnotations(annotatedType);
			return mapTypes == null ? null : mapTypes[1].getAnnotations();
		} else {
			return null;
		}
	}

	private static AnnotatedType[] getParamAnnotations(AnnotatedType annotatedType) {
		if (annotatedType instanceof AnnotatedParameterizedType) {
			AnnotatedParameterizedType apType = (AnnotatedParameterizedType) annotatedType;
			AnnotatedType[] apTypeAnno = apType.getAnnotatedActualTypeArguments();

			for (int i = 0; i < apTypeAnno.length; i++) {
				AnnotatedType apTypeAnnoItem = apTypeAnno[i];
				// if it's an array - we need to go one deeper
				if (apTypeAnnoItem instanceof AnnotatedArrayType) {
					apTypeAnno[i] = ((AnnotatedArrayType) apTypeAnnoItem).getAnnotatedGenericComponentType();
				}
			}
			return apTypeAnno;
		}
		return null;
	}

	public static Annotation[] getListComponentAnnotations(Class type, AnnotatedType annotatedType) {
		if (List.class.isAssignableFrom(type)) {
			AnnotatedType[] listAnnotations = getParamAnnotations(annotatedType);
			return listAnnotations == null ? null : listAnnotations[0].getAnnotations();
		} else {
			return null;
		}
	}

	public static Annotation[] getSetComponentAnnotations(Class type, AnnotatedType annotatedType) {
		if (Set.class.isAssignableFrom(type)) {
			AnnotatedType[] listAnnotations = getParamAnnotations(annotatedType);
			return listAnnotations == null ? null : listAnnotations[0].getAnnotations();
		} else {
			return null;
		}
	}

	public static Class returnBoxedType(Class primitiveType) {
		if (primitiveType == float.class) {
			return Float.class;
		} else if (primitiveType == double.class) {
			return Double.class;
		} else if (primitiveType == int.class) {
			return Integer.class;
		} else if (primitiveType == long.class) {
			return Long.class;
		} else if (primitiveType == boolean.class) {
			return Boolean.class;
		} else if (primitiveType == byte.class) {
			return Byte.class;
		} else if (primitiveType == short.class) {
			return Short.class;
		} else if (primitiveType == char.class) {
			return Character.class;
		} else {
			throw new IllegalStateException("Unknown primitive type: " + primitiveType);
		}
	}

//	public static <T> T coerceToType(Class<T> type, Object object) {
//
//		if (type == Double.class || type == Float.class) {
//			if (object.getClass() == Long.class) {
//				return (T) Double.valueOf(((Long) object).doubleValue());
//			}
//		} else if (type == Byte.class) {
//			if (object.getClass() == Long.class) {
//				return (T) Byte.valueOf(((Long) object).byteValue());
//			}
//		} else if (type == Short.class) {
//			if (object.getClass() == Long.class) {
//				return (T) Short.valueOf(((Long) object).shortValue());
//			}
//		} else if (type == Integer.class) {
//			if (object.getClass() == Long.class) {
//				return (T) Integer.valueOf(((Long) object).intValue());
//			}
//		}
//		return (T) object;
//	}
}
