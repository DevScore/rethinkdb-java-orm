package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

import java.util.Collection;

@SuppressWarnings("unchecked")
public class EmbeddedObjectConverterFactory implements ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		if (typeInfo == null) {
			return null;
		}
		return new EmbeddedObjectConverter(typeInfo.type);
	}

	@Override
	public boolean canConvert(TypeInfo typeInfo) {

		// any object as long as it is not array or Collection (those are handled by other converters)
		return !typeInfo.type.isArray() && !Collection.class.isAssignableFrom(typeInfo.type);
	}
}
