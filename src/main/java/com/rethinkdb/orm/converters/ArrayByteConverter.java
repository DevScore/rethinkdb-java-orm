package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.ConverterFactory;
import com.rethinkdb.orm.TypeInfo;

import java.util.ArrayList;
import java.util.List;

public class ArrayByteConverter implements Converter<byte[], List<Long>>, ConverterFactory {

	@Override
	public Converter init(TypeInfo typeInfo) {
		return this;
	}

	public boolean canConvert(TypeInfo typeInfo) {
		return byte[].class.isAssignableFrom(typeInfo.type);
	}

	public byte[] fromProperty(List<Long> list) {

		byte[] array = new byte[list.size()];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i).byteValue();
		}
		return array;
	}

	public List<Long> fromField(byte[] fieldValue) {
		List<Long> list = new ArrayList<>(fieldValue.length);
		for (byte val : fieldValue) {
			list.add((long) val);
		}
		return list;
	}
}


