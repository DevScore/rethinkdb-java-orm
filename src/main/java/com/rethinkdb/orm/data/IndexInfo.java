package com.rethinkdb.orm.data;

import com.rethinkdb.orm.ClassMapper;

import java.util.HashMap;

/**
 *
 */
public class IndexInfo {

	private final String table;

	private final String name;

	private final boolean geo;

	private final boolean outdated;

	private final boolean ready;

	private final boolean multi;

	public IndexInfo(Class clazz, HashMap<String, Object> status) {

		ClassMapper mapper = ClassMapper.getMapper(clazz);

		table = mapper.getTableName();
		geo = (boolean) status.get("geo");
		outdated = (boolean) status.get("outdated");
		ready = (boolean) status.get("ready");
		name = (String) status.get("index");
		multi = (boolean) status.get("multi");
	}

	public boolean isMulti() {

		return multi;
	}

	public String getName() {

		return name;
	}

	public boolean isReady() {

		return ready;
	}

	public boolean isOutdated() {

		return outdated;
	}

	public boolean isGeo() {

		return geo;
	}

	@Override
	public String toString() {

		return table + " . " + name + " (outdated=" + outdated + ", geo=" + geo + ", multi=" + multi + ")";
	}
}
