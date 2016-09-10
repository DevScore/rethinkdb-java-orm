package com.rethinkdb.orm;

import com.rethinkdb.orm.entities.EntityWithIgnore;
import com.rethinkdb.orm.utils.DateTimeUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class IgnorePropertyTest extends BaseTest {

	@BeforeClass
	public static void before() {

		BaseTest.before();

		rdb.tablePurge(EntityWithIgnore.class);
		rdb.register(EntityWithIgnore.class);
	}

	@Test
	public void createRecord() {

		EntityWithIgnore in = new EntityWithIgnore();
		in.id = "timestamp1";
		in.time = null;
		in.offsetTime = DateTimeUtils.toOffsetDateTime(System.currentTimeMillis());

		rdb.create(in);

		EntityWithIgnore compare = rdb.get(EntityWithIgnore.class, in.id);
		assertNull(compare.time);
		assertEquals(compare.offsetTime, in.offsetTime);
	}
}
