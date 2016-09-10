package com.rethinkdb.orm;

import com.rethinkdb.net.Connection;
import com.rethinkdb.orm.data.IndexInfo;
import com.rethinkdb.orm.entities.EntityIndexOne;
import com.rethinkdb.orm.entities.EntityOne;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;

import static com.rethinkdb.RethinkDB.r;
import static org.junit.Assert.*;

/**
 *
 */
public class IndexTests extends BaseTest {

	@BeforeClass
	public static void before() {

		BaseTest.before();

		// start with clean DB
		rdb.tablePurge(EntityIndexOne.class);
		rdb.tablePurge(EntityOne.class);
	}

	@Test
	public void indexCheck() {

		rdb.register(EntityOne.class);
		rdb.tablePurge(EntityOne.class);

		rdb.registerIndex(EntityOne.class, "testIndex", row -> r.gt(row.g("one"), 2), true);

		EntityOne one = new EntityOne();
		one.userId = "one";
		one.one = 1;
		rdb.create(one);
		EntityOne two = new EntityOne();
		two.userId = "two";
		two.one = 3;
		rdb.create(two);
		EntityOne three = new EntityOne();
		three.userId = "three";
		three.one = 4;
		rdb.create(three);

		List<EntityOne> getList = rdb.getAll(EntityOne.class, one.userId, two.userId);
		Assert.assertEquals(2, getList.size());
		Assert.assertEquals(1, getList.get(0).one);
		Assert.assertEquals(3, getList.get(1).one);

		List greaterThanTwo = rdb.between(EntityOne.class, "one", 3, Integer.MAX_VALUE);
		Assert.assertEquals(2, greaterThanTwo.size());

		List lessThanTwo = rdb.between(EntityOne.class, "one", 0, 2);
		Assert.assertEquals(1, lessThanTwo.size());

	}

	@Test
	public void registerAndCreateIndex() throws InterruptedException {

		// register classes & create tables
		rdb.register(EntityIndexOne.class);
		rdb.tablePurge(EntityIndexOne.class);

		IndexInfo info;

		try (Connection conn = rdb.getConnection()) {
			info = rdb.indexing().getIndexInfo(EntityIndexOne.class, "nonExistent", conn);
			assertNull(info);

			// check index
			rdb.indexing().checkIndexesAreReady(EntityIndexOne.class, conn);

			info = rdb.indexing().getIndexInfo(EntityIndexOne.class, "one", conn);
		}

		assertNotNull(info);
		assertEquals("one", info.getName());
		assertTrue(info.isReady());
		assertFalse(info.isGeo());
		assertFalse(info.isMulti());
		assertFalse(info.isOutdated());

		// store data and query
		// create some data to query
		EntityIndexOne one = new EntityIndexOne();
		one.one = 1;
		one.name = "test1";

		EntityIndexOne two = new EntityIndexOne();
		two.one = 2;
		two.name = "test2";

		EntityIndexOne three = new EntityIndexOne();
		three.one = 1;
		three.name = "test3";

		rdb.create(one);
		rdb.create(two);
		rdb.create(three);

		// query
		List<EntityIndexOne> list = rdb.query(EntityIndexOne.class, "one", 1);
		assertEquals(2, list.size());

		list.sort(Comparator.comparing(entityIndexOne -> entityIndexOne.name));
		assertEquals("test1", list.get(0).name);
		assertEquals("test3", list.get(1).name);

		// again with different filter
		list = rdb.query(EntityIndexOne.class, "one", 2);
		assertEquals(1, list.size());
		assertEquals("test2", list.get(0).name);
	}

	@Test
	public void searchInHashMap() {

		// register classes & create tables
		rdb.register(EntityIndexOne.class);
		rdb.tablePurge(EntityIndexOne.class);

		EntityIndexOne one = new EntityIndexOne();
		one.one = 1;
		one.name = "test1";
		one.items.put("1", "one");

		EntityIndexOne two = new EntityIndexOne();
		two.one = 2;
		two.name = "test2";
		two.items.put("1", "one");
		two.items.put("2", "two");

		EntityIndexOne three = new EntityIndexOne();
		three.one = 1;
		three.name = "test3";
		three.items.put("2", "two");
		three.items.put("3", "three");

		rdb.create(one);
		rdb.create(two);
		rdb.create(three);

		List<EntityIndexOne> list = rdb.query(EntityIndexOne.class, "items", "1");
		assertEquals(2, list.size());
		list.sort(Comparator.comparing(entityIndexOne -> entityIndexOne.name));

		assertEquals("test1", list.get(0).name);
		assertEquals("test2", list.get(1).name);

		// 2
		list = rdb.query(EntityIndexOne.class, "items", "2");
		assertEquals(2, list.size());
		list.sort(Comparator.comparing(entityIndexOne -> entityIndexOne.name));

		assertEquals("test2", list.get(0).name);
		assertEquals("test3", list.get(1).name);

		// 3
		list = rdb.query(EntityIndexOne.class, "items", "3");
		assertEquals(1, list.size());
		assertEquals("test3", list.get(0).name);
	}

	@Test
	public void searchInList() {
		// register classes & create tables
		rdb.register(EntityIndexOne.class);
		rdb.tablePurge(EntityIndexOne.class);

		EntityIndexOne one = new EntityIndexOne();
		one.one = 1;
		one.name = "test1";
		one.list.add("1");

		EntityIndexOne two = new EntityIndexOne();
		two.one = 2;
		two.name = "test2";
		two.list.add("1");
		two.list.add("2");

		EntityIndexOne three = new EntityIndexOne();
		three.one = 1;
		three.name = "test3";
		three.list.add("2");
		three.list.add("3");

		rdb.create(one);
		rdb.create(two);
		rdb.create(three);

		List<EntityIndexOne> list = rdb.query(EntityIndexOne.class, "list", "1");
		assertEquals(2, list.size());
		list.sort(Comparator.comparing(entityIndexOne -> entityIndexOne.name));

		assertEquals("test1", list.get(0).name);
		assertEquals("test2", list.get(1).name);

		// 2
		list = rdb.query(EntityIndexOne.class, "list", "2");
		assertEquals(2, list.size());
		list.sort(Comparator.comparing(entityIndexOne -> entityIndexOne.name));

		assertEquals("test2", list.get(0).name);
		assertEquals("test3", list.get(1).name);

		// 3
		list = rdb.query(EntityIndexOne.class, "list", "3");
		assertEquals(1, list.size());
		assertEquals("test3", list.get(0).name);
	}

	@Test
	public void searchBetween() {

		// register classes & create tables
		rdb.register(EntityIndexOne.class);
		rdb.tablePurge(EntityIndexOne.class);

		EntityIndexOne one = new EntityIndexOne();
		one.one = 1;

		EntityIndexOne two = new EntityIndexOne();
		two.one = 20;

		EntityIndexOne three = new EntityIndexOne();
		three.one = 10;

		rdb.create(one);
		rdb.create(two);
		rdb.create(three);

		//
		List<EntityIndexOne> list = rdb.between(EntityIndexOne.class, "one", 1, 10);
		assertEquals(2, list.size());

		list.sort(Comparator.comparing(entityIndexOne -> entityIndexOne.one));

		assertEquals(1, list.get(0).one);
		assertEquals(10, list.get(1).one);

		//
		list = rdb.between(EntityIndexOne.class, "one", 1, 20);
		assertEquals(3, list.size());

		list.sort(Comparator.comparing(entityIndexOne -> entityIndexOne.one));

		assertEquals(1, list.get(0).one);
		assertEquals(10, list.get(1).one);
		assertEquals(20, list.get(2).one);

		//
		list = rdb.between(EntityIndexOne.class, "one", 11, 20);
		assertEquals(1, list.size());
		assertEquals(20, list.get(0).one);
	}
}
