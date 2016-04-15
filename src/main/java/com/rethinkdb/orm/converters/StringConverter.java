package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;

import java.lang.reflect.Field;

public class StringConverter implements Converter<String, String>, ConverterFactory {

	@Override
	public Converter init(Field field) {
		return this;
	}

	public boolean canConvert(Class type) {
		return String.class.isAssignableFrom(type);
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
