package com.rethinkdb.orm.entities;

import com.rethinkdb.orm.annotations.Id;

import java.time.OffsetDateTime;
import java.util.Date;

public class EntityDate {
	@Id
	public String id;
	public Date now;
	public Date past;
	public Date epoch;
	public Date iso;
}
