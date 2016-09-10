package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.RdbException;
import com.rethinkdb.orm.TypeInfo;
import com.rethinkdb.orm.annotations.Binary;

import static com.rethinkdb.RethinkDB.r;

public class BinaryConverter implements Converter<byte[], Object>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	@Override
	public boolean canConvert(TypeInfo typeInfo) {
		return typeInfo.annotations != null
				&& typeInfo.hasAnnotation(Binary.class)
				&& byte[].class.equals(typeInfo.type);
	}

	@Override
	public byte[] fromProperty(Object property) {
		if (property == null) {
			return null;
		}

		if (!byte[].class.equals(property.getClass())) {
			throw new RdbException(RdbException.Error.UnexpectedPropertyType, "Unexpected property type: " +
					"expected byte[], got " + property.getClass());
		}

		return (byte[]) property;
	}

	@Override
	public Object fromField(byte[] fieldValue) {
		return r.binary(fieldValue);
	}
}
