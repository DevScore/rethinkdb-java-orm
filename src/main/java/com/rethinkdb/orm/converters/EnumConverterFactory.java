package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

public class EnumConverterFactory implements ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		if (typeInfo == null) {
			return null;
		}
		return new EnumConverter(typeInfo.type);
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return Enum.class.isAssignableFrom(typeInfo.type);
	}


}
