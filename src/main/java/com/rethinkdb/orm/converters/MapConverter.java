package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

import java.util.Map;

public class MapConverter implements Converter<Map, Map<String, Object>>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return Map.class.isAssignableFrom(typeInfo.type);
	}

	public Map fromProperty(Map<String, Object> properties) {
		return properties;
	}

	public Map<String, Object> fromField(Map fieldValue) {
		return fieldValue;
	}

}
