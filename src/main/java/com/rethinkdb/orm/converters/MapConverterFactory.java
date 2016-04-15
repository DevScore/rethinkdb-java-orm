package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeUtils;
import com.rethinkdb.orm.annotations.AsJson;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;

public class MapConverterFactory implements ConverterFactory {

	@SuppressWarnings("unchecked")
	@Override
	public Converter init(Field field) {
		Type valueType = TypeUtils.getMapValueType(field);

//		if (valueType != null && valueType instanceof Class) {
//			Class classType = (Class) valueType;
//			if (classType.isAnnotationPresent(AsJson.class)) {
//				Class fieldType = field.getType();
//				return new MapJsonConverter(fieldType, classType);
//			}
//		}

		return new MapConverter();
	}

	public boolean canConvert(Class type) {
		return Map.class.isAssignableFrom(type);
	}

}
