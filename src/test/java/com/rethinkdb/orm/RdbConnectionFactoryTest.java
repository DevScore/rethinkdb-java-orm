package com.rethinkdb.orm;

import com.rethinkdb.net.Connection;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Run manually
 */
@Ignore
public class RdbConnectionFactoryTest {

	private static final Logger log = LoggerFactory.getLogger(RdbConnectionFactoryTest.class);



	@Test
	public void rotateConnectionPoolTest() {

		// make sure 3 nodes are running ...
		RDB rdb = new RDB();

		rdb.addConnection("localhost", 28015, "test");
		rdb.addConnection("localhost", 28016, "test");
		rdb.addConnection("localhost", 28017, "test"); // this one should not exist
		rdb.addConnection("localhost", 28018, "test"); // this one should not exist

		rdb.initialize();

		int port;
		try (Connection conn = rdb.getConnection()) {
			assertTrue(conn.isOpen());
			assertEquals("localhost", conn.hostname);

			port = conn.port;
			if (port >= 28016) {
				port = 28015;
			}
			else {
				port++;
			}
		}
		//assertEquals(28015, conn.port);

		// 2.
		try (Connection conn = rdb.getConnection()) {
			assertTrue(conn.isOpen());
			assertEquals("localhost", conn.hostname);
			assertEquals(port, conn.port);

			port = conn.port;
			if (port >= 28016) {
				port = 28015;
			}
			else {
				port++;
			}
		}

		// 3.
		try (Connection conn = rdb.getConnection()) {
			assertTrue(conn.isOpen());
			assertEquals("localhost", conn.hostname);
			assertEquals(port, conn.port);

			port = conn.port;
			if (port >= 28016) {
				port = 28015;
			}
			else {
				port++;
			}
		}


		// 3.
		try (Connection conn = rdb.getConnection()) {
			assertTrue(conn.isOpen());
			assertEquals("localhost", conn.hostname);
			assertEquals(port, conn.port);
		}
	}


	@Test
	public void connectionPoolRefreshTest() throws InterruptedException {


		RDB rdb = new RDB();

		rdb.addConnection("localhost", 28015, "test");
		rdb.addConnection("localhost", 28016, "test");
		rdb.addConnection("localhost", 28017, "test"); // this one should not exist
		rdb.addConnection("localhost", 28018, "test"); // this one should not exist

		rdb.initialize();

		for (int i = 0; i < 1000; i ++) {
			try (Connection conn = rdb.getConnection()) {
				assertTrue(conn.isOpen());
				assertEquals("localhost", conn.hostname);

				log.info("Got connection: " + conn.hostname + ":" + conn.port);

				Thread.sleep(1000);
			}
		}
	}
}