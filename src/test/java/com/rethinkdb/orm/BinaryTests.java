package com.rethinkdb.orm;

import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;
import com.rethinkdb.orm.entities.EntityBinary;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.rethinkdb.RethinkDB.r;

public class BinaryTests extends BaseTest {

	@BeforeClass
	public static void before() {

		BaseTest.before();

		rdb.register(EntityBinary.class);
		rdb.tablePurge(EntityBinary.class);
	}

	@Test
	public void testBinaryAnnotation() {

		EntityBinary entity1 = new EntityBinary();
		entity1.id = new byte[]{1, 2, 3};
		entity1.binary = new byte[]{4, 5, 6};
		entity1.binaryList = new ArrayList<>();
		entity1.binaryList.add(new byte[]{7, 8});
		entity1.binaryList.add(new byte[]{9, 10});
		entity1.binaryList.add(new byte[]{10, 11});

		entity1.binaryMap = new HashMap<>();
		entity1.binaryMap.put("one", new byte[]{7, 8});
		entity1.binaryMap.put("two", new byte[]{9, 10});
		entity1.binaryMap.put("three", new byte[]{10, 11});

		EntityBinary entity2 = new EntityBinary();
		entity2.id = new byte[]{1, 2};
		entity2.binary = new byte[]{4, 5};
		entity2.binaryList = new ArrayList<>();
		entity2.binaryList.add(new byte[]{7, 8});
		entity2.binaryList.add(new byte[]{9, 10});
		entity2.binaryList.add(new byte[]{10, 11});

		entity2.binaryMap = new HashMap<>();
		entity2.binaryMap.put("one", new byte[]{7, 8});
		entity2.binaryMap.put("four", new byte[]{9, 10});
		entity2.binaryMap.put("five", new byte[]{10, 11});

		rdb.create(entity1);
		rdb.create(entity2);

		EntityBinary res = rdb.get(EntityBinary.class, entity1.id);
		Assert.assertEquals(entity1, res);

		try (Connection conn = rdb.getConnection()) {
			Cursor resA = rdb.table(EntityBinary.class).getAll(new Object[]{r.binary(entity1.id)}).run(conn);
			List resAList = resA.toList();

			// query by Id
			List<EntityBinary> resAll = rdb.getAll(EntityBinary.class, new Object[]{entity1.id});
			Assert.assertEquals(1, resAll.size());
			Assert.assertEquals(entity1, resAll.get(0));
			List<EntityBinary> resAll2 = rdb.getAll(EntityBinary.class, new Object[]{entity2.id});
			Assert.assertEquals(1, resAll2.size());
			Assert.assertEquals(entity2, resAll2.get(0));

			// Query test

			// query by property 'binary'
			List<EntityBinary> queryRes1 = rdb.query(EntityBinary.class, "binary", (Object) entity1.binary);
			Assert.assertEquals(1, queryRes1.size());
			Assert.assertEquals(entity1, queryRes1.get(0));
			List<EntityBinary> queryRes2 = rdb.query(EntityBinary.class, "binary", (Object) entity2.binary);
			Assert.assertEquals(1, queryRes2.size());
			Assert.assertEquals(entity2, queryRes2.get(0));

			// query by property 'binaryList'
			List<EntityBinary> queryResList = rdb.query(EntityBinary.class, "binaryList", (Object) entity1.binaryList.get(0));
			Assert.assertEquals(2, queryResList.size());
			Assert.assertEquals(entity1, queryResList.get(0));
			Assert.assertEquals(entity2, queryResList.get(1));

			// query by property 'binaryMap'
			List<EntityBinary> queryResMap = rdb.query(EntityBinary.class, "binaryMap", "one");
			Assert.assertEquals(2, queryResMap.size());
			Assert.assertEquals(entity1, queryResMap.get(0));
			Assert.assertEquals(entity2, queryResMap.get(1));
		}
	}

}
