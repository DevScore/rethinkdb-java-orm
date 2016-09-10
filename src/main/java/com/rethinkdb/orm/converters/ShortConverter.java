package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

public class ShortConverter implements Converter<Short, Long>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return Short.class.isAssignableFrom(typeInfo.type) || short.class.isAssignableFrom(typeInfo.type);
	}

	public Short fromProperty(Long property) {

		if (property == null) {
			return null;
		}
		if (property > Short.MAX_VALUE || property < Short.MIN_VALUE) {
			throw new IllegalArgumentException("Error: DB property is out of bounds, value=" + property
					+ ". Short values must be between " + Short.MIN_VALUE + " and " + Short.MAX_VALUE);
		}
		return property.shortValue();
	}

	public Long fromField(Short fieldValue) {
		return Long.valueOf(fieldValue);
	}

}
