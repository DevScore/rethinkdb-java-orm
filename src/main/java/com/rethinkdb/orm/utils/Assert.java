package com.rethinkdb.orm.utils;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Method parameter check helper
 * add additional check when needed
 */
public class Assert {

	protected Assert() {
	}

	public static void isTrue(boolean test, String message) {

		if (!test) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void isFalse(boolean test, String message) {

		isTrue(!test, message);
	}

	public static void isNull(Object test, String message) {

		isTrue(test == null, message);
	}

	public static void notNull(Object test, String message) {

		isFalse(test == null, message);
	}

	public static void notNullOrEmptyTrimmed(String value, String message) {

		isFalse(StringUtils.isNullOrEmptyTrimmed(value), message);
	}

	public static <T> void matchesAll(Set<T> set, Predicate<? super T> predicate, T... values){
		Stream<T> vals = Arrays.stream(values);
		isTrue(set.stream().allMatch(obj -> vals.anyMatch(predicate)), "Given set does not match values via the given predicate.");
	}
}
