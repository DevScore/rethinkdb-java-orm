package com.rethinkdb.orm;

public interface Converter<FIELD, PROPERTY> {

	FIELD fromProperty(PROPERTY property);

	PROPERTY fromField(FIELD fieldValue);

}
