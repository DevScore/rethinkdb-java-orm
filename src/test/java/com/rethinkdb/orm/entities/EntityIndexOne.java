package com.rethinkdb.orm.entities;

import com.rethinkdb.orm.annotations.Id;
import com.rethinkdb.orm.annotations.Indexed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class EntityIndexOne {

	@Id
	public String id;

	@Indexed
	public int one;

	public String name;

	@Indexed
	public Map<String, String> items = new HashMap<>();

	@Indexed
	public List<String> list = new ArrayList<>();
}
