package com.rethinkdb.orm.types;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = GeoPoint.class, name = "Point"),
		@JsonSubTypes.Type(value = GeoLine.class, name = "LineString"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoData extends JsonBase{

	public enum Type {Point, LineString, Polygon }

	public Type type;
}
