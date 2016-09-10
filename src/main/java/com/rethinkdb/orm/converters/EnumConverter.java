package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.Converter;
import com.rethinkdb.orm.TypeUtils;

import java.lang.reflect.Type;

@SuppressWarnings("unchecked")
public class EnumConverter<E extends Enum<E>> implements Converter<E, String> {

	private final Class<E> enumClass;

	public EnumConverter(Type type) {
		enumClass = (Class<E>) TypeUtils.erase(type);
	}

	public E fromProperty(String property) {
		return property == null ? null : Enum.valueOf(enumClass, property);
	}

	public String fromField(E fieldValue) {
		return fieldValue.name();
	}

}
