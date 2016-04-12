package com.rethinkdb.orm;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.FixedConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EntityConverterTest {

	private FixedConnectionPool pool;

	@Before
	public void before() {

		Connection.Builder connBuilder1 = Connection.build().hostname("localhost").port(28015);
		Connection.Builder connBuilder2 = Connection.build().hostname("localhost").port(28016);
		Connection.Builder connBuilder3 = Connection.build().hostname("localhost").port(28017);
		Connection.Builder connBuilder4 = Connection.build().hostname("localhost").port(28018); // this one should not exist

		pool = new FixedConnectionPool(connBuilder1, connBuilder2, connBuilder3, connBuilder4);
		RethinkDB.setGlobalConnectionPool(pool);

		RethinkDB.r.db("test").tableCreate("pooltest").run();

		// the gist of this project - registering new POJO converters
		RethinkDB.registerPojoConverter(new EntityConverter());
	}

	@After
	public void after() {
		RethinkDB.r.db("test").tableDrop("pooltest").run();
		pool.close();
	}

	@Test
	public void simpleConverterTest() {

	}

}
