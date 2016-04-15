package com.rethinkdb.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Flags a field that holds set name of the Record
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface TableName {
	/**
	 * @return The SetName used for this Class. If empty, the short class name is used as default.
	 */
	String value() default "";
}

