package com.rethinkdb.orm;

import com.rethinkdb.gen.ast.ReqlExpr;
import com.rethinkdb.net.Connection;
import com.rethinkdb.orm.entities.EntityBinary;
import com.rethinkdb.orm.entities.EntityEmbedded;
import com.rethinkdb.orm.entities.EntityEnum;
import com.rethinkdb.orm.entities.EntityOne;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static com.rethinkdb.RethinkDB.r;

public class CollectionTests extends BaseTest {

	@BeforeClass
	public static void before() {

		BaseTest.before();

		rdb.register(EntityOne.class);
		rdb.register(EntityBinary.class);

		rdb.tablePurge(EntityOne.class);
		rdb.tablePurge(EntityBinary.class);
	}

	@Test
	public void collectionTests() {
		EntityOne one = createEmbedded();
		// reload
		EntityOne res = rdb.get(EntityOne.class, one.userId);
		Assert.assertEquals(one, res);
	}

	public EntityOne createEmbedded() {
		EntityOne one = TestUtils.randomEntityOne();
		one.nine = new ArrayList<>();
		one.nine.add("1");
		one.nine.add("2");
		one.nine.add("3");

		one.floatList = new ArrayList<>();
		one.floatList.add(1.1f);
		one.floatList.add(0.0f);
		one.floatList.add(100f);

		one.floatArray = new Float[]{1.1f, 0.0f, 100f};
		one.floatPrimitiveArray = new float[]{1.1f, 0.0f, 100f};

		one.embeddedList = new ArrayList<>();
		one.embeddedList.add(new EntityEmbedded(1, "2", null));
		one.embeddedList.add(new EntityEmbedded(3, "4", null));
		one.embeddedList.add(new EntityEmbedded(5, "6", new EntityEmbedded(7, "8", null)));

		one.enumList = new ArrayList<>();
		one.enumList.add(EntityEnum.FIRST);
		one.enumList.add(EntityEnum.SECOND);

		one.embeddedSet = new HashSet<>();
		one.embeddedSet.add(new EntityEmbedded(11, "12", null));
		one.embeddedSet.add(new EntityEmbedded(13, "14", null));
		one.embeddedSet.add(new EntityEmbedded(15, "16", new EntityEmbedded(17, "18", null)));

		one.mapObject = new HashMap<>();
		one.mapObject.put("first", new EntityEmbedded(21, "22", null));
		one.mapObject.put("second", new EntityEmbedded(23, "24", null));
		one.mapObject.put("third", new EntityEmbedded(25, "26", new EntityEmbedded(27, "28", null)));

		one.mapFloat = new HashMap<>();
		one.mapFloat.put("first", 1.1f);
		one.mapFloat.put("second", 0.0f);
		one.mapFloat.put("third", 100.0f);

		rdb.create(one);

		return one;
	}

	@Test
	public void collectionAppendTest() {
		EntityOne one = createEmbedded();

		// append list
		List<Object> appendEmbeddList = new ArrayList<>();
		appendEmbeddList.add(new EntityEmbedded(200, "100", null));
		appendEmbeddList.add(new EntityEmbedded(201, "101", null));
		appendEmbeddList.add(new EntityEmbedded(202, "102", null));
		rdb.append(EntityOne.class, one.userId, "embeddedList", appendEmbeddList);

		// append set
		Set<Object> appendEmbeddSet = new HashSet<>();
		appendEmbeddSet.add(new EntityEmbedded(200, "100", null));
		appendEmbeddSet.add(new EntityEmbedded(201, "101", null));
		appendEmbeddSet.add(new EntityEmbedded(202, "102", null));
		rdb.append(EntityOne.class, one.userId, "embeddedSet", appendEmbeddSet);

		// append simple list
		List<String> appendStringList = new ArrayList<>();
		appendStringList.add("1001");
		appendStringList.add("1002");
		rdb.append(EntityOne.class, one.userId, "nine", appendStringList);

		// append primitive array
		float[] floatPrimitiveArray = new float[]{11.1f, 22.2f};
		rdb.append(EntityOne.class, one.userId, "floatPrimitiveArray", floatPrimitiveArray);

		// append array
		Float[] floatArray = new Float[]{11.1f, 22.2f};
		rdb.append(EntityOne.class, one.userId, "floatArray", floatArray);

		EntityOne res = rdb.get(EntityOne.class, one.userId);

		// check list
		Assert.assertEquals(6, res.embeddedList.size());
		Assert.assertEquals(200, res.embeddedList.get(3).first.intValue());
		Assert.assertEquals(201, res.embeddedList.get(4).first.intValue());
		Assert.assertEquals(202, res.embeddedList.get(5).first.intValue());

		// check set
		Assert.assertEquals(6, res.embeddedSet.size());
		Iterator<EntityEmbedded> setIter = res.embeddedSet.iterator();
		setIter.next();
		setIter.next();
		setIter.next();
		Assert.assertEquals(200, setIter.next().first.intValue());
		Assert.assertEquals(201, setIter.next().first.intValue());
		Assert.assertEquals(202, setIter.next().first.intValue());


		// check simple list
		Assert.assertEquals(5, res.nine.size());
		Assert.assertEquals("1001", res.nine.get(3));
		Assert.assertEquals("1002", res.nine.get(4));

		// check primitive array
		Assert.assertEquals(5, res.floatPrimitiveArray.length);
		Assert.assertEquals(11.1f, res.floatPrimitiveArray[3], 0.01f);
		Assert.assertEquals(22.2f, res.floatPrimitiveArray[4], 0.01f);

		// check array
		Assert.assertEquals(5, res.floatArray.length);
		Assert.assertEquals(11.1f, res.floatArray[3], 0.01f);
		Assert.assertEquals(22.2f, res.floatArray[4], 0.01f);
	}

	@Test
	public void mapAppendTest() {
		EntityOne one = createEmbedded();

		// append map
		EntityEmbedded embedded1 = new EntityEmbedded(200, "200", null);
		rdb.updateMap(EntityOne.class, one.userId, "mapObject", "fourth", embedded1);
		EntityEmbedded embedded2 = new EntityEmbedded(201, "201", null);
		rdb.updateMap(EntityOne.class, one.userId, "mapObject", "fifth", embedded2);

		// update existing map key
		EntityEmbedded embedded3 = new EntityEmbedded(202, "202", null);
		rdb.updateMap(EntityOne.class, one.userId, "mapObject", "first", embedded3);

		EntityOne res = rdb.get(EntityOne.class, one.userId);

		// check map appends
		Assert.assertEquals(5, res.mapObject.size());
		Assert.assertEquals(200, res.mapObject.get("fourth").first.intValue());
		Assert.assertEquals(201, res.mapObject.get("fifth").first.intValue());
		// check map update
		Assert.assertEquals(202, res.mapObject.get("first").first.intValue());
	}

	@Test
	public void mapAppendBinaryTest() {

		// create test entity
		EntityBinary binary = new EntityBinary();
		binary.id = new byte[]{1};
		binary.binaryMap = new HashMap<>();
		binary.binaryMap.put("1", new byte[]{1, 2, 3});
		binary.binaryMap.put("2", new byte[]{2, 3, 4});
		rdb.create(binary);

		// append map
		rdb.updateMap(EntityBinary.class, binary.id, "binaryMap", "3", new byte[]{3, 4, 5});
		rdb.updateMap(EntityBinary.class, binary.id, "binaryMap", "4", new byte[]{4, 5, 6});

		// update existing map key
		rdb.updateMap(EntityBinary.class, binary.id, "binaryMap", "1", new byte[]{1});

		EntityBinary res = rdb.get(EntityBinary.class, binary.id);

		// check map appends
		Assert.assertEquals(4, res.binaryMap.size());
		Assert.assertArrayEquals(new byte[]{3, 4, 5}, res.binaryMap.get("3"));
		Assert.assertArrayEquals(new byte[]{4, 5, 6}, res.binaryMap.get("4"));
		// check map update
		Assert.assertArrayEquals(new byte[]{1}, res.binaryMap.get("1"));

		try (Connection conn = rdb.getConnection()) {
			long allArraysSize = rdb.table(EntityBinary.class)
				.get(r.binary(binary.id))
				.do_(doc -> doc.g("binaryMap").values().map(ReqlExpr::count).sum())
				.run(conn);

			Assert.assertEquals(10, allArraysSize);
		}
	}
}
