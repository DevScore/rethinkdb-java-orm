package com.rethinkdb.orm.entities;

import com.rethinkdb.orm.annotations.DbName;
import com.rethinkdb.orm.annotations.Id;

@DbName("NonExistingDbName")
public class EntityNonExistingDbName {

	@Id
	public Long userId;

}
