package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.*;

import java.lang.annotation.Annotation;
import java.util.Map;

public class MapConverterFactory implements ConverterFactory {

	@SuppressWarnings("unchecked")
	@Override
	public Converter init(TypeInfo typeInfo) {
		if (typeInfo == null) {
			return null;
		}
		Class keyType = (Class) TypeUtils.getMapKeyType(typeInfo.type, typeInfo.genericType);
		Class valueType = (Class) TypeUtils.getMapValueType(typeInfo.type, typeInfo.genericType);
		Annotation[] valueAnnotations = TypeUtils.getMapValueAnnotations(typeInfo.type, typeInfo.annotatedType);
		TypeInfo valueTypeInfo = new TypeInfo(valueAnnotations, valueType, null, null);
		Converter converter = MapperUtils.findConverter(valueTypeInfo);

		if (keyType == null || keyType != String.class) {
			throw new IllegalArgumentException("Error: unsupported Map generic key argument type '" + keyType.getSimpleName() + "'"
					+ " Supported key type is String. " +
					"Maps must be of type Map<String, T> where T is a type supported by the built in converters.");
		}

		if (valueType == null) {
			throw new IllegalArgumentException("Error: Maps without generic type arguments are unsupported: " +
					"Maps must be of type Map<String, T> where T is a type supported by the built in converters.");
		}

		if (converter != null) {
			return new MapSimpleConverter<>(converter);
		}

		return new MapObjectConverter(valueType);
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return Map.class.isAssignableFrom(typeInfo.type);
	}

}
