package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;

import java.lang.reflect.Field;

public class DoubleConverter implements Converter<Double, Object>, ConverterFactory {

	@Override
	public Converter init(Field field) {
		return this;
	}

	public boolean canConvert(Class type) {
		return Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type);
	}

	public Double fromProperty(Object property) {
		if (property == null) {
			return null;
		} else if (property instanceof Double) {
			return (Double) property;
		} else if (property instanceof Long) {
			return Double.longBitsToDouble((Long) property);
		} else {
			throw new IllegalArgumentException("Fields of type 'double' can only be mapped to DB values of Long or Double.");
		}
	}

	public Object fromField(Double fieldValue) {

		return Double.doubleToLongBits(fieldValue); // return Long - the old, pre-3.6.0 way of converting Double to Long
	}

}
