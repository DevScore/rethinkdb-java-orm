package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

import java.time.OffsetDateTime;

public class OffsetDateConverter implements Converter<OffsetDateTime, OffsetDateTime>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return OffsetDateTime.class.isAssignableFrom(typeInfo.type);
	}

	@Override
	public OffsetDateTime fromProperty(OffsetDateTime property) {
		if (property == null) {
			return null;
		}
		if (property.getYear() < 1400 || property.getYear() > 9999) {
			throw new IllegalArgumentException("Error: date is out of bounds. " +
					"Year must be between 1400 and 9999. Date:" + property.toString());
		}
		return property;
	}

	@Override
	public OffsetDateTime fromField(OffsetDateTime fieldValue) {
		return fieldValue;
	}


}
