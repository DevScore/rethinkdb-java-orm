package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class ArrayBooleanConverter implements Converter<boolean[], List<Boolean>>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return boolean[].class.equals(typeInfo.type) && boolean.class == typeInfo.type.getComponentType();
	}

	public boolean[] fromProperty(List<Boolean> list) {

		boolean[] array = new boolean[list.size()];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		return array;
	}

	public List<Boolean> fromField(boolean[] fieldValue) {

		List<Boolean> list = new ArrayList<>(fieldValue.length);
		for (boolean val : fieldValue) {
			list.add(val);
		}
		return list;
	}

}
