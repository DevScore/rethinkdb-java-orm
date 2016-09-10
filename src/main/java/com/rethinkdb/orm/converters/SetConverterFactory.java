package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.*;

import java.lang.annotation.Annotation;
import java.util.Set;

@SuppressWarnings("unchecked")
public class SetConverterFactory implements ConverterFactory {


	@Override
	public Converter init(TypeInfo typeInfo) {
		if (typeInfo == null) {
			return null;
		}

		Class componentType = (Class) TypeUtils.getCollectionValueType(typeInfo.type, typeInfo.genericType);
		Annotation[] componentAnnotations = TypeUtils.getSetComponentAnnotations(typeInfo.type, typeInfo.annotatedType);

		if (componentType == null) {
			throw new IllegalArgumentException("Error: cannot derive generic argument type: " +
					"Field '" + typeInfo.type.getSimpleName() + " " + typeInfo.type.getName() +
					"' in class " + typeInfo.type.getDeclaringClass().getSimpleName()
					+ " is missing concrete generic argument type, e.g. Set<String> or Set<T>," +
					" where T is a type supported by built-in converters.");
		}

		TypeInfo componentTypeInfo = new TypeInfo(componentAnnotations, componentType, null, null);
		Converter converter = MapperUtils.findConverter(componentTypeInfo);

		if (converter != null) {
			return new SetSimpleConverter<>(converter, componentType);
		}

		return new SetObjectConverter(componentType);
	}

	@Override
	public boolean canConvert(TypeInfo typeInfo) {
		return Set.class.isAssignableFrom(typeInfo.type);
	}
}
