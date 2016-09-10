package com.rethinkdb.orm;

import java.util.Collection;
import java.util.Map;

public class BigMap<K, V> {

	private final RDB rdb;
	private final String tableName;
	private String propertyName;
	private final Object entityId;

//	private  subs

	/**
	 * Initializes a BigMap
	 *
	 * @param rdb
	 * @param tableName
	 * @param propertyName
	 * @param entityId
	 */
	public BigMap(RDB rdb, String tableName, String propertyName, Object entityId) {
		this.rdb = rdb;
		this.tableName = tableName;
		this.propertyName = propertyName;
		this.entityId = entityId;
	}

	public void put(K key, V item) {

	}

	public void putAll(Map<K, V> items) {

	}

	public Map<K, V> getAll() {
		throw new IllegalStateException("Not implemented yet.");
	}

	public Collection<V> values() {
		throw new IllegalStateException("Not implemented yet.");
	}
}
