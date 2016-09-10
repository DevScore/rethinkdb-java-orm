package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

public class IntegerConverter implements Converter<Integer, Long>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return Integer.class.isAssignableFrom(typeInfo.type) || int.class.isAssignableFrom(typeInfo.type);
	}

	public Integer fromProperty(Long property) {

		if (property == null) {
			return null;
		}
		if (property > Integer.MAX_VALUE || property < Integer.MIN_VALUE) {
			throw new IllegalArgumentException("Error: DB property is out of bounds, value=" + property
					+ ". Integer values must be between " + Integer.MIN_VALUE + " and " + Integer.MAX_VALUE);
		}
		return property.intValue();
	}

	public Long fromField(Integer fieldValue) {
		return Long.valueOf(fieldValue);
	}

}
