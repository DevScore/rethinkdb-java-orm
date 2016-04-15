package com.rethinkdb.orm;

import com.rethinkdb.gen.ast.Geojson;
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
			new StringConverter(),
			new IntegerConverter(),
			new LongConverter(),
			new ByteConverter(),
			new FloatConverter(),
			new DoubleConverter(),
			new BooleanConverter(),
			new DateConverter(),
			new ShortConverter(),
			new ByteArrayConverter(),
			new SetConverterFactory(),
			new ListConverterFactory(),
			new MapConverterFactory(),
			new EnumConverterFactory()
	);

	public static Converter findConverter(Field field) {
		for (ConverterFactory converterFactory : converters) {
			if (converterFactory.canConvert(field.getType())) {
				return converterFactory.init(field);
			}
		}
		return null;
	}

	public static Converter findConverter(Class type) {
		for (ConverterFactory converterFactory : converters) {
			if (converterFactory.canConvert(type)) {
				return converterFactory.init(null);
			}
		}
		return null;
	}

	public static Map<String /** bin name **/, String /** field name **/> getBinMappings(Class clazz) {
		Map<String, FieldMapper> fieldMappers = getFieldMappers(clazz);

		Map<String, String> binMappings = new HashMap<>(fieldMappers.size());
		for (Map.Entry<String, FieldMapper> fieldMapperEntry : fieldMappers.entrySet()) {
			binMappings.put(fieldMapperEntry.getValue().binName, fieldMapperEntry.getKey());
		}
		return binMappings;
	}

	public static Map<String /** field name **/, FieldMapper> getFieldMappers(Class clazz) {

		Map<String, FieldMapper> mappers = new HashMap<>();

		for (Field field : clazz.getDeclaredFields()) {

//			AsJson asJson = field.getAnnotation(AsJson.class);
//			// AsJson with default target are handled via JsonConverter
//			if (asJson != null && asJson.target() == ConversionTarget.DEFAULT) {
//
//				mappers.put(field.getName(), new FieldMapper(getBinName(field), new JsonConverter(field), field));
//
//			} else if (asJson != null && asJson.target() == ConversionTarget.MAPVALUES) {
//
//				mappers.put(field.getName(), new FieldMapper(getBinName(field), new JsonConverter(field), field));
//
//			} else if (asJson != null && asJson.target() == ConversionTarget.LIST) {
//
//				mappers.put(field.getName(), new FieldMapper(getBinName(field), new JsonConverter(field), field));
//			} else

			if (mappableField(field)) {

				Converter fieldConverter = findConverter(field);

				if (fieldConverter == null) {
					throw new RuntimeException("Error: unable to map field '" + field.getDeclaringClass() + "." + field.getName() + "' " +
							"of unsupported type '" +  field.getType() + "'.");
				}
				mappers.put(field.getName(), new FieldMapper(getBinName(field), fieldConverter, field));
			}
		}

		return mappers;
	}

	public static String getBinName(Field field) {
		// is @BinName annotation used
		String binName = field.getName();
		if (field.getAnnotation(FieldName.class) != null) {
			if (field.getAnnotation(FieldName.class).value().isEmpty()) {
				throw new RuntimeException("Error: @FieldName has empty value: '" + field.getDeclaringClass() + "." + field.getName() + "'.");
			}
			binName = field.getAnnotation(FieldName.class).value();
			if (binName.length() > 14) {
				throw new RuntimeException("Error: @FieldName value too long: value must be max 14 chars long, currently it's " + binName.length() +
						". Field: '" + field.getDeclaringClass() + "." + field.getName() + "'.");
			}
		}
		if (binName.length() > 14) {
			throw new RuntimeException("Error: Field name too long: value must be max 14 chars long, currently it's " + binName.length() +
					". Field: '" + field.getDeclaringClass() + "." + field.getName() + "'.");
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
					return new FieldMapper<>(null, findConverter(field), field);
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
					return new FieldMapper(null, findConverter(field), field);
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
					return new FieldMapper(null, findConverter(field), field);
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
//				&& !field.isAnnotationPresent(AsJson.class)
				&& !field.isAnnotationPresent(Ignore.class)
				&& (field.getModifiers() & IGNORED_FIELD_MODIFIERS) == 0
				&& !field.isSynthetic();
	}

	public static <TYPE> FieldMapper<Object, ?> getIdFieldMapper(Class<TYPE> clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(Id.class) != null) {
				Class fieldType = field.getType();
				if (isValidIdFiledType(fieldType)) {
					Converter converter = findConverter(field);
					return new FieldMapper(null, converter, field);
				} else {
					throw new RuntimeException("Error: field marked with @UserKey must be of type String, Long or long.");
				}
			}
		}
		return null;
	}

	// todo create checks for all valid Id field types
	private static boolean isValidIdFiledType(Class fieldType) {
		return true;
	}



}
