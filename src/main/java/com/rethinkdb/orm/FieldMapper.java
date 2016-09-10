package com.rethinkdb.orm;

import com.rethinkdb.orm.annotations.IgnoreEmpty;
import com.rethinkdb.orm.converters.DoubleConverter;
import com.rethinkdb.orm.converters.FloatConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import static com.rethinkdb.orm.RdbException.Error.ConfigMismatch;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class FieldMapper<F, P> {

	private static final Logger log = LoggerFactory.getLogger(FieldMapper.class);

	public final Converter<F, P> converter;
	public final String propertyName;

	public Field field;
	private boolean ignoreNull;
	private boolean ignoreEmpty;
	public MethodHandle getter;
	public MethodHandle setter;

	public FieldMapper(String propertyName, Converter<F, P> converter, Field field, boolean ignoreNull) {
		this.propertyName = propertyName;
		this.converter = converter;
		this.field = field;
		this.ignoreNull = ignoreNull;

		ignoreEmpty = field.getAnnotation(IgnoreEmpty.class) != null;
		if (ignoreEmpty && !(Collection.class.isAssignableFrom(field.getType()) || Map.class.isAssignableFrom(field.getType()))) {
			throw new RdbException(ConfigMismatch, "Annotation @IgnoreEmpty can only be used on fields of type Collection.");
		}

		field.setAccessible(true);
		this.field = field;
		try {
			this.getter = MethodHandles.lookup().unreflectGetter(field);
			this.setter = MethodHandles.lookup().unreflectSetter(field);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public P getPropertyValue(Object object) {
		try {
			F fieldValue = (F) field.get(object);
			if (fieldValue == null) {
				return null;
			}
			return converter.fromField(fieldValue);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e); //todo nicer error
		}
	}

	public P toPropertyValue(F value) {
		return converter.fromField(value);
	}


	public P toPropertyComponentValue(F value) {
		return (P) converter.getPropertyComponentValue(value);
	}

	public void setFieldValue(Object targetObject, P propertyValue) {
		try {

			F value;

			// this is a nasty workaround around RDB craziness
			// saving 0.0 will return Long
			// saving 1.1 will return Double
			if (propertyValue instanceof Long && propertyValue.equals(0L) && (converter instanceof DoubleConverter || converter instanceof FloatConverter)) {
				value = converter.fromProperty((P) new Double(0));
			} else {
				value = converter.fromProperty(propertyValue);
			}

			if (!(value == null && field.getType().isPrimitive())) { // do not set value if primitive type, leave it default
				field.set(targetObject, value);
			}
		} catch (ClassCastException e) {

			log.error("Failed to convert: " + propertyValue + " to: " + targetObject);
			throw e; // rethrow

		} catch (IllegalAccessException e) {
			throw new RuntimeException(e); //todo nicer error
		}
	}

	public F toFieldValue(P propertyValue) {
		return converter.fromProperty(propertyValue);
	}

	public boolean ignoreNull() {
		return ignoreNull;
	}

	public boolean ignoreEmpty() {
		return ignoreEmpty;
	}

}
