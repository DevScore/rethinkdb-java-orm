package com.rethinkdb.orm.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "$reql_type$")
@JsonSubTypes({
		@Type(value = GeoData.class, name = "GEOMETRY"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonBase {
}
