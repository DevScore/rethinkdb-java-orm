package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

public class StringConverter implements Converter<String, String>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return String.class.isAssignableFrom(typeInfo.type);
	}

	@Override
	public String fromProperty(String property) {
		return property;
	}

	@Override
	public String fromField(String fieldValue) {
		return fieldValue;
	}


}
