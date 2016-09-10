package com.rethinkdb.orm.utils;

/**
 * An interface for an incremental Hasher
 */
public interface IncrementalHasher {

	IncrementalHasher add(byte b);
	IncrementalHasher add(byte[] b);
	IncrementalHasher add(short b);
	long[] hash();
}
