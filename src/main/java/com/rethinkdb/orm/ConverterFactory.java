package com.rethinkdb.orm;

public interface ConverterFactory {

	Converter init(TypeInfo typeInfo);

	boolean canConvert(TypeInfo typeInfo);
}
