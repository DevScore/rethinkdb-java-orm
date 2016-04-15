package com.rethinkdb.orm;

import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.rethinkdb.RethinkDB.r;

public class RDB {

	private static final Logger log = LoggerFactory.getLogger(RDB.class);

	private static ClassConstructor classConstructor = new NoArgClassConstructor();

	public static void register(Class... classes) {

		Connection conn = CPool.getConnection();
		ArrayList dbList = r.dbList().run(conn);

		for (Class clazz : classes) {

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
			dbName = dbName == null ? conn.db().get() : dbName;

			ArrayList<String> tableList = r.db(dbName).tableList().run(conn);

			if (tableList == null || tableList.isEmpty() || !tableList.contains(tableName)) {
				r.db(dbName).tableCreate(tableName).run(conn);
			}

			// todo create indexes based on @Indexed annotations

		}

	}

	public static void tableDrop(Class clazz, Connection conn) {

		ClassMapper mapper = ClassMapper.getMapper(clazz);

		String dbName = mapper.getDbName();

		if (dbName == null) {
			r.tableDrop(mapper.getTableName()).run(conn);
		} else {
			r.db(dbName).tableDrop(mapper.getTableName()).run(conn);
		}
	}

	public static Table table(Class clazz) {

		ClassMapper mapper = ClassMapper.getMapper(clazz);

		String dbName = mapper.getDbName();

		if (dbName == null) {
			return r.table(mapper.getTableName());
		} else {
			return r.db(dbName).table(mapper.getTableName());
		}
	}

	public static <T> T get(Class<T> clazz, int[] ids) {
		List<Integer> list = new ArrayList<>(ids.length);
		for (int id : ids) {
			list.add(id);
		}
		return get(clazz, list);
	}

	public static <T> T get(Class<T> clazz, float[] ids) {
		List list = new ArrayList<>(ids.length);
		for (float id : ids) {
			list.add(id);
		}
		return get(clazz, list);
	}

	public static <T> T get(Class<T> clazz, double[] ids) {
		List list = new ArrayList<>(ids.length);
		for (double id : ids) {
			list.add(id);
		}
		return get(clazz, list);
	}

	public static <T> T get(Class<T> clazz, long[] ids) {
		List list = new ArrayList<>(ids.length);
		for (double id : ids) {
			list.add(id);
		}
		return get(clazz, list);
	}

	public static <T> T get(Class<T> clazz, List ids) {
		return get(clazz, (Object) ids);
	}

	public static <T> T get(Class<T> clazz, Object[] ids) {
		List list = new ArrayList<>(ids.length);
		Collections.addAll(list, ids);
		return get(clazz, list);
	}

	public static <T> T get(Class<T> clazz, Object id) {

		if (id.getClass().isArray() && id.getClass().getComponentType().isPrimitive()) {
			throw new RuntimeException("Yet unsupported Id type: " + id.getClass().getComponentType() + "[]");
		}

		Connection conn = CPool.getConnection();
		Object res = table(clazz).get(id).run(conn);

		if (res == null) {
			return null;
		}

		if (res instanceof Map) {
			Map<String, Object> resMap = (Map<String, Object>) res;

			T object = classConstructor.construct(clazz);
			ClassMapper mapper = ClassMapper.getMapper(clazz);

			String dbName = getDbName(mapper, conn);
			String tableName = mapper.getTableName();

			mapper.map(object, dbName, tableName, resMap);

			return object;

//			for (String key : resMap.keySet()) {
//				if (resMap.get(key) instanceof JSONObject) {
////					Object obj = JsonUtils.fromJsonObject((JSONObject) resMap.get(key));
////					System.out.println("json obj: "+obj.getClass());
//					JSONObject json = (JSONObject) resMap.get(key);
//					String reqlType = (String) json.get("$reql_type$");
//					System.out.println("reqlType: " + reqlType);
//				}
//				System.out.println(resMap.get(key).getClass().getName() + "  " + key + " : " + resMap.get(key));
//			}
//			return null;

		} else {
			throw new IllegalStateException("Some kind of error: table().get() did not return a Map<String, Object>. " +
					"Investigate pls, Class:" + clazz.getName() + " id:" + id);
		}
	}

	public static String getDbName(ClassMapper mapper, Connection conn) {

		String dbName = mapper.getDbName();


		if (dbName == null && !conn.db().isPresent()) {
			throw new IllegalStateException("Database '" + dbName + "' does not exist. Check class annotations: " + mapper.getType().getName());
		}

		if (dbName == null && !conn.db().isPresent()) {
			throw new IllegalStateException("Database name not defined. Either class must contain @DbName " +
					"or Connection.Builder.db() must be called when defining the connection pool.");
		}

		// set the DB name
		return dbName == null ? conn.db().get() : dbName;
	}

}
