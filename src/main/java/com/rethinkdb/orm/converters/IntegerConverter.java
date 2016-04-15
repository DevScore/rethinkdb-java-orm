package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;

import java.lang.reflect.Field;

public class IntegerConverter implements Converter<Integer, Long>, ConverterFactory {

	@Override
	public Converter init(Field field) {
		return this;
	}

	public boolean canConvert(Class type) {
		return Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type);
	}

	public Integer fromProperty(Long property) {
		return property == null ? null : property.intValue();
	}

	public Long fromField(Integer fieldValue) {
		return Long.valueOf(fieldValue);
	}

}
