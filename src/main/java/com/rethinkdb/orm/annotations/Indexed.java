package com.rethinkdb.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Indexed {

	/**
	 * @return Specific index name. Default is an automatically-generated value.
	 */
	String name() default "";

}
