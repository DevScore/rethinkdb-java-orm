package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class ArrayDoubleConverter implements Converter<double[], List<Double>>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return double[].class.equals(typeInfo.type) && double.class == typeInfo.type.getComponentType();
	}

	public double[] fromProperty(List<Double> list) {

		double[] array = new double[list.size()];

		for (int i = 0; i < list.size(); i++) {

			// dirty workaround, because RDB returns n.0 as Long n
			Object item = list.get(i);
			if (item.getClass() == Long.class) {
				array[i] = ((Long) item).floatValue();
			} else {
				array[i] = (Double) item;
			}
		}

		return array;
	}

	public List<Double> fromField(double[] fieldValue) {

		List<Double> list = new ArrayList<>(fieldValue.length);
		for (double val : fieldValue) {
			list.add(val);
		}
		return list;
	}

}
