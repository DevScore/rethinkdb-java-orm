package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;

import java.lang.reflect.Field;

public class ByteConverter implements Converter<Byte, Long>, ConverterFactory {

	@Override
	public Converter init(Field field) {
		return this;
	}

	public boolean canConvert(Class type) {
		return Byte.class.isAssignableFrom(type) || byte.class.isAssignableFrom(type);
	}

	public Byte fromProperty(Long property) {
		return property == null ? null : property.byteValue();
	}

	public Long fromField(Byte fieldValue) {
		return Long.valueOf(fieldValue);
	}

}
