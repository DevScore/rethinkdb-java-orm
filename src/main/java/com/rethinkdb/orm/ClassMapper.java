package com.rethinkdb.orm;

import com.rethinkdb.orm.annotations.DbName;
import com.rethinkdb.orm.annotations.TableName;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class ClassMapper<TYPE> {

	private static final Map<Class, ClassMapper> classMappers = new ConcurrentHashMap<>();

	public static <T> ClassMapper<T> getMapper(Class<T> clazz) {

		ClassMapper<T> classMapper = classMappers.get(clazz);
		if (classMapper == null) {
			classMapper = new ClassMapper<>(clazz);
			classMappers.put(clazz, classMapper);
		}
		return classMapper;
	}

	public static boolean hasMapper(Class clazz) {
		return classMappers.containsKey(clazz);
	}

	private final Map<String /** field name **/, FieldMapper> mappers;

	private final Class<TYPE> type;
	private final String classTableName;
	private final String classDbName;

	private final FieldMapper<String, String> dbNameFieldMapper;
	private final FieldMapper<String, String> tableNameFieldMapper;
	private final FieldMapper idFieldMapper;
	private final FieldMapper anyPropertyMapper;

	public ClassMapper(Class<TYPE> clazz) {
		this.type = clazz;

		// parse @Namespace class annotation
		DbName namespaceClassAnnotation = clazz.getAnnotation(DbName.class);
		classDbName = namespaceClassAnnotation != null ? namespaceClassAnnotation.value() : null;

		// parse @SetName class annotation
		TableName setNameAnnotation = clazz.getAnnotation(TableName.class);
		classTableName = setNameAnnotation != null ? setNameAnnotation.value() : null;

		Map<String, FieldMapper> fieldMappers = MapperUtils.getFieldMappers(clazz);
//		fieldMappers.putAll(MapperUtils.getJsonMappers(clazz));
		mappers = fieldMappers;

		dbNameFieldMapper = MapperUtils.getDbNameFieldMapper(clazz);
		tableNameFieldMapper = MapperUtils.getTableNameFieldMapper(clazz);
		idFieldMapper = MapperUtils.getIdFieldMapper(clazz);
		anyPropertyMapper = MapperUtils.getAnyFieldMapper(clazz);
	}

	public void map(TYPE object,
	                String dbName,
	                String tableName,
	                Map<String, Object> properties) {
		// set meta-fields on the entity: @Namespace, @SetName, @Expiration..
		this.setMetaFieldValues(object, dbName, tableName);

		// set field values
		this.setFieldValues(object, properties);
	}

	public Class<TYPE> getType() {
		return type;
	}

	public ObjectMetadata getRequiredMetadata(Object target, String defaultNamespace) {
		Class type = target.getClass();
		ObjectMetadata metadata = new ObjectMetadata();

		// acquire UserKey
		if (idFieldMapper == null) {
			throw new RuntimeException("Class " + type.getName() + " is missing a field with @UserKey annotation.");
		}
		Object userKeyObj = idFieldMapper.getPropertyValue(target);
		if (userKeyObj == null) {
			throw new RuntimeException("Field with @Id annotation cannot be null" +
					" Field " + type.getName() + "$" + idFieldMapper.field.getName() + " type is " + idFieldMapper.field.getType().getName() + ", value: null");
		} else if (userKeyObj instanceof String) {
			metadata.idString = (String) userKeyObj;
			if (metadata.idString.isEmpty()) {
				throw new RuntimeException("Field with @Id annotation and with type of String cannot be empty" +
						" Field " + type.getName() + "$" + idFieldMapper.field.getName() + " type is " + idFieldMapper.field.getType().getName() + ", value: ''");

			}
		} else if (userKeyObj instanceof Long) {
			metadata.idLong = (Long) userKeyObj;
		} else {
			throw new RuntimeException("@UserKey annotation can only be used on fields of type: String, Long or long." +
					" Field " + type.getName() + "$" + idFieldMapper.field.getName() + " type is " + idFieldMapper.field.getType().getName());
		}

		// acquire Namespace in the following order
		// 1. use @Namespace on a field or
		// 2. use @Namespace on class or
		// 3. use default dbName
		String fieldNamespace = dbNameFieldMapper != null ? dbNameFieldMapper.getPropertyValue(target) : null;
		metadata.dbName = fieldNamespace != null ? fieldNamespace :
				(classDbName != null ? classDbName : defaultNamespace);
		// dbName still not available
		if (metadata.dbName == null) {
			throw new RuntimeException("Error: dbName could not be inferred from class/field annotations, " +
					"for class " + type.getName() +
					", nor is default dbName available.");
		}

		// acquire @SetName in the following order
		// 1. use @SetName on a field or
		// 2. use @SetName on class or
		// 3. Use Class simple name
		String fieldSetName = tableNameFieldMapper != null ? tableNameFieldMapper.getPropertyValue(target) : null;
		metadata.tableName = fieldSetName != null ? fieldSetName :
				(classTableName != null ? classTableName : type.getSimpleName());

		return metadata;
	}

	/**
	 * @return returns set name according to class type or tableName annotation
	 */
	public String getTableName() {

		return classTableName != null ? classTableName : type.getSimpleName();
	}

	public String getDbName() {
		return classDbName;
	}

	public Map<String, Object> getProperties(TYPE object) {

		Map<String, Object> props = new HashMap<>(mappers.size());
		for (FieldMapper fieldMapper : mappers.values()) {
			Object propertyValue = fieldMapper.getPropertyValue(object);
			props.put(fieldMapper.binName, propertyValue);
		}

		// find unmapped properties
		if (anyPropertyMapper != null) {
			Map<String, Object> unmappedProperties = (Map<String, Object>) anyPropertyMapper.getPropertyValue(object);
			for (String propName : unmappedProperties.keySet()) {
				props.put(propName, unmappedProperties.get(propName));
			}
		}

		return props;
	}

	public FieldMapper getFieldMapper(String fieldName) {
		return mappers.get(fieldName);
	}

	public void setFieldValues(TYPE object, Map<String, Object> properties) {

		// create a copy
		Map<String, Object> mappedProps = new HashMap<>(properties);

		for (FieldMapper fieldMapper : mappers.values()) {
			Object prop = mappedProps.get(fieldMapper.binName);
			mappedProps.remove(fieldMapper.binName);
			if (prop != null) {
				fieldMapper.setFieldValue(object, prop);
			}
		}

		// at this point mappedProps should only contain unmapped properties
		if (anyPropertyMapper != null) {
			anyPropertyMapper.setFieldValue(object, mappedProps);
		}
	}

	/**
	 * Translates bin names/values into field names/values.
	 *
	 * @param properties map of field properties
	 * @return mapped properties
	 */
	public Map<String, Object> getFieldValues(Map<String, Object> properties) {

		Map<String, Object> fieldValues = new HashMap<>();

		for (FieldMapper fieldMapper : mappers.values()) {
			if (properties.containsKey(fieldMapper.binName)) {
				Object propValue = properties.get(fieldMapper.binName);
				fieldValues.put(fieldMapper.field.getName(), fieldMapper.getFieldValue(propValue));
			}
		}

		return fieldValues;
	}


	public void setMetaFieldValues(Object object, String namespace, String setName) {

		if (dbNameFieldMapper != null) {
			dbNameFieldMapper.setFieldValue(object, namespace);
		}
		if (tableNameFieldMapper != null) {
			tableNameFieldMapper.setFieldValue(object, setName);
		}
	}

	public void setUserKey(TYPE object, String userKey) {
		if (idFieldMapper != null) {
			if (!String.class.isAssignableFrom(idFieldMapper.field.getType())) {
				throw new RuntimeException("Id type mismatch: @Id field '" +
						idFieldMapper.field.getDeclaringClass().getName() + "#" + idFieldMapper.field.getName() +
						"' has type '" + idFieldMapper.field.getType() + "', while key has type 'String'."
				);
			}
			idFieldMapper.setFieldValue(object, userKey);
		}
	}

	public void setUserKey(TYPE object, Long userKey) {
		if (idFieldMapper != null) {
			if (!long.class.isAssignableFrom(idFieldMapper.field.getType()) && // UserKey could be a long but given "userKey" will always be Long
					!Long.class.isAssignableFrom(idFieldMapper.field.getType())) {
				throw new RuntimeException("Key type mismatch: @UserKey field '" +
						idFieldMapper.field.getDeclaringClass().getName() + "#" + idFieldMapper.field.getName() +
						"' has type '" + idFieldMapper.field.getType() + "', while key has type 'Long'."
				);
			}
			idFieldMapper.setFieldValue(object, userKey);
		}
	}


	public String getPropertyName(String fieldName) {
		FieldMapper fieldMapper = mappers.get(fieldName);
		return MapperUtils.getBinName(fieldMapper.field);
	}

}
