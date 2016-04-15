package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;

import java.lang.reflect.Field;

public class ShortConverter implements Converter<Short, Long>, ConverterFactory {

	@Override
	public Converter init(Field field) {
		return this;
	}

	public boolean canConvert(Class type) {
		return Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type);
	}

	public Short fromProperty(Long property) {
		return property == null ? null : property.shortValue();
	}

	public Long fromField(Short fieldValue) {
		return Long.valueOf(fieldValue);
	}

}
