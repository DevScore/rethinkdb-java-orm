package com.rethinkdb.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Flags a field should be serialised to JSON
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface FieldName {
	/**
	 * The field name to which this filed is mapped
	 * @return Name of the field. Default value is empty string indicating to use the java field name as document field name.
	 */
	String value() default "";
}

