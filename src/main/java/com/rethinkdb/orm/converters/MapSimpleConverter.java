package com.rethinkdb.orm.converters;


import com.rethinkdb.orm.Converter;

import java.util.Map;
import java.util.stream.Collectors;

public class MapSimpleConverter<T> implements Converter<Map<String, T>, Map<String, Object>> {

	// converter for list items
	private final Converter<T, Object> valueConverter;

	public MapSimpleConverter(Converter valueConverter) {
		this.valueConverter = valueConverter;
	}

	@Override
	public Object getPropertyComponentValue(Object fieldComponentValue) {

		return fieldComponentValue;
	}

	public Map<String, T> fromProperty(Map<String, Object> properties) {
		 return properties.entrySet()
				.stream()
				.collect(
						Collectors.toMap(Map.Entry::getKey, e -> valueConverter.fromProperty(e.getValue()))
				);
	}

	public Map<String, Object> fromField(Map<String, T> fieldValue) {
		return fieldValue.entrySet()
				.stream()
				.collect(
						Collectors.toMap(Map.Entry::getKey, e -> valueConverter.fromField(e.getValue()))
				);
	}

}
