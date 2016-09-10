package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

public class ByteConverter implements Converter<Byte, Long>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return Byte.class.isAssignableFrom(typeInfo.type) || byte.class.isAssignableFrom(typeInfo.type);
	}

	public Byte fromProperty(Long property) {
		if (property == null) {
			return null;
		}
		if (property > Byte.MAX_VALUE || property < Byte.MIN_VALUE) {
			throw new IllegalArgumentException("Error: DB property is out of bounds, value=" + property
					+ ". Byte values must be between " + Byte.MIN_VALUE + " and " + Byte.MAX_VALUE);
		}
		return property.byteValue();
	}

	public Long fromField(Byte fieldValue) {
		return Long.valueOf(fieldValue);
	}

}
