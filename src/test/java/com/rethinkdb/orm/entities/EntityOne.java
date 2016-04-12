package com.rethinkdb.orm.entities;

import com.rethinkdb.orm.annotations.*;

import java.util.*;

public class EntityOne {

	@Id
	public Long userId;

	@Indexed(name = "index_one") // type = IndexType.NUMERIC)
	public int one;

	@Indexed
	public String two;

	@FieldName("third")  // explicitly set the name of the bin	public double three;
	public double three;
	public float four;
	private short five;
	private byte six;

	@Indexed
	public boolean seven;

	public Date eight;

	@Indexed
	public List<String> nine;

	public Map ten;
	public EntityEnum eleven;
	public Set twelve;
	public byte[] thirteen;

	public EntitySub sub;

	@AnyProperty
	public final Map<String, Object> unmapped = new HashMap<>();

	// un-mappable class must be ignored
	@Ignore
	public java.util.Calendar calendar;

	@Ignore
	public String ignored;

	public short getFive() {
		return five;
	}

	public void setFive(short five) {
		this.five = five;
	}

	public byte getSix() {
		return six;
	}

	public void setSix(byte six) {
		this.six = six;
	}
}
