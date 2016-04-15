package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;

import java.lang.reflect.Field;
import java.util.Set;

@SuppressWarnings("unchecked")
public class SetConverterFactory implements ConverterFactory {


	@Override
	public Converter init(Field field) {
		return new SetConverter<>((Class<? extends Set>) field.getType());
	}

	@Override
	public boolean canConvert(Class type) {
		return Set.class.isAssignableFrom(type);
	}
}
