package com.rethinkdb.orm.converters;


import com.rethinkdb.orm.Converter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SetSimpleConverter<T> implements Converter<Set<T>, List<Object>> {

	// converter for list items
	private final Converter<T, Object> itemConverter;
	private final Class<T> componentType;

	public SetSimpleConverter(Converter converter, Class<T> componentType) {
		this.itemConverter = converter;
		this.componentType = componentType;
	}

	@Override
	public Object getPropertyComponentValue(Object fieldComponentValue) {
		return itemConverter.getPropertyComponentValue(fieldComponentValue);
	}

	public Set<T> fromProperty(List<Object> list) {
		return list.stream().map(itemConverter::fromProperty).collect(Collectors.toSet());
	}

	public List<Object> fromField(Set<T> fieldValues) {
		return fieldValues.stream().map(itemConverter::fromField).collect(Collectors.toList());
	}

}
