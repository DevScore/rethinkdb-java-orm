package com.rethinkdb.orm;

import com.rethinkdb.orm.entities.EntityEmbedded;
import com.rethinkdb.orm.entities.EntityOne;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class EmbeddedTests extends BaseTest {

	@BeforeClass
	public static void before() {

		BaseTest.before();

		rdb.register(EntityOne.class);
		rdb.tablePurge(EntityOne.class);
	}


	@Test
	public void embeddedClass() {
		EntityOne one = TestUtils.randomEntityOne();
		EntityEmbedded subEmbedded = new EntityEmbedded(3, "4", null);
		EntityEmbedded embedded = new EntityEmbedded(1, "2", subEmbedded);
		one.embedded = embedded;
		one.embedded.embeddedArray = new EntityEmbedded[]{subEmbedded, subEmbedded};
		rdb.create(one);

		EntityOne result = rdb.get(EntityOne.class, one.userId);
		Assert.assertEquals(one.userId, result.userId);
		Assert.assertEquals(one.embedded, embedded);
		Assert.assertEquals(one.embedded.embedded, subEmbedded);
		Assert.assertEquals(2, one.embedded.embeddedArray.length);
	}

}
