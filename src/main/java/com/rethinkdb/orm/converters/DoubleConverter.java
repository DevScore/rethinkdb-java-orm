package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

public class DoubleConverter implements Converter<Double, Object>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return Double.class.isAssignableFrom(typeInfo.type) || double.class.isAssignableFrom(typeInfo.type);
	}

	public Double fromProperty(Object inProperty) {
		if (inProperty == null) {
			return null;
		}

		Double property;
		if (inProperty.getClass() == Long.class) {
			return ((Long) inProperty).doubleValue();
		} else {
			return (Double) inProperty;
		}
	}

	public Double fromField(Double fieldValue) {

		if(fieldValue > DB_MAX_VAL || fieldValue < DB_MIN_VAL){
			throw new IllegalArgumentException("Error: Field value is out of bounds, value=" + fieldValue
					+ ". DB numeric values must be between " + DB_MIN_VAL + " and " + DB_MAX_VAL);
		}

		return fieldValue;
	}

}
