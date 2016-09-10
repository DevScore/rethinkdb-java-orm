package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class ArraySimpleConverter<T> implements Converter<T[], List<Object>> {

	private final Class<T> componentType;
	private final Converter<T, Object> itemConverter;

	public ArraySimpleConverter(Converter converter, Class<T> arrayComponentType) {
		this.itemConverter = converter;
		this.componentType = arrayComponentType;
	}

	@Override
	public Object getPropertyComponentValue(Object fieldComponentValue) {
		return itemConverter.fromProperty(fieldComponentValue);
	}

	public T[] fromProperty(List<Object> list) {

		if (list == null) {
			return null;
		}

		return list.stream()
				.map(itemConverter::fromProperty)
				.toArray(size -> (T[]) Array.newInstance(componentType, list.size())); // convert to array
	}

	public List<Object> fromField(T[] fieldValue) {
		return Arrays.stream(fieldValue).map(itemConverter::fromField).collect(Collectors.toList());
	}

}
