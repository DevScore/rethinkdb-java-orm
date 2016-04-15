package com.rethinkdb.orm;

import com.rethinkdb.gen.exc.ReqlDriverError;
import com.rethinkdb.net.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CPool {

	private static final Logger log = LoggerFactory.getLogger(CPool.class);

	private static Map<String, Connection.Builder> connBuilders = new HashMap<>();

	private static List<Connection> connectionPool = new ArrayList<>();

	private static int connIndex;

	/**
	 * @param builder
	 */
	public synchronized static void addConnection(Connection.Builder builder) {

		Connection conn = null;
		try {
			conn = builder.connect();
		} catch (ReqlDriverError rde) {
			log.warn("ReqlDriverError: " + rde.getMessage() + (rde.getBacktrace().isPresent() ? " backtrace:" + rde.getBacktrace().get().toString() : ""));
			return;
		}

		if (conn == null && !conn.isOpen()) {
			log.warn("Could not open connection to: " + conn.hostname + " port:" + conn.port);
			return;
		}

		String hostnameKey = conn.hostname + ":" + conn.port;

		// only one connection per hostname
		if (!connBuilders.containsKey(hostnameKey)) {
			connBuilders.put(hostnameKey, builder);
			connectionPool.add(conn);
		}

	}

	public synchronized static Connection getConnection() {

		if (connectionPool.isEmpty()) {
			throw new IllegalStateException("ConnectionPool is empty. " +
					"Before first use you must add connections to the pool via ConnectionPool.addConnection(..).");
		}

		Connection conn = connectionPool.get(connIndex);

		// loop index through a pool
		connIndex++;
		if (connIndex >= connectionPool.size()) {
			connIndex = 0;
		}

		return conn;
	}


	/**
	 * Closes all connections and clears connection settings.
	 * After this method the connection pool will not be usable until addConnection() is called at leas once.
	 */
	public static void clearConnections() {
		for (Connection connection : connectionPool) {
			connection.close();
		}
		connBuilders.clear();
		connectionPool.clear();
	}


}
