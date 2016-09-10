package com.rethinkdb.orm.entities;

import com.rethinkdb.orm.annotations.Id;
import com.rethinkdb.orm.annotations.IgnoreNull;
import com.rethinkdb.orm.annotations.Timestamp;

import java.time.OffsetDateTime;

/**
 *
 */
public class EntityWithIgnore {

	@Id
	public String id;

	@IgnoreNull
	@Timestamp
	public Long time;

	@IgnoreNull
	public @Timestamp OffsetDateTime offsetTime;
}
