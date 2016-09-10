package com.rethinkdb.orm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Response {

	private static final Logger log = LoggerFactory.getLogger(Response.class);

	public final long deleted;
	public final long inserted;
	public final long unchanged;
	public final long replaced;
	public final long errors;
	public final long skipped;
	public final List generatedKeys;

	public Response(long deleted, long inserted, long unchanged, long replaced, long errors, long skipped, List generated_keys) {
		this.deleted = deleted;
		this.inserted = inserted;
		this.unchanged = unchanged;
		this.replaced = replaced;
		this.errors = errors;
		this.skipped = skipped;
		this.generatedKeys = generated_keys == null ? Collections.emptyList() : generated_keys;
	}


	public static Response parse(Map<String, Object> props) {

		Long deleted = (Long) props.get("deleted");
		Long inserted = (Long) props.get("inserted");
		Long unchanged = (Long) props.get("unchanged");
		Long replaced = (Long) props.get("replaced");

		Long errors = (Long) props.get("errors");
		if (!errors.equals(0L)) {
			StringBuilder sb = new StringBuilder("DB error happened:\n");
			for (String key : props.keySet()) {
				sb.append("  ").append(key).append(":").append(props.get(key)).append("\n");
			}
			// TODO: put to debug when in production
			log.warn("DB error response:\n" + sb.toString());
			String errorMsg = (String) props.get("first_error");
			throw RdbException.parseDbError(errorMsg);
		}

		Long skipped = (Long) props.get("skipped");
		List generatedKeys = (List) props.get("generated_keys");

		return new Response(deleted, inserted, unchanged, replaced, errors, skipped, generatedKeys);
	}

	@Override
	public String toString() {
		return "Response{" +
				"deleted=" + deleted +
				", inserted=" + inserted +
				", unchanged=" + unchanged +
				", replaced=" + replaced +
				", errors=" + errors +
				", skipped=" + skipped +
				", generated_keys=" + generatedKeys +
				'}';
	}
}
