package com.rethinkdb.orm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rethinkdb.orm.types.JsonBase;
import org.json.simple.JSONObject;

import java.io.IOException;

public class JsonUtils {

	private static final String reqlTypeKey = "$reql_type$";

	public enum ReqlType {GEOMETRY}

	public static Object fromJsonObject(JSONObject json) {
		String jsonString = json.toJSONString();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonBase obj = null;
		try {
			obj = objectMapper.readValue(jsonString, JsonBase.class);

			System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return obj;
	}

//	public static Class getJsonObjectType(JSONObject json) {
//
//		if (!json.containsKey(reqlTypeKey)) {
//			throw new IllegalArgumentException("ReQL JSON does not contain $reql_type$ key. JSON: " + json.toJSONString());
//		}
//
//		String typeString = (String) json.get(reqlTypeKey);
//
//		try {
//			ReqlType jsonType = ReqlType.valueOf(typeString);
//		} catch (IllegalArgumentException iae) {
//			throw new IllegalStateException("Undefined Reql JSON type: " + typeString);
//		}
//
//		switch (typeString)
//	}
}
