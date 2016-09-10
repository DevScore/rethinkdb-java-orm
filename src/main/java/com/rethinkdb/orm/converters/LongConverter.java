package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

public class LongConverter implements Converter<Long, Long>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return Long.class.isAssignableFrom(typeInfo.type) || long.class.isAssignableFrom(typeInfo.type);
	}

	@Override
	public Long fromProperty(Long property) {
		return property;
	}

	@Override
	public Long fromField(Long fieldValue) {

		if(fieldValue > DB_MAX_VAL || fieldValue < DB_MIN_VAL){
			throw new IllegalArgumentException("Error: Field value is out of bounds, value=" + fieldValue
					+ ". DB numeric values must be between " + DB_MIN_VAL + " and " + DB_MAX_VAL);
		}
		return fieldValue;
	}
}
