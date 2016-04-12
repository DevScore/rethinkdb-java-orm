package com.devscore.rethinkdb;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
