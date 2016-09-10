package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

public class PassThroughConverter implements Converter<Object, Object>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	/**
	 * Designed to only be used manually, not via class/field introspection
	 * @param typeInfo The type to check.
	 * @return Always returns false, so it can not be discovered via field mapping process
	 */
	public boolean canConvert(TypeInfo typeInfo) {
		return false;  // always returns false, so it can not be discovered via field mapping process
	}

	public Object fromProperty(Object property) {
		return property;
	}

	public Object fromField(Object fieldValue) {
		return fieldValue;
	}

}
