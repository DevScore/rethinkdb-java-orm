package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public class DateConverter implements Converter<Date, OffsetDateTime>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return Date.class.isAssignableFrom(typeInfo.type);
	}

	public Date fromProperty(OffsetDateTime property) {
		if (property == null) {
			return null;
		}

		if (property.getYear() < 1400 || property.getYear() > 9999) {
			throw new IllegalArgumentException("Error: date is out of bounds. " +
					"Year must be between 1400 and 9999. Date:" + property.toString());
		}
		return Date.from(property.toInstant());
	}

	public OffsetDateTime fromField(Date date) {
		return OffsetDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
	}
}
