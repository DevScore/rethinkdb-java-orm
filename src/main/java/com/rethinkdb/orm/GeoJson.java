package com.rethinkdb.orm;

public class GeoJson {

	public enum Type {Point, LineString, Polygon}

	public Type type;

	public Float[][] coordinates;

}
