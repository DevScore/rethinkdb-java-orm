package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class ArrayFloatConverter implements Converter<float[], List<Double>>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return float[].class.equals(typeInfo.type) && float.class == typeInfo.type.getComponentType();
	}

	public float[] fromProperty(List<Double> list) {

		float[] array = new float[list.size()];

		for (int i = 0; i < list.size(); i++) {
			// dirty workaround, because RDB returns n.0 as Long n
			Object item = list.get(i);
			if (item.getClass() == Long.class) {
				array[i] = ((Long) item).floatValue();
			} else {
				array[i] = ((Double) item).floatValue();
			}		}

		return array;
	}

	public List<Double> fromField(float[] fieldValue) {

		List<Double> list = new ArrayList<>(fieldValue.length);
		for (float val : fieldValue) {
			list.add((double) val);
		}
		return list;
	}

}
