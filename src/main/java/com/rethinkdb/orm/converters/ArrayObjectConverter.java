package com.rethinkdb.orm.converters;

import com.rethinkdb.orm.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class ArrayObjectConverter<T> implements Converter<T[], List<Map<String, Object>>> {

	private static ClassConstructor classConstructor = new NoArgClassConstructor();
	private final Class<T> componentType;
	private final ClassMapper<T> mapper;

	public ArrayObjectConverter(Class<T> arrayComponentType) {
		this.componentType = arrayComponentType;
		this.mapper = ClassMapper.getMapper(componentType);
	}

	@Override
	public Object getPropertyComponentValue(Object fieldComponentValue) {
			throw new RdbException(RdbException.Error.ClassMappingError, "Mapping to Object[] component type is not supported.");
	}

	public T[] fromProperty(List<Map<String, Object>> list) {

		if (list == null) {
			return null;
		}

		return list.stream()
				.map(itemProps -> {  // map DB properties to object
					T object = classConstructor.construct(componentType);
					mapper.setFieldValues(object, itemProps);
					return object;
				})
				.toArray(size -> (T[]) Array.newInstance(componentType, list.size())); // convert to array

	}

	public List<Map<String, Object>> fromField(T[] fieldValue) {

		return Arrays.stream(fieldValue).map(mapper::getProperties).collect(Collectors.toList());
	}

}
