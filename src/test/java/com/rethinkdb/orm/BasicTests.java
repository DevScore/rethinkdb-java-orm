package com.rethinkdb.orm;

import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;
import com.rethinkdb.orm.entities.EntityNonExistingDbName;
import com.rethinkdb.orm.entities.EntityOne;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.rethinkdb.RethinkDB.r;

public class BasicTests {


	@BeforeClass
	public static void before() {

		Connection.Builder connBuilder1 = Connection.build().hostname("localhost").port(28015).db("test");
		Connection.Builder connBuilder2 = Connection.build().hostname("localhost").port(28016).db("test");
		Connection.Builder connBuilder3 = Connection.build().hostname("localhost").port(28017).db("test");
		Connection.Builder connBuilder4 = Connection.build().hostname("localhost").port(28018).db("test"); // this one should not exist

		CPool.addConnection(connBuilder1);
		CPool.addConnection(connBuilder2);
		CPool.addConnection(connBuilder3);
		CPool.addConnection(connBuilder4);

		RDB.register(EntityOne.class);
	}

	@AfterClass
	public static void after() {

		RDB.tableDrop(EntityOne.class, CPool.getConnection());
		CPool.clearConnections();
	}

	@Test(expected = IllegalStateException.class)
	public void nonExistingDbName() {
		RDB.register(EntityNonExistingDbName.class);  // throws IllegalStateException because DB does not exist
	}

	@Test
	public void simpleTest() {

		Connection connection = CPool.getConnection();
		Table t = RDB.table(EntityOne.class);
		t.run(connection);
	}

	@Test
	public void getNumberId() {

		Connection connection = CPool.getConnection();

		int id = 1;

		RDB.table(EntityOne.class).insert(r.hashMap("id", id)
				.with("one", 1)
				.with("two", "two")
				.with("three", 3.14d)
				.with("four", 3.14f)
				.with("five", (short) 100)
				.with("six", (byte) 127)
		).run(connection);

		EntityOne res = RDB.get(EntityOne.class, id);

		//todo write assert
	}

	@Test
	public void getArrayId() {

		Connection connection = CPool.getConnection();

		// integer array
		int[] ids = new int[]{1, 2, 3};

		// we provide an array of floats as ID
		RDB.table(EntityOne.class).insert(r.hashMap("id", r.array(1.0f, 2.0f, 3.0f)).with("type", "floats")).run(connection);
		EntityOne res = RDB.get(EntityOne.class, ids);

		//todo write assert

	}

	@Test
	public void getArrayIntegers() {

		Connection connection = CPool.getConnection();

		// integer array
		Integer[] ids = new Integer[]{1, 2, 3};

		// we provide an array of floats as ID
		RDB.table(EntityOne.class).insert(r.hashMap("id", r.array(1.0f, 2.0f, 3.0f))
		).run(connection);
		EntityOne res = RDB.get(EntityOne.class, ids);

		//todo write assert

	}

	@Test(expected = RuntimeException.class)
	public void unsupportedIdArrayType() {

		Connection connection = CPool.getConnection();
		// integer array
		byte[] ids = new byte[]{1, 2, 3};
		EntityOne res = RDB.get(EntityOne.class, ids);
	}

	@Test
	public void geoData() {

		Connection connection = CPool.getConnection();

		// we provide an array of floats as ID
		RDB.table(EntityOne.class).insert(r.hashMap("id", "geo1")
				.with("point", r.point(1, 2))
				.with("line", r.line(r.point(1, 2), r.point(3, 4)))
				.with("poly", r.polygon(r.point(1, 2), r.point(3, 4), r.point(5, 6)))
		).run(connection);
		EntityOne res = RDB.get(EntityOne.class, "geo1");

		//todo write assert

	}

	@Test
	public void timeData() {

		Connection connection = CPool.getConnection();

		// we provide an array of floats as ID
		RDB.table(EntityOne.class).insert(r.hashMap("id", "time1")
				.with("now", r.now())
				.with("past", r.time(2016, 1, 1, "+00")) // 1-1-2016 UTC-0
				.with("epoch", r.epochTime(531360000))
				.with("iso", r.iso8601("1986-11-03T08:30:00-07:00"))
		).run(connection);
		EntityOne res = RDB.get(EntityOne.class, "time1");

		//todo write assert

	}

}
