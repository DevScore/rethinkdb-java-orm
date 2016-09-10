package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

public class FloatConverter implements Converter<Float, Object>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return Float.class.isAssignableFrom(typeInfo.type) || float.class.isAssignableFrom(typeInfo.type);
	}

	public Float fromProperty(Object inProperty) {
		if (inProperty == null) {
			return null;
		}

		Float property;
		if (inProperty.getClass() == Long.class) {
			property = ((Long) inProperty).floatValue();
		} else if (inProperty.getClass() == Double.class) {
			property = ((Double) inProperty).floatValue();
		} else {
			property = (Float) inProperty;
		}

		if (property > Float.MAX_VALUE || property < -Float.MAX_VALUE) {
			throw new IllegalArgumentException("Error: DB property is out of bounds, value=" + property
					+ ". Float values must be between " + Float.MIN_VALUE + " and " + Float.MAX_VALUE);
		}
		return property;
	}

	public Double fromField(Float fieldValue) {
		if (fieldValue > DB_MAX_VAL || fieldValue < DB_MIN_VAL) {
			throw new IllegalArgumentException("Error: Field value is out of bounds, value=" + fieldValue
					+ ". DB numeric values must be between " + DB_MIN_VAL + " and " + DB_MAX_VAL);
		}
		double doubleValue = fieldValue.doubleValue();
		return doubleValue;
	}

}
