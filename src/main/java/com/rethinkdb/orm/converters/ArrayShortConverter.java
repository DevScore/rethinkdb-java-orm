package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class ArrayShortConverter implements Converter<short[], List<Long>>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return short[].class.equals(typeInfo.type) && short.class == typeInfo.type.getComponentType();
	}

	public short[] fromProperty(List<Long> list) {

		short[] array = new short[list.size()];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i).shortValue();
		}
		return array;
	}

	public List<Long> fromField(short[] fieldValue) {

		List<Long> list = new ArrayList<>(fieldValue.length);
		for (short val : fieldValue) {
			list.add((long) val);
		}
		return list;
	}

}
