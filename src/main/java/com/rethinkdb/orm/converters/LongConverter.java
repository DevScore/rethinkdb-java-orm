package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;

import java.lang.reflect.Field;

public class LongConverter implements Converter<Long, Long>, ConverterFactory {

	@Override
	public Converter init(Field field) {
		return this;
	}

	public boolean canConvert(Class type) {
		return Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type);
	}

	@Override
	public Long fromProperty(Long property) {
		return property;
	}

	@Override
	public Long fromField(Long fieldValue) {
		return fieldValue;
	}


}
