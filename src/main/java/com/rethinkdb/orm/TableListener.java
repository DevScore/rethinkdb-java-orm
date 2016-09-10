package com.rethinkdb.orm;

import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * General purpose database listener
 */
public abstract class TableListener<T> implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(TableListener.class);

	protected final RDB rdb;

	protected final Class<T> clazz;

	private boolean isRunning;

	private boolean shutdown;

	private Cursor cursor;

	private Connection connection;

	public TableListener(RDB rdbProvider, Class<T> tableClazz) {

		rdb = rdbProvider;
		clazz = tableClazz;
	}

	/**
	 * provide hook to table that waits for changes
	 */
	protected abstract Cursor getCursor(Connection connection);

	/**
	 * @param newValue new value inserted into table
	 */
	protected abstract void trigger(T newValue);

	@Override
	public void run() {

		shutdown = false;
		isRunning = true;

		// hook to table and wait ...
		connection = rdb.getConnection();
		cursor = getCursor(connection);

		try {

			for (Object change : cursor) {

				HashMap map = (HashMap) change;
				Object newValue = map.get("new_val");

				// notification was either inserted or updated ... send to clients if any
				// trigger action ...
				if (newValue != null) {
					T job = rdb.map(clazz, (Map<String, Object>) newValue);
					trigger(job);
				}
			}

			cursor.close();
			connection.close();
		}
		catch (Exception e) {
			cursor.close();
			connection.close();

			if (!shutdown) {
				log.info("Listener: " + this.getClass().getSimpleName() + " CLOSED by terminating thread!");
			}
			else {
				log.error("Exception thrown while listening: " + e);
			}
		}

		isRunning = false;
	}

	public void shutdown() {

		log.info("Manually stopping listening ... ");
		if (connection != null &&
			connection.isOpen() &&
			cursor != null) {

			shutdown = true;

			cursor.close();
			connection.close();
		}
	}

	public boolean isRunning() {

		return isRunning;
	}
}
