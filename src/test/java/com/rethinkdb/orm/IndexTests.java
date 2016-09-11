package com.rethinkdb.orm;

import com.rethinkdb.net.Connection;
import com.rethinkdb.orm.data.IndexInfo;
import com.rethinkdb.orm.entities.EntityIndexOne;
import com.rethinkdb.orm.entities.EntityOne;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

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

		Set<EntityOne> getSet = rdb.getAll(EntityOne.class, one.userId, two.userId);
		assertEquals(2, getSet.size());
		Iterator<EntityOne> iter1 = getSet.iterator();
		assertEquals(1, iter1.next().one);
		assertEquals(3, iter1.next().one);

		Set<EntityOne> greaterThanTwo = rdb.between(EntityOne.class, "one", 3, Integer.MAX_VALUE);
		assertEquals(2, greaterThanTwo.size());

		Set<EntityOne> lessThanTwo = rdb.between(EntityOne.class, "one", 0, 2);
		assertEquals(1, lessThanTwo.size());

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
		Set<EntityIndexOne> set = rdb.query(EntityIndexOne.class, "one", 1);
		assertEquals(2, set.size());

		Set<String> values = new HashSet<>(Arrays.asList("test1", "test3"));
		assertTrue(set.stream().allMatch(ent -> values.contains(ent.name)));

		// again with different filter
		set = rdb.query(EntityIndexOne.class, "one", 2);
		assertEquals(1, set.size());
		assertEquals("test2", set.iterator().next().name);
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

		Set<EntityIndexOne> set = rdb.query(EntityIndexOne.class, "items", "1");
		assertEquals(2, set.size());
		List<String> vals1 = Arrays.asList("test1", "test2");
		assertTrue(set.stream().allMatch(ent -> vals1.contains(ent.name)));

		// 2
		set = rdb.query(EntityIndexOne.class, "items", "2");
		assertEquals(2, set.size());
		List<String> vals2 = Arrays.asList("test2", "test3");
		assertTrue(set.stream().allMatch(ent -> vals2.contains(ent.name)));

		// 3
		set = rdb.query(EntityIndexOne.class, "items", "3");
		List<String> vals3 = Collections.singletonList("test3");
		assertEquals(vals3.size(), set.size());
		assertTrue(set.stream().allMatch(ent -> vals3.contains(ent.name)));
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

		Set<EntityIndexOne> set = rdb.query(EntityIndexOne.class, "list", "1");
		List<String> vals1 = Arrays.asList("test1", "test2");
		assertEquals(vals1.size(), set.size());
		assertTrue(set.stream().allMatch(ent -> vals1.contains(ent.name)));

		// 2
		set = rdb.query(EntityIndexOne.class, "list", "2");
		List<String> vals2 = Arrays.asList("test2", "test3");
		assertEquals(vals2.size(), set.size());
		assertTrue(set.stream().allMatch(ent -> vals2.contains(ent.name)));


		// 3
		set = rdb.query(EntityIndexOne.class, "list", "3");
		List<String> vals3 = Arrays.asList("test3");
		assertEquals(vals3.size(), set.size());
		assertTrue(set.stream().allMatch(ent -> vals3.contains(ent.name)));
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
		Set<EntityIndexOne> set = rdb.between(EntityIndexOne.class, "one", 1, 10);
		List<Integer> vals1 = Arrays.asList(1, 10);
		assertEquals(vals1.size(), set.size());
		assertTrue(set.stream().allMatch(ent -> vals1.contains(ent.one)));

		//
		set = rdb.between(EntityIndexOne.class, "one", 1, 20);
		List<Integer> vals2 = Arrays.asList(1, 10, 20);
		assertEquals(vals2.size(), set.size());
		assertTrue(set.stream().allMatch(ent -> vals2.contains(ent.one)));

		//
		set = rdb.between(EntityIndexOne.class, "one", 11, 20);
		List<Integer> vals3 = Collections.singletonList(20);
		assertEquals(vals3.size(), set.size());
		assertTrue(set.stream().allMatch(ent -> vals3.contains(ent.one)));
	}
}
