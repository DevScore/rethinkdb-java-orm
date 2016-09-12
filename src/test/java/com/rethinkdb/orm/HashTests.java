package com.rethinkdb.orm;

import com.rethinkdb.net.Connection;
import com.rethinkdb.orm.converters.HashConverter;
import com.rethinkdb.orm.entities.EntityHash;
import com.rethinkdb.orm.types.Hash;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static com.rethinkdb.RethinkDB.r;
import static org.junit.Assert.assertEquals;

public class HashTests extends BaseTest {

	@BeforeClass
	public static void before() {

		BaseTest.before();

		rdb.register(EntityHash.class);
		rdb.tablePurge(EntityHash.class);
	}

	@Test
	public void testHash() {

		EntityHash entity = new EntityHash();
		entity.id = Hash.wrap(new byte[]{1, 2, 3});
		entity.hash =  Hash.wrap(new byte[]{4, 5, 6});
		entity.hashList = new ArrayList<>();
		entity.hashList.add( Hash.wrap(new byte[]{7, 8}));
		entity.hashList.add( Hash.wrap(new byte[]{9, 10}));
		entity.hashList.add( Hash.wrap(new byte[]{10, 11}));

		rdb.create(entity);

		EntityHash res = rdb.get(EntityHash.class, entity.id);
		assertEquals(entity, res);

		try (Connection conn = rdb.getConnection()) {
			Map<String, Object> resMap = rdb.table(EntityHash.class).get(r.binary(entity.id.getHash())).run(conn);
			EntityHash resEnt = rdb.map(EntityHash.class, resMap);
			assertEquals(entity, resEnt);
		}

		// query by Id
		Collection<EntityHash> resAll = rdb.getAll(EntityHash.class, entity.id);
		assertEquals(1, resAll.size());
		assertEquals(entity, resAll.iterator().next());

		// query by property 'binary'
		Collection<EntityHash> queryRes = rdb.query(EntityHash.class, "hash", entity.hash);
		assertEquals(1, queryRes.size());
		assertEquals(entity, queryRes.iterator().next());

		// query by property 'binaryList'
		Collection<EntityHash> queryResList = rdb.query(EntityHash.class, "hashList", (Object) entity.hashList.get(0));
		assertEquals(1, queryResList.size());
		assertEquals(entity, queryResList.iterator().next());
	}

	@Test
	public void HashWrapUnwrap() {

		Hash hash = Hash.wrap("8bed916d519af68ca2d5f375eefcf996f245df7e");

		//assertEquals("{data=i+2RbVGa9oyi1fN17vz5lvJF334=, $reql_type$=BINARY}", r.binary(hash.getHash()).build());

		assertEquals("7530546B7052314565676F7879433578456A7050634C37583949553D", new HashConverter().fromProperty("u0TkpR1EegoxyC5xEjpPcL7X9IU=".getBytes()).getHashAsHexString());
		assertEquals("8BED916D519AF68CA2D5F375EEFCF996F245DF7E", hash.getHashAsHexString());
	}

}
