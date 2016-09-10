package com.rethinkdb.orm.converters;


import com.rethinkdb.orm.ClassConstructor;
import com.rethinkdb.orm.ClassMapper;
import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.NoArgClassConstructor;

import java.util.Map;
import java.util.stream.Collectors;

public class MapObjectConverter<T> implements Converter<Map<String, T>, Map<String, Map<String, Object>>> {

	private static ClassConstructor classConstructor = new NoArgClassConstructor();
	private final Class<T> componentType;
	private final ClassMapper<T> mapper;

	public MapObjectConverter(Class<T> mapValueType) {
		this.componentType = mapValueType;

		this.mapper = ClassMapper.getMapper(componentType);
	}

	public Map<String, T> fromProperty(Map<String, Map<String, Object>> properties) {

		if (properties == null) {
			return null;
		}

		return properties
				.entrySet()
				.stream()
				.collect(
						Collectors.toMap(
								Map.Entry::getKey,
								e -> {
									T object = classConstructor.construct(componentType);
									mapper.setFieldValues(object, e.getValue());
									return object;
								})
				);
	}

	public Map<String, Map<String, Object>> fromField(Map<String, T> fieldValue) {

		return fieldValue
				.entrySet()
				.stream()
				.collect(
						Collectors.toMap(
								Map.Entry::getKey,
								e -> mapper.getProperties(e.getValue()))
				);
	}

}
