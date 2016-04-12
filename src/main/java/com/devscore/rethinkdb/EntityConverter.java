package com.devscore.rethinkdb;

import com.rethinkdb.net.PojoConverter;

import java.util.Map;

public class EntityConverter implements PojoConverter {

	@Override
	public boolean willConvert(Class clazz) {
		return false;
	}

	@Override
	public <T> T toPojo(Class<T> clazz, Map<String, Object> properties) {
		return null;
	}

	@Override
	public Map<String, Object> fromPojo(Object object) {
		return null;
	}
}
