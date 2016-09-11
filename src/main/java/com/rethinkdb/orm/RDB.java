package com.rethinkdb.orm;

import com.rethinkdb.gen.ast.*;
import com.rethinkdb.model.OptArgs;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;
import com.rethinkdb.orm.annotations.Indexed;
import com.rethinkdb.orm.annotations.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.Map;
import java.util.function.Function;

import static com.rethinkdb.RethinkDB.r;

public class RDB {

	private static final Logger log = LoggerFactory.getLogger(RDB.class);

	private static final long READ_TIMEOUT = 10_000L; // 10s at most

	private static ClassConstructor classConstructor = new NoArgClassConstructor();

	private IndexingService indexing;

	private RdbConnectionFactory pool;

	public RDB() {

		indexing = new IndexingService(this);
		pool = new RdbConnectionFactory();
	}

	public void addConnection(String name, int port, String dbname) {

		addConnection(name, port, dbname, null);
	}

	public void addConnection(String name, int port, String dbname, String authkey) {

		pool.addConnection(name, port, dbname, authkey);
	}


	/**
	 * Warm up ... and make sure we have at least one RDB connection up and running
	 */
	public void initialize() {

		pool.initialize();
	}

	/**
	 * Get a new RDB connection - take care of connection life ~ DO NOT forget to close connection
	 * <p>
	 * Or even better use RDB methods instead: execute(ReqlExpr ...)
	 *
	 * @return a brand new RDB connection
	 */
	public Connection getConnection() {
		return pool.getConnection();
	}

	public void register(Class<?>... classes) {

		//todo change the flow so that instead of three conn().run() we use only one
		// = write complex ReQL that preforms all this

		try (Connection conn = pool.getConnection()) {
			ArrayList dbList = r.dbList().run(conn);

			Map<String, List<String>> tablesMap = new HashMap<>();
			for (Class<?> clazz : classes) {

				ClassMapper mapper = ClassMapper.getMapper(clazz);
				String dbName = mapper.getDbName();
				String tableName = mapper.getTableName();

				if (dbName != null && !dbList.contains(dbName)) {
					throw new IllegalStateException("Database '" + dbName + "' does not exist. Check class annotations: " + clazz.getName());
				}

				if (dbName == null && !conn.db().isPresent()) {
					throw new IllegalStateException("Database name not defined. Either class must contain @DbName " +
							"or Connection.Builder.db() must be called when defining the connection pool.");
				}

				// set the DB name
				dbName = dbName == null && conn.db().isPresent() ? conn.db().get() : dbName;

				List<String> tables = tablesMap.get(dbName);
				if (tables == null || tables.isEmpty()) {
					tables = r.db(dbName).tableList().run(conn);
					tablesMap.put(dbName, tables);
				}

				if (!tables.contains(tableName)) {
					log.info("Table: " + tableName + ", not found in: " + dbName + ", creating table!");
					r.db(dbName).tableCreate(tableName).run(conn);

					tables.add(tableName);
				}

				// create indexes based on @Indexed annotations
				indexing.createIndex(clazz, conn);
			}
		}
	}

	public void registerIndex(Class clazz, String indexName, ReqlFunction0 indexDefinition) {

		registerIndex(clazz, indexName, indexDefinition, false);
	}

	public void registerIndex(Class clazz, String indexName, ReqlFunction1 indexDefinition) {

		registerIndex(clazz, indexName, indexDefinition, false);
	}

	public void registerIndex(Class clazz, String indexName, Object indexDefinition) {

		registerIndex(clazz, indexName, indexDefinition, false);
	}

	public void registerIndex(Class clazz, String indexName, Javascript indexDefinition) {

		registerIndex(clazz, indexName, indexDefinition, false);
	}

	private void registerIndex(Class clazz, String indexName, ReqlFunction0 indexDefinition, boolean waitIndexFinished) {

		indexing.registerIndex(clazz, indexName, indexDefinition, waitIndexFinished, pool.getConnection());
	}

	void registerIndex(Class clazz, String indexName, ReqlFunction1 indexDefinition, boolean waitIndexFinished) {

		indexing.registerIndex(clazz, indexName, indexDefinition, null, waitIndexFinished, pool.getConnection());
	}

	void registerIndex(Class clazz, String indexName, ReqlFunction1 indexDefinition, Map<String, Object> optArgs, boolean waitIndexFinished) {

		indexing.registerIndex(clazz, indexName, indexDefinition, optArgs, waitIndexFinished, pool.getConnection());
	}

	private void registerIndex(Class clazz, String indexName, Object indexDefinition, boolean waitIndexFinished) {

		indexing.registerIndex(clazz, indexName, indexDefinition, waitIndexFinished, pool.getConnection());
	}

	private void registerIndex(Class clazz, String indexName, Javascript indexDefinition, boolean waitIndexFinished) {

		indexing.registerIndex(clazz, indexName, indexDefinition, waitIndexFinished, pool.getConnection());
	}

	/**
	 * Run "native" RDB ReqlExpr query
	 * <p>
	 * DO NOT use this for Cursors (connection is closed immediately)
	 *
	 * @param expr
	 * @param <T>
	 * @return
	 */
	public <T> T execute(ReqlExpr expr) {
		return execute(expr, null);
	}

	public <T> T execute(ReqlExpr expr, OptArgs optArgs) {
		try (Connection conn = pool.getConnection()) {
			if (optArgs != null && !optArgs.isEmpty()) {
				return expr.run(conn, optArgs);
			} else {
				return expr.run(conn);
			}
		}
	}

	public <T> T executeCursor(ReqlExpr expr, Function<Cursor, T> call) {
		return executeCursor(expr, null, call);
	}

	public <T> T executeCursor(ReqlExpr expr, OptArgs optArgs, Function<Cursor, T> call) {
		try (Connection conn = pool.getConnection()) {
			Cursor<T> cursor = null;
			if (optArgs != null && !optArgs.isEmpty()) {
				cursor = expr.run(conn, optArgs);
			} else {
				cursor = expr.run(conn);
			}
			T result = call.apply(cursor);
			cursor.close();
			return result;
		}
	}

	public void tableDrop(Class<?> clazz) {

		try (Connection conn = pool.getConnection()) {
			ClassMapper mapper = ClassMapper.getMapper(clazz);

			String dbName = mapper.getDbName();

			if (dbName == null) {
				r.branch(r.tableList().contains(mapper.getTableName()), r.tableDrop(mapper.getTableName()), false).run(conn);
			} else {
				r.branch(r.db(dbName).tableList().contains(mapper.getTableName()), r.db(dbName).tableDrop(mapper.getTableName()), false).run(conn);
			}
		}
	}

	public Table table(Class<?> clazz) {

		ClassMapper mapper = ClassMapper.getMapper(clazz);

		String dbName = mapper.getDbName();

		if (dbName == null) {
			return r.table(mapper.getTableName());
		}

		return r.db(dbName).table(mapper.getTableName());
	}

	/**
	 * Deletes record mapped to given Object. Object is used to determine table name and id of the record to be deleted.
	 *
	 * @param object Mapped object to be deleted
	 * @return true object/entity was deleted
	 */
	public Boolean delete(Object object) {

		Class<?> clazz = object.getClass();
		ClassMapper mapper = ClassMapper.getMapper(clazz);
		Object id = mapper.getId(object);
		if (id == null) {
			throw new IllegalArgumentException("Error: can not delete entity. Entity is missing field with @Id or id field is null.");
		}

		// return true if one entity was deleted
		try (Connection conn = pool.getConnection()) {
			Response res = Response.parse(table(clazz).get(id).delete().run(conn));
			return res.deleted == 1;
		}
	}

	public Object create(Object object) {

		return create(object, "error");  // throws error if Id already exists
	}

	public Object replace(Object object) {

		return create(object, "replace"); // replaces an existing Id
	}

	private Object create(Object object, String conflictResolution) {

		Class<?> clazz = object.getClass();

		ClassMapper mapper = ClassMapper.getMapper(clazz);
		Map<String, Object> props = mapper.getProperties(object);

		String defaultDbName = pool.getDbName();
		try (Connection conn = pool.getConnection()) {
			ObjectMetadata metadata = mapper.getRequiredMetadata(object, defaultDbName);

			// add Id as a property to be passed to DB (RDB treats field 'id' as a default primary key)
			if (metadata.id != null) {
				if (props.containsKey("id")) {
					// todo test this scenario
					throw new RuntimeException("Error: duplicate Id provided: " +
							"Class " + clazz.getName() + " contains field named 'id' and another field marked with @Id.");
				}
				props.put("id", metadata.id);
			}

			Response res = Response.parse(table(clazz)
					.insert(props)
					.optArg("conflict", conflictResolution)
					.optArg("durability", "hard")
					.run(conn));

			// checking returned inserts
			if (conflictResolution.equals("error") && res.inserted != 1L) {
				throw new RuntimeException("DB error: create(Object) should return one result, instead 'inserted'=" + res.inserted);
			}

			// checking returned inserts
			if (conflictResolution.equals("replace") && !(res.replaced == 1L || res.unchanged == 1L)) {
				throw new RuntimeException("DB error: replace(Object) should return one result, instead 'replaced'=" + res.replaced);
			}

			// was the Id generated by the RDB?
			if (res.generatedKeys.size() > 0) {

				Object generatedId = res.generatedKeys.get(0);

				// id was set on entity, but RDB still generated an id
				if (metadata.id != null) {
					throw new RuntimeException("Error: id was set on entity, but RDB still generated an id. id=" + metadata.id + " generatedId=" + generatedId);
				}
				mapper.setId(object, generatedId);
				return generatedId;
			}

			return metadata.id;
		}
	}

	public <T> void append(Class<T> clazz, Object id, String collectionFieldName, Object items) {

		if (!(items instanceof Collection) && !items.getClass().isArray()) {
			throw new IllegalArgumentException("Error: 'items' argument must be an array or a Collection");
		}

		ClassMapper<T> classMapper = ClassMapper.getMapper(clazz);

		// converts id to DB-supported value
		Object propertyId = classMapper.toIdValue(id);

		if (propertyId == null) {
			throw new RdbException(RdbException.Error.ClassMappingError, " Could not convert provided Id value to DB-supported id value.");
		}

		// convert to DB supported values
		ClassMapper mapper = ClassMapper.getMapper(clazz);
		Object props = mapper.toPropertyValue(collectionFieldName, items);

		// DB append() requires an array
		Object[] propArray;
		if (props instanceof Collection) {
			propArray = ((Collection) props).toArray();
		} else if (props.getClass().isArray()) {
			propArray = (Object[]) props;
		} else {
			throw new RdbException(RdbException.Error.ClassMappingError, " Provided field '" + collectionFieldName + "' is not a Collection or an array.");
		}

		// ReQL to append to an array inside a document (whole thing happens server-side)
		try (Connection conn = pool.getConnection()) {
			Response res = Response.parse(table(clazz)
					.get(propertyId)  // get the given doc
					.update(doc -> r.hashMap(collectionFieldName, doc.g(collectionFieldName).setUnion(propArray))) // update collectionField with its's value appended with the given array
					.run(conn));

			// checking that one document was changed
			if (res.replaced != 1L && res.unchanged != 1L) {
				throw new RuntimeException("DB error: append() should return one result, instead 'replaced'=" + res.replaced + " 'unchanged'=" + res.unchanged);
			}
		}
	}

	/**
	 * Updates a map inside a document
	 *
	 * @param clazz     A class that the table is mapped into
	 * @param id        Id of the document
	 * @param fieldName a field name containing the map
	 * @param key       Map key value to be updated
	 * @param value     Map value to be updated
	 * @param <T>
	 */
	public <T> void updateMap(Class<T> clazz, Object id, String fieldName, String key, Object value) {

		updateMap(clazz, id, fieldName, Collections.singletonMap(key, value));
	}

	/**
	 * Updates a map inside a document
	 *
	 * @param clazz     A class that the table is mapped into
	 * @param id        Id of the document
	 * @param fieldName a field name containing the map
	 * @param valueMap  a Map of key, values to be updated
	 * @param <T>
	 */
	public <T> void updateMap(Class<T> clazz, Object id, String fieldName, Map<String, Object> valueMap) {

		ClassMapper<T> classMapper = ClassMapper.getMapper(clazz);

		// converts id to DB-supported value
		Object propertyId = classMapper.toIdValue(id);

		if (propertyId == null) {
			throw new RdbException(RdbException.Error.ClassMappingError, " Could not convert provided Id value to DB-supported id value.");
		}

		// convert to DB supported values
		ClassMapper mapper = ClassMapper.getMapper(clazz);
		Object propMap = mapper.toPropertyValue(fieldName, valueMap);

		// ReQL to append to an array inside a document (whole thing happens server-side)
		try (Connection conn = pool.getConnection()) {
			Response res = Response.parse(table(clazz)
					.get(propertyId)  // get the given doc
					.update(doc -> r.hashMap(fieldName, propMap)) // update collectionField with its's value appended with the given array
					.run(conn));

			// checking that one document was changed
			if (res.replaced != 1L) {
				throw new RuntimeException("DB error: append() should return one result, instead 'replaced'=" + res.inserted);
			}
		}
	}

	public <T> T get(Class<T> clazz, Object id) {

		ClassMapper<T> classMapper = ClassMapper.getMapper(clazz);

		// converts id to DB-supported value
		Object propertyId = classMapper.toIdValue(id);

		if (propertyId == null) {
			throw new RdbException(RdbException.Error.ClassMappingError, " Could not convert provided Id value to DB-supported id value.");
		}

		String defaultDbName = pool.getDbName();

		try (Connection conn = pool.getConnection()) {
			Object res = table(clazz).get(propertyId).run(conn);

			if (res == null) {
				return null;
			}

			if (res instanceof Map) {
				Map<String, Object> resMap = (Map<String, Object>) res;

				T object = classConstructor.construct(clazz);
				classMapper.map(object, defaultDbName, classMapper.getTableName(), propertyId, resMap);
				return object;
			}

			throw new RdbException(RdbException.Error.UnexpectedReturnType, "Some kind of error: table().get() did not return a Map<String, Object>. " +
					"Investigate pls, Class:" + clazz.getName() + " id:" + id
					+ " RDB returned:\n" + res);
		}
	}

	public <T> T map(Class<T> clazz, Map<String, Object> resMap) {

		if (resMap == null) {
			return null;
		}

		T object = classConstructor.construct(clazz);
		ClassMapper mapper = ClassMapper.getMapper(clazz);

		String defaultDbName = pool.getDbName();
		mapper.map(object, defaultDbName, mapper.getTableName(), null, resMap);

		return object;
	}

	public <T> List<T> map(Class<T> clazz, List<Map<String, Object>> resMapList) {

		if (resMapList == null || resMapList.isEmpty()) {
			return Collections.EMPTY_LIST;
		}

		List<T> entities = new ArrayList<>(resMapList.size());

		String defaultDbName = pool.getDbName();
		for (Map<String, Object> resMap : resMapList) {

			T object = classConstructor.construct(clazz);
			ClassMapper mapper = ClassMapper.getMapper(clazz);
			mapper.map(object, defaultDbName, mapper.getTableName(), null, resMap);
			entities.add(object);
		}

		return entities;
	}


	public <T, K> Set<T> getAll(Class<T> clazz, K... id) {

		Object res;
		try (Connection conn = pool.getConnection()) {

			if (id == null || id.length == 0) {
				res = table(clazz).run(conn);
			} else {
				ClassMapper<T> classMapper = ClassMapper.getMapper(clazz);
				Object[] idProps = Arrays.stream(id).map(classMapper::toIdValue).toArray();
				res = table(clazz).getAll(idProps).run(conn);
			}

			return getResultSet(res, clazz);
		}
	}

	/**
	 * Value filtering by index
	 *
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	/*public <T> List<T> filter(Class<T> clazz, String index, Object... values) {
		return filter(clazz, index, null, values);
	}*/
	public <T> Set<T> filter(Class<T> clazz, ReqlFunction1 filterFunction) {

		ReqlExpr reql = table(clazz);
		if (null != filterFunction) {
			reql = reql.filter(filterFunction);
		}

		// execute query
		try (Connection conn = pool.getConnection()) {
			Object res = reql.run(conn);
			return getResultSet(res, clazz);
		}
	}

	/**
	 * Value filtering by index
	 *
	 * @param clazz
	 * @param index
	 * @param fromValue start
	 * @param toValue   end
	 * @param <T>
	 * @return
	 */
	public <T> Set<T> between(Class<T> clazz, String index, Object fromValue, Object toValue) {


		// get indexed field annotations
		Annotation[] annotations = indexing.getAnnotations(clazz, index);

		if (!hasAnnotation(Indexed.class, annotations)) {
			throw new RdbException(RdbException.Error.IndexNotDefined, "Missing index: '" + index + "' on: " + clazz.getName());
		}

		if (hasAnnotation(Timestamp.class, annotations)) {

			fromValue = toEpochTime(fromValue);
			toValue = toEpochTime(toValue);
		}

		try (Connection conn = pool.getConnection()) {
			Object res = table(clazz).between(fromValue, toValue).optArg("right_bound", "closed").optArg("index", index).run(conn);
			return getResultSet(res, clazz);
		}
	}

	/**
	 * Value filtering by index
	 *
	 * @param clazz  to be filtered
	 * @param index  index name
	 * @param values to filter for
	 * @param <T>    result type
	 * @return list of found results or empty if none found
	 */
	public <T> Set<T> query(Class<T> clazz, String index, Object... values) {

		// get indexed field annotations
		Annotation[] annotations = indexing.getAnnotations(clazz, index);

		if (!hasAnnotation(Indexed.class, annotations)) {
			throw new RdbException(RdbException.Error.IndexNotDefined, "Missing index: '" + index + "' on: " + clazz.getName());
		}

		ClassMapper<T> classMapper = ClassMapper.getMapper(clazz);
		values = Arrays.stream(values).map(value -> classMapper.toPropertyComponentValue(index, value)).toArray();

		try (Connection conn = pool.getConnection()) {
			ReqlExpr reql = table(clazz).getAll(values).optArg("index", index);

			// execute query
			Object res = reql.run(conn);
			return getResultSet(res, clazz);
		}
	}

	/**
	 * Makes sure given from/to timestamp is correctly converted to epochTime seconds
	 *
	 * @param value to convert ... must be int or long ... TODO: throw exception if invalid type of from/to timestamp given
	 * @return converted time in seconds if possible
	 */
	public static EpochTime toEpochTime(Object value) {

		if (value == null) {
			return new EpochTime(0);
		}

		long time;
		if (Integer.class.isAssignableFrom(value.getClass())) {
			time = Integer.toUnsignedLong((int) value);
		} else {
			time = (long) value;
		}

		return r.epochTime(time / 1000.0); // Timestamp must be converted to seconds (float)
	}


	private boolean hasAnnotation(Class<? extends Annotation> annotation, Annotation[] list) {

		if (list == null || list.length == 0) {
			return false;
		}

		for (Annotation ann : list) {
			if (ann.annotationType().equals(annotation)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Iterates the RDB cursor and returns the ordered list of unique objects.
	 * Returned list contains unique objects as defined by their respective database IDs.
	 *
	 * @param res
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public <T> Set<T> getResultSet(Object res, Class<T> clazz) {

		if (res == null) {
			return Collections.emptySet();
		}

		ClassMapper<T> mapper = ClassMapper.getMapper(clazz);
		String defaultDbName = pool.getDbName();

		if (res instanceof Cursor) {

			Cursor<Map<String, Object>> cursor = (Cursor<Map<String, Object>>) res;

			try {

				// must iterate over cursor ... in long lists especially
				Map<Object, T> output = new HashMap<>();
				for (Map<String, Object> item : cursor) {

					T object = classConstructor.construct(clazz);
					Object id = mapper.map(object, defaultDbName, mapper.getTableName(), item);
					output.put(id, object);  // this effectively de-duplicates based on document id
				}
				return new HashSet<>(output.values());
			} finally {
				cursor.close();
			}
		} else {
			throw new RuntimeException("Error: response from .getAll() is not a Cursor. Response: " + res);
		}
	}

	/**
	 * Removes all data from table
	 *
	 * @param clazz table name
	 */
	public void tablePurge(Class<?> clazz) {

		try (Connection conn = pool.getConnection()) {
			if (tableExists(clazz, conn)) {
				table(clazz).delete().optArg("durability", "hard").run(conn);
			}
		}
	}

	private boolean tableExists(Class<?> clazz, Connection conn) {

		ClassMapper mapper = ClassMapper.getMapper(clazz);

		String dbName = pool.getDbName();
		ArrayList<String> tableList = r.db(dbName).tableList().run(conn);
		return tableList.contains(mapper.getTableName());
	}

	IndexingService indexing() {

		return indexing;
	}
}
