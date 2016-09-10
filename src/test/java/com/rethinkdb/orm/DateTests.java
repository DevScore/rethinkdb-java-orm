package com.rethinkdb.orm;

import com.rethinkdb.net.Connection;
import com.rethinkdb.orm.entities.EntityDate;
import com.rethinkdb.orm.entities.EntityOffsetDate;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.rethinkdb.RethinkDB.r;

public class DateTests {

	private static RDB rdb = new RDB();


	@BeforeClass
	public static void before() {

		rdb.addConnection("localhost", 28015, "test");
		rdb.addConnection("localhost", 28016, "test");
		rdb.addConnection("localhost", 28017, "test");
		rdb.addConnection("localhost", 28018, "test"); // this one should not exist

		// register classes & create tables
		rdb.register(EntityDate.class);
		rdb.register(EntityOffsetDate.class);

		// start with clean DB
		rdb.tablePurge(EntityDate.class);
		rdb.tablePurge(EntityOffsetDate.class);
	}

	@Test
	public void testTimestampAnnotation() {

		long epochTimeMillis = System.currentTimeMillis();

		EntityOffsetDate in = new EntityOffsetDate();
		in.id = "timestamp1";
		in.timestamp = epochTimeMillis;
		in.timestamps = new HashMap<>();
		in.timestamps.put("stamp1", epochTimeMillis);

		rdb.create(in);
		EntityOffsetDate out = rdb.get(EntityOffsetDate.class, in.id);

		Assert.assertEquals(in, out);
	}

	@Test
	public void testOffsetDate() {

		try (Connection conn = rdb.getConnection()) {

			// epoch time in seconds
			long now = System.currentTimeMillis() / 1000;

			long epochTimeMillis = 531360000;

			// create a record with date properties by hand
			Map<String, Object> res = rdb.table(EntityOffsetDate.class).insert(r.hashMap("id", "offsetDate2")
				.with("now", r.now())
				.with("past", r.time(2016, 1, 1, "+01")) // 1-1-2016 UTC-0
				.with("epoch", r.epochTime(epochTimeMillis / 1000))
				.with("iso", r.iso8601("1986-11-03T08:30:00-07:00"))
			).run(conn);
			Assert.assertEquals(res.get("errors"), 0L);

			EntityOffsetDate ent = rdb.get(EntityOffsetDate.class, "offsetDate2");

			Assert.assertEquals(ent.now.toInstant().getEpochSecond(), now, 5);  // max 5sec difference between server time and client time
			Assert.assertEquals(ent.epoch.toInstant().getEpochSecond(), epochTimeMillis / 1000, 0);

			Assert.assertEquals(ent.past.getYear(), 2016);
			Assert.assertEquals(ent.past.getMonthValue(), 1);
			Assert.assertEquals(ent.past.getDayOfMonth(), 1);
			Assert.assertEquals(ent.past.getHour(), 0);
			Assert.assertEquals(ent.past.getMinute(), 0);
			Assert.assertEquals(ent.past.getOffset().getTotalSeconds(), 3600); // one hour offset in secondsA

			Assert.assertEquals(ent.iso.getYear(), 1986);
			Assert.assertEquals(ent.iso.getMonthValue(), 11);
			Assert.assertEquals(ent.iso.getDayOfMonth(), 3);
			Assert.assertEquals(ent.iso.getHour(), 8);
			Assert.assertEquals(ent.iso.getMinute(), 30);
			Assert.assertEquals(ent.iso.getOffset().getTotalSeconds(), -7 * 3600); // -7 hour offset in seconds
		}
	}

	@Test
	public void testOffsetDateInsert() {

		try (Connection conn = rdb.getConnection()) {

			// epoch time in seconds
			long now = System.currentTimeMillis() / 1000;

			long epochTimeMillis = 531360000;

			Map<String, Object> props = new HashMap<>();
			props.put("id", "offsetDate1");
			props.put("now", OffsetDateTime.now());
			props.put("past", OffsetDateTime.of(2016, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(+1)));
			props.put("iso", OffsetDateTime.parse("1986-11-03T08:30:00-07:00"));
			props.put("epoch", OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochTimeMillis), ZoneOffset.UTC));

			// create a record with date properties by hand
			Map<String, Object> res = rdb.table(EntityOffsetDate.class).insert(props).run(conn);
			Assert.assertEquals(res.get("errors"), 0L);

			EntityOffsetDate ent = rdb.get(EntityOffsetDate.class, "offsetDate1");

			Assert.assertEquals(ent.now.toInstant().getEpochSecond(), now, 5);  // max 5sec difference between server time and client time
			Assert.assertEquals(ent.epoch.toInstant().getEpochSecond(), epochTimeMillis / 1000, 0);

			Assert.assertEquals(ent.past.getYear(), 2016);
			Assert.assertEquals(ent.past.getMonthValue(), 1);
			Assert.assertEquals(ent.past.getDayOfMonth(), 1);
			Assert.assertEquals(ent.past.getHour(), 0);
			Assert.assertEquals(ent.past.getMinute(), 0);
			Assert.assertEquals(ent.past.getOffset().getTotalSeconds(), 3600); // one hour offset in secondsA

			Assert.assertEquals(ent.iso.getYear(), 1986);
			Assert.assertEquals(ent.iso.getMonthValue(), 11);
			Assert.assertEquals(ent.iso.getDayOfMonth(), 3);
			Assert.assertEquals(ent.iso.getHour(), 8);
			Assert.assertEquals(ent.iso.getMinute(), 30);
			Assert.assertEquals(ent.iso.getOffset().getTotalSeconds(), -7 * 3600); // -7 hour offset in seconds
		}
	}

	@Test
	public void testOffsetDateRoundtrip() {

		// epoch time in seconds
		long now = System.currentTimeMillis() / 1000;

		long epochTimeMillis = 531360000;

		EntityOffsetDate entity = new EntityOffsetDate();
		entity.id = "offsetDate3";
		entity.now = OffsetDateTime.now();
		entity.past = OffsetDateTime.of(2016, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(+1));
		entity.iso = OffsetDateTime.parse("1986-11-03T08:30:00-07:00");
		entity.epoch = OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochTimeMillis), ZoneOffset.UTC);

		// create a record with date properties by hand
		Object resId = rdb.create(entity);
		Assert.assertEquals(entity.id, resId);

		EntityOffsetDate result = rdb.get(EntityOffsetDate.class, "offsetDate3");

		Assert.assertEquals(result.now.toInstant().getEpochSecond(), now, 5);  // max 5sec difference between server time and client time
		Assert.assertEquals(result.epoch.toInstant().getEpochSecond(), epochTimeMillis / 1000, 0);

		Assert.assertEquals(result.past.getYear(), 2016);
		Assert.assertEquals(result.past.getMonthValue(), 1);
		Assert.assertEquals(result.past.getDayOfMonth(), 1);
		Assert.assertEquals(result.past.getHour(), 0);
		Assert.assertEquals(result.past.getMinute(), 0);
		Assert.assertEquals(result.past.getOffset().getTotalSeconds(), 3600); // one hour offset in secondsA

		Assert.assertEquals(result.iso.getYear(), 1986);
		Assert.assertEquals(result.iso.getMonthValue(), 11);
		Assert.assertEquals(result.iso.getDayOfMonth(), 3);
		Assert.assertEquals(result.iso.getHour(), 8);
		Assert.assertEquals(result.iso.getMinute(), 30);
		Assert.assertEquals(result.iso.getOffset().getTotalSeconds(), -7 * 3600); // -7 hour offset in seconds
	}

	@Test
	public void testDateProperties() {

		try (Connection conn = rdb.getConnection()) {

			// epoch time in seconds
			long now = System.currentTimeMillis() / 1000;
			long epochTimeMillis = 531360000;

			Map<String, Object> props = new HashMap<>();
			props.put("id", "date1");
			props.put("now", OffsetDateTime.now());
			props.put("past", OffsetDateTime.of(2016, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(+1)));
			props.put("iso", OffsetDateTime.parse("1986-11-03T08:30:00-07:00"));
			props.put("epoch", OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochTimeMillis), ZoneOffset.UTC));

			// create a record with date properties by hand
			Map<String, Object> res = rdb.table(EntityDate.class).insert(props).run(conn);
			Assert.assertEquals(res.get("errors"), 0L);
			Assert.assertEquals(res.get("inserted"), 1L);

			EntityDate ent = rdb.get(EntityDate.class, "date1");

			Assert.assertEquals(ent.now.toInstant().getEpochSecond(), now, 5);  // max 5sec difference between server time and client time
			Assert.assertEquals(ent.epoch.toInstant().getEpochSecond(), 531360000 / 1000, 0);

			OffsetDateTime iso = OffsetDateTime.parse("1986-11-03T08:30:00-07:00");
			OffsetDateTime past = OffsetDateTime.of(2016, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(+1));

			Assert.assertEquals(ent.iso.toInstant().getEpochSecond(), iso.toInstant().getEpochSecond(), 0);
			Assert.assertEquals(ent.past.toInstant().getEpochSecond(), past.toInstant().getEpochSecond(), 0);
		}
	}

	@Test
	public void testDateRoundtrip() {

		// epoch time in seconds
		long now = System.currentTimeMillis() / 1000;
		long epochTimeMillis = 531360000;

		EntityDate entity = new EntityDate();
		entity.id = "date2";
		entity.now = new Date(System.currentTimeMillis());
		long pastSec = OffsetDateTime.of(2016, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(+1)).toEpochSecond();
		long isoSec = OffsetDateTime.parse("1986-11-03T08:30:00-07:00").toEpochSecond();
		long epochSec = OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochTimeMillis), ZoneOffset.UTC).toEpochSecond();
		entity.past = new Date(1000 * pastSec);
		entity.iso = new Date(1000 * isoSec);
		entity.epoch = new Date(1000 * epochSec);

		// create a record with date properties by hand
		Object resId = rdb.create(entity);
		Assert.assertEquals(entity.id, resId);

		EntityDate ent = rdb.get(EntityDate.class, "date2");
		Assert.assertNotNull(ent);

		Assert.assertEquals(ent.now.toInstant().getEpochSecond(), now, 5);  // max 5sec difference between server time and client time
		Assert.assertEquals(ent.epoch.toInstant().getEpochSecond(), 531360000 / 1000, 0);

		OffsetDateTime iso = OffsetDateTime.parse("1986-11-03T08:30:00-07:00");
		OffsetDateTime past = OffsetDateTime.of(2016, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(+1));

		Assert.assertEquals(ent.iso.toInstant().getEpochSecond(), iso.toInstant().getEpochSecond(), 0);
		Assert.assertEquals(ent.past.toInstant().getEpochSecond(), past.toInstant().getEpochSecond(), 0);

	}

}
