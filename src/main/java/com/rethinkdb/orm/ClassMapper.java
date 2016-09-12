package com.rethinkdb.orm;

import com.rethinkdb.orm.annotations.DbName;
import com.rethinkdb.orm.annotations.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class ClassMapper<TYPE> {

	private static final Logger log = LoggerFactory.getLogger(RDB.class);

	private static final Map<Class, ClassMapper> classMappers = new ConcurrentHashMap<>();

	public static <T> ClassMapper<T> getMapper(Class<T> clazz) {

		ClassMapper<T> classMapper = classMappers.get(clazz);
		if (classMapper == null) {
			classMapper = new ClassMapper<>(clazz);
			classMappers.put(clazz, classMapper);
			classMapper.init();
		}
		return classMapper;
	}


	private final Class<TYPE> type;
	private final String classTableName;
	private final String classDbName;

	private Map<String /*field name*/, FieldMapper> mappers;
	private FieldMapper<String /*field name*/, String /*property name*/> dbNameFieldMapper;
	private FieldMapper<String /*field name*/, String /*property name*/> tableNameFieldMapper;
	private FieldMapper idFieldMapper;
	private FieldMapper anyPropertyMapper;

	private ClassMapper(Class<TYPE> clazz) {
		this.type = clazz;

		// parse @Namespace class annotation
		DbName namespaceClassAnnotation = clazz.getAnnotation(DbName.class);
		classDbName = namespaceClassAnnotation != null ? namespaceClassAnnotation.value() : null;

		// parse @SetName class annotation
		TableName setNameAnnotation = clazz.getAnnotation(TableName.class);
		classTableName = setNameAnnotation != null ? setNameAnnotation.value() : null;
	}

	private void init() {

		mappers = MapperUtils.getFieldMappers(type);

		dbNameFieldMapper = MapperUtils.getDbNameFieldMapper(type);
		tableNameFieldMapper = MapperUtils.getTableNameFieldMapper(type);
		idFieldMapper = MapperUtils.getIdFieldMapper(type);
		anyPropertyMapper = MapperUtils.getAnyFieldMapper(type);
	}

	public void map(TYPE object,
	                String defaultDbName,
	                String tableName,
	                Object id,
	                Map<String, Object> properties) {

		// property "id" is a special generated property and represents the primary key
		// we must remove it from a list of normal record properties
		Object prodId = properties.remove("id");

		if (id != null) {
			if (id.getClass() == prodId.getClass() && !id.equals(prodId)) {
				log.warn("Error: id mismatch. Requested id=" + id + " but returned id=" + prodId);
			}
		}

		this.setId(object, prodId);

		// set meta-fields on the entity: @Namespace, @SetName, @Expiration..
		this.setMetaFieldValues(object, defaultDbName, tableName);

		// set field values
		this.setFieldValues(object, properties);
	}

	public Object map(TYPE object,
	                  String defaultDbName,
	                  String tableName,
	                  Map<String, Object> properties) {

		// property "id" is a special generated property and represents the primary key
		// we must remove it from a list of normal record properties
		Object prodId = properties.remove("id");

		this.setId(object, prodId);

		// set meta-fields on the entity: @Namespace, @SetName, @Expiration..
		this.setMetaFieldValues(object, defaultDbName, tableName);

		// set field values
		this.setFieldValues(object, properties);

		return fromIdValue(prodId);
	}

	public Class<TYPE> getType() {
		return type;
	}

	ObjectMetadata getRequiredMetadata(Object target, String defaultDbName) {

		Class type = target.getClass();
		ObjectMetadata metadata = new ObjectMetadata();

		// acquire UserKey
		if (idFieldMapper == null) {
			throw new RuntimeException("Class " + type.getName() + " is missing a field with @Id annotation.");
		}
		metadata.id = idFieldMapper.getPropertyValue(target);

		// acquire dbName in the following order
		// 1. use @DbName on a field or
		// 2. use @DbName on class or
		// 3. use default dbName
		String fieldNamespace = dbNameFieldMapper != null ? dbNameFieldMapper.getPropertyValue(target) : null;
		metadata.dbName = fieldNamespace != null ? fieldNamespace :
				(classDbName != null ? classDbName : defaultDbName);
		// dbName still not available
		if (metadata.dbName == null) {
			throw new RuntimeException("Error: dbName could not be inferred from class/field annotations, " +
					"for class " + type.getName() +
					", nor is default dbName available.");
		}

		// acquire @TableName in the following order
		// 1. use @TableName on a field or
		// 2. use @TableName on class or
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

	String getDbName() {

		return classDbName;
	}

	public Map<String, Object> getProperties(TYPE object) {

		Map<String, Object> props = new HashMap<>(mappers.size());
		for (FieldMapper fieldMapper : mappers.values()) {
			Object propertyValue = fieldMapper.getPropertyValue(object);

			// skips writing property value if field is marked with @IgnoreNull && fieldVale == null
			if (fieldMapper.ignoreNull() && propertyValue == null) {
				continue;
			}

			// skips writing property value if field is marked with @IgnoreEmpty && collection.isEmpty()
			if (fieldMapper.ignoreEmpty()
					&& Collection.class.isAssignableFrom(propertyValue.getClass())
					&& ((Collection) propertyValue).isEmpty()) {
				continue;
			}
			// skips writing property value if field is marked with @IgnoreEmpty && map.isEmpty()
			if (fieldMapper.ignoreEmpty()
					&& Map.class.isAssignableFrom(propertyValue.getClass())
					&& ((Map) propertyValue).isEmpty()) {
				continue;
			}

			props.put(fieldMapper.propertyName, propertyValue);
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

	public FieldMapper getdMapper(String fieldName) {
		return mappers.get(fieldName);
	}

	/**
	 * Converts a field value to DB property value
	 *
	 * @param fieldName
	 * @param fieldValue
	 * @return
	 */
	public Object toPropertyValue(String fieldName, Object fieldValue) {
		FieldMapper mapper = mappers.get(fieldName);
		if (mapper == null) {
			throw new RdbException(RdbException.Error.ClassMappingError, "Class " + type.getName() + " does not have a mapper for field " + fieldName);
		}
		return mapper.toPropertyValue(fieldValue);
	}

	/**
	 * Converts a field value to DB property value
	 *
	 * @param fieldName
	 * @param fieldValue
	 * @return
	 */
	public Object toPropertyComponentValue(String fieldName, Object fieldValue) {
		FieldMapper mapper = mappers.get(fieldName);
		if (mapper == null) {
			throw new RdbException(RdbException.Error.ClassMappingError, "Class " + type.getName() + " does not have a mapper for field " + fieldName);
		}
		return mapper.toPropertyComponentValue(fieldValue);
	}

	/**
	 * Converts a field value to DB property value
	 *
	 * @param fieldValue
	 * @return
	 */
	public Object toIdValue(Object fieldValue) {
		if (idFieldMapper == null) {
			throw new RdbException(RdbException.Error.ClassMappingError, "Class " + type.getName() + " does not have a mapper for Id field.");
		}
		return idFieldMapper.toPropertyValue(fieldValue);
	}

	/**
	 * Converts a DB ID property to field ID
	 *
	 * @param propertyValue
	 * @return
	 */
	public Object fromIdValue(Object propertyValue) {
		if (idFieldMapper == null) {
			throw new RdbException(RdbException.Error.ClassMappingError, "Class " + type.getName() + " does not have a mapper for Id field.");
		}
		return idFieldMapper.toFieldValue(propertyValue);
	}

	/**
	 * Converts a DB property value to field value
	 *
	 * @param fieldName
	 * @param propertyValue
	 * @return
	 */
	public Object toFieldValue(String fieldName, Object propertyValue) {
		FieldMapper mapper = mappers.get(fieldName);
		if (mapper == null) {
			throw new RdbException(RdbException.Error.ClassMappingError, "Class " + type.getName() + " does not have a mapper for field " + fieldName);
		}
		return mapper.toFieldValue(propertyValue);
	}

	public void setFieldValues(TYPE object, Map<String, Object> properties) {

		// create a copy
		Map<String, Object> mappedProps = new HashMap<>(properties);

		for (FieldMapper fieldMapper : mappers.values()) {
			Object prop = mappedProps.get(fieldMapper.propertyName);
			mappedProps.remove(fieldMapper.propertyName);
			if (prop != null) {
				fieldMapper.setFieldValue(object, prop);
			}
		}

		// at this point mappedProps should only contain unmapped properties
		if (anyPropertyMapper != null) {
			try {
				anyPropertyMapper.setFieldValue(object, mappedProps);
			} catch (java.lang.ClassCastException e) {
				log.error("Failed to cast: " + object + " from: " + mappedProps);
				throw e;
			}
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
			if (properties.containsKey(fieldMapper.propertyName)) {
				Object propValue = properties.get(fieldMapper.propertyName);
				fieldValues.put(fieldMapper.field.getName(), fieldMapper.toFieldValue(propValue));
			}
		}

		return fieldValues;
	}


	private void setMetaFieldValues(Object object, String dbName, String tableName) {

		if (dbNameFieldMapper != null) {
			dbNameFieldMapper.setFieldValue(object, dbName);
		}
		if (tableNameFieldMapper != null) {
			tableNameFieldMapper.setFieldValue(object, tableName);
		}
	}

	public void setId(TYPE object, Object id) {
		if (idFieldMapper != null) {
			idFieldMapper.setFieldValue(object, id);
		}
	}

	public Object getId(Object object) {
		if (idFieldMapper != null) {
			return idFieldMapper.getPropertyValue(object);
		}
		return null;
	}

	public FieldMapper getIdFieldMapper() {
		return idFieldMapper;
	}

	public String getPropertyName(String fieldName) {
		FieldMapper fieldMapper = mappers.get(fieldName);
		return MapperUtils.getPropertyName(fieldMapper.field);
	}

}
