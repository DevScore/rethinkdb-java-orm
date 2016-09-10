package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.ClassConstructor;
import com.rethinkdb.orm.ClassMapper;
import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.NoArgClassConstructor;

import java.util.Map;

public class EmbeddedObjectConverter<T> implements Converter<T, Map<String, Object>> {

	private static ClassConstructor classConstructor = new NoArgClassConstructor();
	private final ClassMapper<T> mapper;
	private final Class<T> type;

	public EmbeddedObjectConverter(Class<T> type) {
		this.type = type;
		this.mapper = ClassMapper.getMapper(type);
	}

	public boolean canConvert(Class type) {
		return ClassMapper.getMapper(type) != null;
	}

	public T fromProperty(Map<String, Object> property) {
		if (property == null) {
			return null;
		}

		T object = classConstructor.construct(type);
		mapper.setFieldValues(object, property);
		return object;
	}

	public Map<String, Object> fromField(T fieldValue) {
		return mapper.getProperties(fieldValue);
	}

}
