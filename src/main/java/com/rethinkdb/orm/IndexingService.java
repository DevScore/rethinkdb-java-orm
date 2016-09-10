package com.rethinkdb.orm;

import com.rethinkdb.gen.ast.IndexCreate;
import com.rethinkdb.gen.ast.Javascript;
import com.rethinkdb.gen.ast.ReqlFunction0;
import com.rethinkdb.gen.ast.ReqlFunction1;
import com.rethinkdb.net.Connection;
import com.rethinkdb.orm.annotations.Id;
import com.rethinkdb.orm.annotations.Ignore;
import com.rethinkdb.orm.annotations.Indexed;
import com.rethinkdb.orm.data.IndexCollectionType;
import com.rethinkdb.orm.data.IndexInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Takes care of index creation and usage
 */
class IndexingService {

	private static final Logger log = LoggerFactory.getLogger(IndexingService.class);

	private final RDB rdb;

	IndexingService(RDB rdbService) {

		rdb = rdbService;
	}

	void createIndex(Class<?> clazz, Connection conn) {

		// look up @Indexed annotations in clazz and prepare data for indexing
		Field[] fields = clazz.getDeclaredFields();

		if (fields != null && fields.length > 0) {
			for (Field field : fields) {

				// ignored fields and keys are skipped
				if (field.isAnnotationPresent(Ignore.class) ||
					field.isAnnotationPresent(Id.class)) {
					continue;
				}

				// field is annotated with index
				Indexed annotation = field.getAnnotation(Indexed.class);
				if (annotation != null) {

					// only create index if not already created
					registerIndex(clazz, field, true, conn);
				}
			}
		}
	}

	Annotation[] getAnnotations(Class<?> clazz, String name) {

		List<Annotation> output = new ArrayList<>();
		Field[] fields = clazz.getDeclaredFields();

		if (fields != null && fields.length > 0) {
			for (Field field : fields) {

				if (field.getName().equals(name)) {
					return field.getAnnotations();
				}
			}
		}

		return null;
	}


	IndexInfo getIndexInfo(Class clazz, String index, Connection conn) {

		List<String> output = rdb.table(clazz).indexList().run(conn);

		if (output.size() == 0) {
			return null;
		}

		if (output.contains(index)) {

			// get info
			List<HashMap<String, Object>> status = rdb.table(clazz).indexStatus(index).run(conn);
			if (status.size() == 1) {
				return new IndexInfo(clazz, status.get(0));
			}
		}

		return null;
	}

	private List<IndexInfo> getIndexInfo(Class<?> clazz, Connection conn) {

		List<String> output = rdb.table(clazz).indexList().run(conn);

		if (output.size() == 0) {
			return Collections.emptyList();
		}

		List<IndexInfo> info = new ArrayList<>();
		for (String index : output) {

			// get info
			List<HashMap<String, Object>> status = rdb.table(clazz).indexStatus(index).run(conn);
			if (status.size() == 1) {
				info.add(new IndexInfo(clazz, status.get(0)));
			}
		}

		return info;
	}

	/**
	 * Makes sure created index is ready to use
	 *
	 * @param clazz entity to check indexes
	 * @param conn database connection
	 */
	void checkIndexesAreReady(Class<?> clazz, Connection conn) {

		List<IndexInfo> info = getIndexInfo(clazz, conn);
		for (IndexInfo index : info) {

			if (!index.isReady()) {
				try {
					Thread.sleep(100); // wait some time ..

					// repeat procedure
					log.info("Index not jet ready: " + index + " ... waiting");
					checkIndexesAreReady(clazz, conn);
					break;
				}
				catch (InterruptedException e) {
					log.warn("Index check interrupted!");
					break;
				}
			}

			// all good .. go to next
		}
	}

	void registerIndex(Class clazz, String indexName, ReqlFunction0 indexDefinition, boolean waitIndexFinished, Connection conn) {

		List<String> indexList = rdb.table(clazz).indexList().run(conn);

		if (!indexList.contains(indexName)) {

			rdb.table(clazz).indexCreate(indexName, indexDefinition).run(conn);
			if (waitIndexFinished) {
				rdb.table(clazz).indexWait(indexName).run(conn);
			}
		}
	}

	void registerIndex(Class clazz, String indexName, ReqlFunction1 indexDefinition, Map<String, Object> optArgs, boolean waitIndexFinished, Connection conn) {

		List<String> indexList = rdb.table(clazz).indexList().run(conn);

		if (!indexList.contains(indexName)) {
			IndexCreate idxc = rdb.table(clazz).indexCreate(indexName, indexDefinition);
			if (null != optArgs) {
				for (Map.Entry<String, Object> entry : optArgs.entrySet()) {
					idxc = idxc.optArg(entry.getKey(), entry.getValue());
				}
			}
			idxc.run(conn);
			if (waitIndexFinished) {
				rdb.table(clazz).indexWait(indexName).run(conn);
			}
		}
	}

	void registerIndex(Class clazz, String indexName, Object indexDefinition, boolean waitIndexFinished, Connection conn) {

		List<String> indexList = rdb.table(clazz).indexList().run(conn);

		if (!indexList.contains(indexName)) {
			rdb.table(clazz).indexCreate(indexName, indexDefinition).run(conn);
			if (waitIndexFinished) {
				rdb.table(clazz).indexWait(indexName).run(conn);
			}
		}
	}

	void registerIndex(Class clazz, String indexName, Javascript indexDefinition, boolean waitIndexFinished, Connection conn) {

		List<String> indexList = rdb.table(clazz).indexList().run(conn);

		if (!indexList.contains(indexName)) {
			rdb.table(clazz).indexCreate(indexName, indexDefinition).run(conn);
			if (waitIndexFinished) {
				rdb.table(clazz).indexWait(indexName).run(conn);
			}
		}
	}

	private void registerIndex(Class clazz, Field field, boolean waitIndexFinished, Connection conn) {

		List<String> indexList = rdb.table(clazz).indexList().run(conn);

		// get index name
		String index = field.getName();

		if (!indexList.contains(index)) {

			IndexCollectionType indexType = getIndexCollectionType(field);

			switch (indexType) {

				case DEFAULT: // simple value index
					log.info("Creating index: " + index + " for: " + clazz.getName());
					rdb.table(clazz).indexCreate(index).run(conn);
					break;

				case LIST: // index on items in list
					log.info("Creating collection index: " + index + " for: " + clazz.getName());
					rdb.table(clazz).indexCreate(index).optArg("multi", true).run(conn); // must be true
					break;

				case KEYS: // index on keys of map
					log.info("Creating key/value index: " + index + " for: " + clazz.getName());
					Map<String, Object> optArgs = new HashMap<>();
					optArgs.put("multi", true); // must be true

					rdb.registerIndex(clazz, index, row -> row.g(index).keys(), optArgs, false);
					break;

			}

			if (waitIndexFinished) {
				rdb.table(clazz).indexWait(index).run(conn);
			}
		}
	}

	/**
	 * Resolves index collection type according to field type
	 *
	 * @param field to inspect
	 * @return index collection type for given field
	 */
	private static IndexCollectionType getIndexCollectionType(Field field) {

		if (Collection.class.isAssignableFrom(field.getType()) ||
			List.class.isAssignableFrom(field.getType()) ||
			Array.class.isAssignableFrom(field.getType()) ||
			Set.class.isAssignableFrom(field.getType())) {
			return IndexCollectionType.LIST;
		}

		if (Map.class.isAssignableFrom(field.getType())) {
			return IndexCollectionType.KEYS;
		}

		return IndexCollectionType.DEFAULT;
	}
}
