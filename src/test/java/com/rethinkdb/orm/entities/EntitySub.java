package com.rethinkdb.orm.entities;

import java.util.Date;

@SuppressWarnings("SameParameterValue")
public class EntitySub {

	public EntitySub() {
	}

	public EntitySub(int first, String second, Date date) {
		this.first = first;
		this.second = second;
		this.date = date;
	}

	public int first;
	public String second;

	public Date date;

}
