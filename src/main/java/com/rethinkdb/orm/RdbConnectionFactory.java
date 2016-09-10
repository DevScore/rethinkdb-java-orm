package com.rethinkdb.orm;

import com.rethinkdb.net.Connection;
import com.rethinkdb.orm.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dynamic connection factory with connection checking ... and refresh logic
 */
public class RdbConnectionFactory {

	private static final Logger log = LoggerFactory.getLogger(RdbConnectionFactory.class);

	private static final int MAX_CONNECTIONS = 100;

	private ConcurrentLinkedQueue<RdbConnection> availableConnections = new ConcurrentLinkedQueue<>();
	private ConcurrentLinkedQueue<RdbConnection> activeConnections = new ConcurrentLinkedQueue<>();

	private static final int MAX_RETRIES = 5;
	private static AtomicInteger retryCount = new AtomicInteger(0);

	private String database = null;

	public String getDbName() {
		return database;
	}

	/**
	 * Adds new connection to thread pool in an asynchronous way
	 *
	 * @param host              host name
	 * @param port              host port
	 * @param databaseName      database name
	 * @param authenticationKey key to access or null if not necessary
	 */
	public void addConnection(String host, int port, String databaseName, String authenticationKey) {

		if (database == null || StringUtils.equals(databaseName, database)) {
			database = databaseName;
		} else {
			throw new RdbException(RdbException.Error.ConfigMismatch,
					"Can't configure multiple databases in one connection pool. Use multiple connection pools instead!");
		}

//		if (allConnections.get() >= MAX_CONNECTIONS) {
		if (availableConnections.size() >= MAX_CONNECTIONS) {
			throw new RdbException(RdbException.Error.ConfigMismatch, "Max number of active connection reached: " + MAX_CONNECTIONS + "!");
		}

		// check if connection is already present before triggering new insert
		RdbConnection conn = find(host, port);
		if (conn != null) {
			log.warn("Connection to: " + host + ":" + port + ", already configured!");
			return;
		}

//		int index = allConnections.get(); // add new
//		connections.set(index, new RdbConnection(host, port, databaseName, authenticationKey));
//		allConnections.incrementAndGet();
//		activeConnections.add(conn);
		availableConnections.add(new RdbConnection(host, port, databaseName, authenticationKey));
	}

	/**
	 * Returns open and valid connection from list of connections ...
	 * if connection is down then a refresh is triggered ...
	 *
	 * @return connection or throws exception ...
	 */
	public Connection getConnection() throws RdbException {
//		String host = "localhost";
//		return RethinkDB.r.connection().hostname(host).db("devscore").connect();

//		int all = allConnections.get();
//		if (all > 0) {
//
//			// loop over all connection staring and current index ...
//			for (int count = 0; count < all; count++) {
//
//				int index = currentConnectionIndex.getAndIncrement();
//				RdbConnection connection = connections.get(index);
//
//				if (currentConnectionIndex.get() >= all) {
//					currentConnectionIndex.set(0);
//				}
//
//				if (connection != null &&
//					connection.isAvailable()) {
//					Connection conn = connection.connect();
//
//					// OK we have found a valid open connection ... let's return it
//					if (conn != null && conn.isOpen()) {
//						return conn;
//					}
//				}
//			}
//		}

		while (!activeConnections.isEmpty()) {
			// get first
			RdbConnection connection = activeConnections.peek();
			// try to connect
			Connection conn = connection.connect();
			// OK we have found a valid open connection ... let's return it
			if (conn != null) {
				log.debug("RDB connected to: {} ({})", connection.getKey(), activeConnections.size());
				// reset retries
				retryCount.set(0);
				return conn;
			} else {
				// remove it from active connections...
				activeConnections.remove(connection);
				log.warn("Removing connection from ACTIVE queue: {} ({})", connection.getKey(), activeConnections.size());
			}
		}

		// last retry
		int retries = retryCount.incrementAndGet();
		if (retries < MAX_RETRIES || retries % 30 == 0) {
			/// initialize again...
			log.info("Re-initializing RDB connections... (# retries: {})", retries);
			initialize();
			// if success, return...
			if (!activeConnections.isEmpty()) {
				RdbConnection connection = activeConnections.peek();
				log.info("RDB connected to: {} ({})", connection.getKey(), activeConnections.size());
				// reset retries
				retryCount.set(0);
				return activeConnections.peek().connect();
			}
		}


		throw new RdbException(RdbException.Error.NoDatabaseConnection, "Could not get any database connection! (# retries=" + retryCount.get() + ")");
	}

	/**
	 * Waits until first connection is available ...
	 * if no connection is available exception is thrown
	 */
	public void initialize() throws RdbException {
		log.info("Initializing RDB connections... Available: {}", availableConnections.size());
//		int max = allConnections.get();
		int max = availableConnections.size();

		ExecutorService service = Executors.newFixedThreadPool(max);

		Set<Callable<RdbConnection>> callables = new HashSet<>();

//		for (int index = 0; index < max; index++) {
//			int finalIndex = index;
		for (RdbConnection connection : availableConnections) {
			callables.add(() -> {
				try (Connection conn = connection.connect()) {
					if (conn != null && conn.isOpen()) {
						activeConnections.add(connection);
						return connection;
					} else {
						throw new RdbException(RdbException.Error.NoDatabaseConnection, "Could not connect to: " + connection.getKey());
					}
				}

			});
		}
//		}

		try {
			RdbConnection gotConnection = service.invokeAny(callables);
			if (gotConnection != null) {
				log.info("Got RDB connection: {} ({})", gotConnection.getKey(), activeConnections.size());
			} else {
				throw new RdbException(RdbException.Error.NoDatabaseConnection, "No database connection available!");
			}
		} catch (InterruptedException | ExecutionException e) {
			log.error("Cannot connect to RDB: ", e);
			throw new RdbException(RdbException.Error.NoDatabaseConnection, "No database connection available! " + e);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//

	private RdbConnection find(String host, int port) {

		for (RdbConnection conn : availableConnections) {
			if (conn.is(host, port)) {
				return conn;
			}
		}
		return null;
	}


}
