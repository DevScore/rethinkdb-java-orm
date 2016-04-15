package com.rethinkdb.orm.converters;


import com.rethinkdb.orm.Converter;
import java.util.List;

public class ListConverter implements Converter<List, List> {

	public List fromProperty(List list) {
		return list;
	}

	public List fromField(List fieldValue) {
		return fieldValue;
	}

}
