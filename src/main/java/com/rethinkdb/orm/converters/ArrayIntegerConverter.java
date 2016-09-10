package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class ArrayIntegerConverter implements Converter<int[], List<Long>>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return int[].class.equals(typeInfo.type) && int.class == typeInfo.type.getComponentType();
	}

	public int[] fromProperty(List<Long> list) {

		int[] array = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i).intValue();
		}
		return array;
	}

	public List<Long> fromField(int[] fieldValue) {

		List<Long> list = new ArrayList<>(fieldValue.length);
		for (int val : fieldValue) {
			list.add((long) val);
		}
		return list;
	}

}
