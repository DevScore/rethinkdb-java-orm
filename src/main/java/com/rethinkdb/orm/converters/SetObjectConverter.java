package com.rethinkdb.orm.converters;


import com.rethinkdb.orm.ClassConstructor;
import com.rethinkdb.orm.ClassMapper;
import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.NoArgClassConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SetObjectConverter<T> implements Converter<Set<T>, List<Map<String, Object>>> {

	private static ClassConstructor classConstructor = new NoArgClassConstructor();
	private final Class<T> componentType;
	private final ClassMapper<T> mapper;

	public SetObjectConverter(Class<T> componentType) {
		this.componentType = componentType;
		this.mapper = ClassMapper.getMapper(componentType);
	}

	public Set<T> fromProperty(List<Map<String, Object>> properties) {

		if (properties == null) {
			return null;
		}

		return properties.stream()
				.map(itemProps -> {  // map DB properties to object
					T object = classConstructor.construct(componentType);
					mapper.setFieldValues(object, itemProps);
					return object;
				})
				.collect(Collectors.toSet());
	}

	public List<Map<String, Object>> fromField(Set<T> fieldValue) {

		List<Map<String, Object>> ret = fieldValue.stream().map(mapper::getProperties).collect(Collectors.toList());
		return ret;
	}

}
