package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

public class BooleanConverter implements Converter<Boolean, Boolean>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return Boolean.class.isAssignableFrom(typeInfo.type) || boolean.class.isAssignableFrom(typeInfo.type);
	}

	public Boolean fromProperty(Boolean inProperty) {
		return inProperty;
	}

	public Boolean fromField(Boolean fieldValue) {
		return fieldValue;
	}

}
