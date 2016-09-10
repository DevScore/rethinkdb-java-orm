package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.MapperUtils;
import com.rethinkdb.orm.TypeInfo;

@SuppressWarnings("unchecked")
public class ArrayObjectConverterFactory implements ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {

		Class componentType = typeInfo.type.getComponentType();
		TypeInfo componentTypeInfo = new TypeInfo(typeInfo.type.getAnnotations(), componentType, null, null);
		Converter converter = MapperUtils.findConverter(componentTypeInfo);

		if (converter != null) {
			return new ArraySimpleConverter<>(converter, componentType);
		}

		return new ArrayObjectConverter<>(componentType);
	}

	@Override
	public boolean canConvert(TypeInfo typeInfo) {
		return typeInfo.type.isArray() && !typeInfo.type.getComponentType().isPrimitive();
	}
}
