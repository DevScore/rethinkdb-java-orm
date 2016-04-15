package com.rethinkdb.orm.entities;

import com.rethinkdb.orm.annotations.*;

import java.util.*;

@DbName("NonExistingDbName")
public class EntityNonExistingDbName {

	@Id
	public Long userId;

}
