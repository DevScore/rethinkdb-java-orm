package com.rethinkdb.orm.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Utilities to help using date and time
 */
public class DateTimeUtils {

	public final static long ONE_MINUTE = 60L * 1000L;

	public final static long ONE_HOUR = 60L * ONE_MINUTE;

	public final static long ONE_DAY = 24L * ONE_HOUR;

	private DateTimeUtils() {
		// hiding constructor
	}

	private static ThreadLocal<SimpleDateFormat> simpleDateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {

			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};

	private static ThreadLocal<SimpleDateFormat> simpleTimeFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {

			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		}
	};

	public static SimpleDateFormat getDateFormat() {

		return simpleDateFormatThreadLocal.get();
	}

	public static SimpleDateFormat getTimeFormat() {

		return simpleTimeFormatThreadLocal.get();
	}

	public static Calendar getCalendar() {

		return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	}

	public static Calendar getCalendar(long time) {

		Calendar calendar = getCalendar();
		calendar.setTimeInMillis(time);
		return calendar;
	}

	public static String formatDateTime(long time) {

		SimpleDateFormat format = getTimeFormat();
		Calendar calendar = getCalendar(time);

		return format.format(calendar.getTime());
	}

	public static String formatDate(long time) {

		SimpleDateFormat format = getDateFormat();
		Calendar calendar = getCalendar(time);

		return format.format(calendar.getTime());
	}

	public static String format(long time, SimpleDateFormat format) {

		if (format == null) {
			return formatDateTime(time);
		}

		Calendar calendar = getCalendar(time);
		return format.format(calendar.getTime());
	}

	public static long getTimezoneTime(long time, int timezone) {

		Calendar calendar = getCalendar(time);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);

		hour = (hour + timezone) % 24;
		if (hour < 0) {
			hour = 24 + hour;
			calendar.add(Calendar.DAY_OF_MONTH, -1);
		}

		calendar.set(Calendar.HOUR_OF_DAY, hour);
		return calendar.getTimeInMillis();
	}

	/**
	 * Converts local hour back to UTC hour
	 *
	 * @param hour     local hour in time zone
	 * @param timezone current time zone
	 * @return UTC hour
	 */
	public static int getUtcHour(int hour, int timezone) {

		hour = (hour - timezone) % 24;
		return (hour < 0) ? 24 + hour : hour;
	}

	/**
	 * Converts UTC hour to time zone hour
	 *
	 * @param hour     UTC hour
	 * @param timezone time zone
	 * @return hour as seen in the given time zone
	 */
	public static int getTimezoneHour(int hour, int timezone) {

		hour = (hour + timezone) % 24;
		return (hour < 0) ? 24 + hour : hour;
	}

	/**
	 * Converts timestamp into OffsetDatetime
	 * @param timestamp to be converted
	 * @return OffsetDatetime representation of timestamp
	 */
	public static OffsetDateTime toOffsetDateTime(long timestamp) {
		Calendar calendar = DateTimeUtils.getCalendar(timestamp);
		return OffsetDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
	}

	public static long fromOffsetDateTime(OffsetDateTime timestamp) {

		Assert.notNull(timestamp, "Missing offset time stamp!");
		return timestamp.toInstant().toEpochMilli();
	}

	/**
	 * Gets first millisecond of first day in month
	 * @param time to get first millisecond
	 * @return first millisecond of month for given time
	 */
	public static long getMonthStart(long time) {

		LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC);
		dateTime = dateTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).with(ChronoField.MILLI_OF_SECOND, 0);
		return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
	}

	/**
	 * Returns last millisecond of last day in month ... +1 = next first day in month
	 * @param time to get last second in month
	 * @return last millisecond of month for given time
	 */
	public static long getMonthEnd(long time) {

		LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC);
		dateTime = dateTime.withDayOfMonth(1).withHour(23).withMinute(59).withSecond(59).with(ChronoField.MILLI_OF_SECOND, 999);
		dateTime = dateTime.plus(1, ChronoUnit.MONTHS).minus(1, ChronoUnit.DAYS);
		return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
	}
}