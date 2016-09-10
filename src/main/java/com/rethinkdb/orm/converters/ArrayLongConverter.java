package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class ArrayLongConverter implements Converter<long[], List<Long>>, ConverterFactory{

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return long[].class.equals(typeInfo.type) && long.class == typeInfo.type.getComponentType();
	}

	public long[] fromProperty(List<Long> list) {

		long[] array = new long[list.size()];

		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}

		return array;
	}

	public List<Long> fromField(long[] fieldValue) {

		List<Long> list = new ArrayList<>(fieldValue.length);
		for (long val : fieldValue) {
			list.add(val);
		}
		return list;
	}

}
