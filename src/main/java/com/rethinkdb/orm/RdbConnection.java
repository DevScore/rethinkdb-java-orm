package com.rethinkdb.orm;

import com.rethinkdb.gen.exc.ReqlDriverError;
import com.rethinkdb.net.Connection;
import com.rethinkdb.orm.utils.Assert;
import com.rethinkdb.orm.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class RdbConnection {

	private static final Logger log = LoggerFactory.getLogger(RdbConnection.class);

	private static final long CONNECTION_TIME_OUT = 10_000L; // 10 seconds

	private static final long WAIT_UNTIL_RETRY = 30_000L; // 30 seconds to retry connecting if failed

	private final String key;

	/**
	 * Indicating that connection was at least established once
	 * true - active, false - node is down or connection could not be provided
	 */
	private boolean active;

	private long lastConnect;

	private final Connection.Builder builder;

	public RdbConnection(String host, int port, String database, String authenticationKey) {

		key = host + ":" + port;

		builder = Connection.build()
			.hostname(host)
			.port(port)
			.timeout(CONNECTION_TIME_OUT)
			.db(database);

		// add Auth key if provided
		if (!StringUtils.isNullOrEmptyTrimmed(authenticationKey)) {
			builder.authKey(authenticationKey);
		}

		// should be true
		Assert.isTrue(CONNECTION_TIME_OUT < WAIT_UNTIL_RETRY, "Time out must be smaller than retry sleep time!");

		active = false;
		lastConnect = 0;
	}

	public String getKey() {

		return key;
	}

	public boolean is(String host, int port) {

		return key.equals(host + ":" + port);
	}

	/**
	 * @return true if connection is ready to be created, false if connection should be skipped until some time passes
	 */
	public boolean isAvailable() {

		return active ||
			lastConnect + WAIT_UNTIL_RETRY < System.currentTimeMillis();
	}

	public Connection connect() {

		lastConnect = System.currentTimeMillis();

		try {
//			log.debug("Connecting to: " + getKey());
			Connection connection = builder.connect();
//			active = (connection != null && connection.isOpen());
			return connection;
		}
		catch (ReqlDriverError rde) {
			log.warn("ReqlDriverError: " + rde.getMessage() + (rde.getBacktrace().isPresent() ? " backtrace:" + rde.getBacktrace().get().toString() + " " : " " + getKey()));
			active = false;
			return null;
		}
	}
}
