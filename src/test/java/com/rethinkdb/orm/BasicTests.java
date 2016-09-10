package com.rethinkdb.orm;

import com.rethinkdb.net.Connection;
import com.rethinkdb.orm.entities.EntityOne;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.rethinkdb.RethinkDB.r;

public class BasicTests extends BaseTest {

	@BeforeClass
	public static void before() {

		BaseTest.before();

		rdb.register(EntityOne.class);
		rdb.tablePurge(EntityOne.class);
	}

	@Test
	public void putProperties() {

		Map<String, Object> props = new HashMap<>();
		props.put("id", "props1");
//		props.put("byte", (byte) 127);
//		props.put("short", (short) 32444);
//		props.put("int", 234872348);
//		props.put("long", 2348723484L);
//		props.put("boolean", true);
//		props.put("float0", 0.0f);
//		props.put("float", 1.1f);
//		props.put("double", 1.23456556d);
//		props.put("id", "props1");
////		props.put("byteA", r.array((byte) 1, (byte) 2, (byte) 3));
//		props.put("byteB", Arrays.asList((byte) 1, (byte) 2, (byte) 3));
//		props.put("shortA", Arrays.asList((short) 32444, (short) 32325));
//		props.put("intA", Arrays.asList(234872348, 5));
//		props.put("longA", Arrays.asList(new long[]{2348723484L, 12123123123L}));
//		props.put("booleanA", Arrays.asList(true, false));
//		props.put("booleanB", r.array(true, false));
//		props.put("charA", Arrays.asList('a', "b"));
//		props.put("floatA", Arrays.asList(1.2345f, 2.33333f));
//		props.put("doubleA", Arrays.asList(1.23456556d, 34234.213243));
		props.put("binary", r.binary(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}));

		// save
		try (Connection conn = rdb.getConnection()) {
			rdb.table(EntityOne.class).insert(props).run(conn);
			// reload
			Map<String, Object> res = rdb.table(EntityOne.class).get("props1").run(conn);

			for (String key : res.keySet()) {
				System.out.println(key + ":" + res.get(key) + "  " + res.get(key).getClass().getName());
			}
		}

		//todo add 'binary' to EntityOne and add an @Indexed on it
//		List<EntityOne> foundBinary = rdb.filter(EntityOne.class, "binary", r.binary(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}));
//		Cursor out = rdb.table(EntityOne.class).between(r.binary(new byte[]{1, 2}), r.binary(new byte[]{1, 3})).optArg("index", "binary").run(CPool.getConnection());

//		List list = out.toList();
//		for (Object obj : list) {
//			System.out.println(obj);
//		}

	}

	@Test(expected = IllegalArgumentException.class)
	public void outOfBoundsFloat() {

		Map<String, Object> props = new HashMap<>();
		props.put("id", "props2");
		props.put("four", (double) 2 * Float.MAX_VALUE);

		// save
		try (Connection conn = rdb.getConnection()) {
			rdb.table(EntityOne.class).insert(props).run(conn);
		}
		// reload
		EntityOne res = rdb.get(EntityOne.class, "props2");
	}

	@Test(expected = IllegalArgumentException.class)
	public void outOfBoundsInteger() {

		Map<String, Object> props = new HashMap<>();
		props.put("id", "props3");
		props.put("one", (long) Integer.MAX_VALUE + 1);

		// save
		try (Connection conn = rdb.getConnection()) {
			rdb.table(EntityOne.class).insert(props).run(conn);
		}
		// reload
		EntityOne res = rdb.get(EntityOne.class, "props3");
	}

}
