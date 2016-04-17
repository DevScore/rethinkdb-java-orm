package com.rethinkdb.orm;

import java.util.List;
import java.util.Map;

public class Response {

	long deleted;
	long inserted;
	long unchanged;
	long replaced;
	long errors;
	long skipped;
	List generated_keys;

	public static Response parse(Map<String, Object> properties){
		return null;
	}
}
