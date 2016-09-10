package com.rethinkdb.orm;

/**
 *
 */
public class BaseTest {

	protected static RDB rdb = new RDB();

	public static void before() {

		rdb.addConnection("localhost", 28015, "test");
		rdb.addConnection("localhost", 28016, "test");
		/*rdb.addConnection("localhost", 28017, "test");
		rdb.addConnection("localhost", 28018, "test");*/

		rdb.initialize();
	}
}
