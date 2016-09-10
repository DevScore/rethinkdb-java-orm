package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.RdbException;
import com.rethinkdb.orm.TypeInfo;
import com.rethinkdb.orm.types.Hash;

import static com.rethinkdb.RethinkDB.r;

public class HashConverter implements Converter<Hash, Object>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	@Override
	public boolean canConvert(TypeInfo typeInfo) {
		return Hash.class.equals(typeInfo.type);
	}

	@Override
	public Hash fromProperty(Object property) {
		if (property == null) {
			return null;
		}

		if (!byte[].class.equals(property.getClass())) {
			throw new RdbException(RdbException.Error.UnexpectedPropertyType, "Unexpected property type: " +
					"expected byte[], got " + property.getClass());
		}

		return Hash.wrap((byte[]) property);
	}

	@Override
	public Object fromField(Hash fieldValue) {
		return r.binary(fieldValue.getHash());
	}
}
