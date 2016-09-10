package com.rethinkdb.orm;

import com.rethinkdb.orm.annotations.*;
import com.rethinkdb.orm.converters.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class MapperUtils {

	private static final int IGNORED_FIELD_MODIFIERS = Modifier.FINAL | Modifier.STATIC;

	private static final List<? extends ConverterFactory> converters = Arrays.asList(
			new TimestampConverter(),
			new BinaryConverter(),
			new HashConverter(),
			new StringConverter(),
			new IntegerConverter(),
			new LongConverter(),
			new ByteConverter(),
			new FloatConverter(),
			new DoubleConverter(),
			new BooleanConverter(),
			new DateConverter(),
			new OffsetDateConverter(),
			new ShortConverter(),
			new SetConverterFactory(),
			new ListConverterFactory(),
			new MapConverterFactory(),
			new ArrayByteConverter(),
			new ArrayBooleanConverter(),
			new ArrayShortConverter(),
			new ArrayFloatConverter(),
			new ArrayDoubleConverter(),
			new ArrayIntegerConverter(),
			new ArrayLongConverter(),
			new ArrayObjectConverterFactory(),
			new EnumConverterFactory(),
			new EmbeddedObjectConverterFactory()
	);

	public static Converter findConverter(Field field) {
		for (ConverterFactory converterFactory : converters) {
			TypeInfo typeInfo = TypeInfo.getTypeInfo(field);
			if (converterFactory.canConvert(typeInfo)) {
				return converterFactory.init(typeInfo);
			}
		}
		return null;
	}

	public static Converter findConverter(TypeInfo typeInfo) {
		for (ConverterFactory converterFactory : converters) {
			if (converterFactory.canConvert(typeInfo)) {
				return converterFactory.init(typeInfo);
			}
		}
		return null;
	}

	public static Map<String /** field name **/, FieldMapper> getFieldMappers(Class clazz) {

		Map<String, FieldMapper> mappers = new HashMap<>();

		for (Field field : clazz.getDeclaredFields()) {

			if (mappableField(field)) {

				Converter fieldConverter = findConverter(field);

				if (fieldConverter == null) {
					throw new RuntimeException("Error: unable to map field '" + field.getDeclaringClass() + "." + field.getName() + "' " +
							"of unsupported type '" + field.getType() + "'.");
				}
				boolean ignoreNull = field.getAnnotation(IgnoreNull.class) != null;
				mappers.put(field.getName(), new FieldMapper(getPropertyName(field), fieldConverter, field, ignoreNull));
			}
		}

		return mappers;
	}

	public static String getPropertyName(Field field) {
		// is @BinName annotation used
		String binName = field.getName();
		if (field.getAnnotation(FieldName.class) != null) {
			if (field.getAnnotation(FieldName.class).value().isEmpty()) {
				throw new RuntimeException("Error: @FieldName has empty value: '" + field.getDeclaringClass() + "." + field.getName() + "'.");
			}
			binName = field.getAnnotation(FieldName.class).value();
		}
		return binName;
	}

	public static FieldMapper<Map<String, ?>, Long> getAnyFieldMapper(Class clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(AnyProperty.class) != null) {
				Class fieldType = field.getType();
				Type fieldTypeParams = field.getGenericType();
				ParameterizedType paramTypes = null;
				if (fieldTypeParams instanceof ParameterizedType) {
					paramTypes = (ParameterizedType) fieldTypeParams;
				}
				if (Map.class.isAssignableFrom(fieldType) && paramTypes != null &&
						paramTypes.getActualTypeArguments()[0].equals(String.class) &&
						paramTypes.getActualTypeArguments()[1].equals(Object.class)) {
					boolean ignoreNull = field.getAnnotation(IgnoreNull.class) != null;
					return new FieldMapper<>(null, findConverter(field), field, ignoreNull);
				} else {
					throw new RuntimeException("Error: fields marked with @AnyProperty must be of type Map<String, Object>.");
				}
			}
		}
		return null;
	}

	public static FieldMapper<String, String> getDbNameFieldMapper(Class clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(DbName.class) != null) {
				Class fieldType = field.getType();
				if (String.class.equals(fieldType)) {
					return new FieldMapper(null, findConverter(field), field, true);
				} else {
					throw new RuntimeException("Error: field marked with @Namespace must be of type String.");
				}
			}
		}
		return null;
	}

	public static FieldMapper<String, String> getTableNameFieldMapper(Class clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(TableName.class) != null) {
				Class fieldType = field.getType();
				if (String.class.equals(fieldType)) {
					return new FieldMapper(null, findConverter(field), field, true);
				} else {
					throw new RuntimeException("Error: field marked with @SetName must be of type String.");
				}
			}
		}
		return null;
	}

	/**
	 * Should this field be mapped?
	 * Ignored fields with: static, final, @Ignore, synthetic modifiers
	 */
	private static boolean mappableField(Field field) {
		return !field.isAnnotationPresent(Id.class)
				&& !field.isAnnotationPresent(DbName.class)
				&& !field.isAnnotationPresent(TableName.class)
				&& !field.isAnnotationPresent(AnyProperty.class)
				&& !field.isAnnotationPresent(Ignore.class)
				&& (field.getModifiers() & IGNORED_FIELD_MODIFIERS) == 0
				&& !field.isSynthetic();
	}

	public static <TYPE> FieldMapper<Object, ?> getIdFieldMapper(Class<TYPE> clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(Id.class) != null) {
				Class fieldType = field.getType();
				if (isValidIdFieldType(fieldType)) {
					Converter converter = findConverter(field);
					return new FieldMapper(null, converter, field, false);
				} else {
					throw new RuntimeException("Error: field marked with @Id must be of type String, Long or long.");
				}
			}
		}
		return null;
	}

	// todo create checks for all valid Id field types
	private static boolean isValidIdFieldType(Class fieldType) {
		return true;
	}


}
