package com.rethinkdb.orm;

public interface Converter<FIELD, PROPERTY> {

	Long DB_MAX_VAL = (long) Math.pow(2, 53);
	Long DB_MIN_VAL = (long) -Math.pow(2, 53);

	FIELD fromProperty(PROPERTY property);

	PROPERTY fromField(FIELD fieldValue);

	default Object getPropertyComponentValue(Object fieldComponentValue) {
		return fromField((FIELD) fieldComponentValue);
	}
}
