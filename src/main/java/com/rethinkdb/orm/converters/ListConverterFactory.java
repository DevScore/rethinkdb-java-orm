package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.*;

import java.lang.annotation.Annotation;
import java.util.List;

public class ListConverterFactory implements ConverterFactory {

	@SuppressWarnings("unchecked")
	@Override
	public Converter init(TypeInfo typeInfo) {

		if (typeInfo == null) {
			return null;
		}

		Class componentType = (Class) TypeUtils.getCollectionValueType(typeInfo.type, typeInfo.genericType);
		Annotation[] componentAnnotations = TypeUtils.getListComponentAnnotations(typeInfo.type, typeInfo.annotatedType);

		if (componentType == null) {
			throw new IllegalArgumentException("Error: cannot derive generic argument type: " +
					"Field '" + typeInfo.type.getSimpleName() + " " + typeInfo.type.getName() +
					"' in class " + typeInfo.type.getDeclaringClass().getSimpleName()
					+ " is missing concrete generic argument type, e.g. List<String> or List<T>," +
					" where T is a type supported by built-in converters.");
		}

		TypeInfo componentTypeInfo = new TypeInfo(componentAnnotations, componentType, null, null);
		Converter converter = MapperUtils.findConverter(componentTypeInfo);

		if (converter != null) {
			return new ListSimpleConverter<>(converter, componentType);
		}

		return new ListObjectConverter(componentType);
	}

	@Override
	public boolean canConvert(TypeInfo typeInfo) {
		return List.class.isAssignableFrom(typeInfo.type);
	}

}
