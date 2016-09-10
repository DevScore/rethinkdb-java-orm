package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;
import com.rethinkdb.orm.annotations.Timestamp;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TimestampConverter implements Converter<Long, OffsetDateTime>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	@Override
	public boolean canConvert(TypeInfo typeInfo) {
		return typeInfo.annotations != null
				&& typeInfo.hasAnnotation(Timestamp.class)
				&& (Long.class.isAssignableFrom(typeInfo.type) || long.class.isAssignableFrom(typeInfo.type));
	}

	@Override
	public Long fromProperty(OffsetDateTime property) {
		if (property == null) {
			return null;
		}
		if (property.getYear() < 1400 || property.getYear() > 9999) {
			throw new IllegalArgumentException("Error: date is out of bounds. " +
					"Year must be between 1400 and 9999. Date:" + property.toString());
		}
		return property.toInstant().toEpochMilli();
	}

	@Override
	public OffsetDateTime fromField(Long epochMillis) {
		// since epoch time does not carry timezone info, we assign it to UTC
		return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
	}
}
