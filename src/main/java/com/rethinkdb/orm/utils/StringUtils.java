package com.rethinkdb.orm.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {

	private StringUtils() {
		// hiding constructor
	}

	/**
	 * Checks if string is <code>null</code> or <code>empty == ""</code>
	 *
	 * @param value string to test
	 * @return <code>true</code> if <code>null</code> or empty,
	 * <code>false</code> otherwise.
	 */
	public static boolean isNullOrEmpty(String value) {

		return (value == null || value.length() == 0);
	}

	/**
	 * Checks if string is null, empty or contains only spaces
	 *
	 * @param value string to test
	 * @return <code>true</code> if <code>null</code>, empty or spaces only,
	 * <code>false</code> otherwise.
	 */
	public static boolean isNullOrEmptyTrimmed(String value) {

		return (value == null || value.trim().length() == 0);
	}

	/**
	 * compares two strings if equal ({@code String.equals} method)
	 *
	 * @param original string to compare against
	 * @param compare  string to compare to
	 * @return <code>true</code> if equal or both <code>null</code>,
	 * <code>false</code> otherwise
	 */
	public static boolean equals(String original, String compare) {

		return equals(original, compare, false);
	}

	/**
	 * compares two strings if equal ({@code String.equals} or
	 * {@code String.equalsIgnoreCase} method)
	 *
	 * @param original   string to compare against
	 * @param compare    string to compare to
	 * @param ignoreCase <code>true</code> equalsIgnoreCase is used, <code>false</code>
	 *                   equals is used
	 * @return <code>true</code> if equal or both <code>null</code>,
	 * <code>false</code> otherwise
	 */
	public static boolean equals(String original, String compare, boolean ignoreCase) {

		if (original == null && compare == null) {
			return true;
		}

		if (original != null) {
			if (ignoreCase) {
				return original.equalsIgnoreCase(compare);
			} else {
				return original.equals(compare);
			}
		}

		return false;
	}

	/**
	 * Compares two strings with <code>String.compare()</code>. Method allows
	 * <code>null</code> values.
	 *
	 * @param one to compare
	 * @param two to compare
	 * @return <b>0</b> if both <code>null</code>, <b>-1</b> if <code>one</code>
	 * is <code>null</code>, <b>1</b> if <code>two</code> is
	 * <code>null</code>, otherwise <code>String.compare()</code> is
	 * called
	 */
	public static int compare(String one, String two) {

		if (one == null && two == null) {
			return 0;
		}

		if (one == null) {
			return -1;
		}

		if (two == null) {
			return 1;
		}

		return one.compareTo(two);
	}

	/**
	 * null resilient trim
	 *
	 * @param value trims value or returns null if null
	 * @return null or trimmed value (empty string is left empty)
	 */
	public static String trim(String value) {

		return value != null ? value.trim() : null;
	}

	/**
	 * Additionally to outer <code>trim()</code> double spaces are removed.
	 *
	 * @param value to remove double spaces from
	 * @return value with single spaces if any
	 */
	public static String trimDoubleSpaces(String value) {

		if (isNullOrEmpty(value)) {
			return value;
		}

		value = value.trim();
		return value.replaceAll("\\s+", " ");
	}

	/**
	 * Additionally to outer <code>trim()</code> all inner spaces are removed.
	 *
	 * @param value to remove inner spaces from
	 * @return value with single spaces if any
	 */
	public static String trimInner(String value) {

		if (isNullOrEmpty(value)) {
			return value;
		}

		value = value.trim();
		return value.replaceAll("\\s+", "");
	}

	/**
	 * Trims only end of text
	 *
	 * @param text to be trimmed at the end
	 * @return trimmed text or original if no changes were applied
	 */
	public static String trimEnd(String text) {

		if (isNullOrEmpty(text)) {
			return text;
		}

		return text.replaceAll("\\s+$", "");
	}

	public static String trimStart(String text) {

		if (isNullOrEmpty(text)) {
			return text;
		}

		return text.replaceAll("^\\s+", "");
	}

	public static String trimAll(String text, String toBeTrimmed) {

		if (isNullOrEmpty(text) || isNullOrEmpty(toBeTrimmed)) {
			return text;
		}

		return text.replaceAll(toBeTrimmed, "");
	}

	/**
	 * Trims down text to null if empty
	 *
	 * @param text to be trimmed
	 * @return text or null if empty
	 */
	public static String trimToNull(String text) {

		text = trim(text);

		if (text == null || text.isEmpty()) {
			return null;
		}

		return text;
	}

	/**
	 * Capitalizes first character in given string
	 *
	 * @param input to capitalize first character
	 * @return capitalized string or null if null
	 */
	public static String capitalize(String input) {

		if (input == null) {
			return null;
		}

		if (input.length() > 1) {
			for (int i = 0; i < input.length(); i++) {
				if (Character.isAlphabetic(input.charAt(i))) {
					return input.substring(0, i) + Character.toString(input.charAt(i)).toUpperCase() + input.substring(i + 1);
				}
			}
		}

		return input.toUpperCase();
	}

	/**
	 * Joins list of string items to a single string, where items are separated
	 * with a defined separator.
	 *
	 * @param list      to join into string
	 * @param separator to be used between elements
	 * @return items joined into a single string
	 */
	public static String join(List<?> list, String separator) {

		if (separator == null || separator.equalsIgnoreCase("")) {
			throw new IllegalArgumentException("Missing separator!");
		}

		String output = "";

		if (list != null && list.size() > 0) {
			for (int i = 1; i <= list.size(); i++) {
				output = output + list.get(i - 1);
				if (i < list.size()) {
					output = output + separator;
				}
			}
		}

		return output;
	}

	/**
	 * Joins list of string items to a single string, where items are separated
	 * with a defined separator.
	 *
	 * @param args      to join into string
	 * @param separator to be used between elements
	 * @return items joined into a single string
	 */
	public static String join(Object[] args, String separator) {

		if (separator == null || separator.equalsIgnoreCase("")) {
			throw new IllegalArgumentException("Missing separator!");
		}

		if (args != null && args.length > 0) {
			List<Object> array = Arrays.asList(args);
			return join(array, separator);
		}

		return "";
	}


	/**
	 * Joins list of string items to a single string, where items are separated
	 * with a defined separator.
	 *
	 * @param items     to join into string
	 * @param separator to be used between elements
	 * @return items joined into a single string
	 */
	public static String join(Set<?> items, String separator) {

		if (separator == null || separator.equalsIgnoreCase("")) {
			throw new IllegalArgumentException("Missing separator!");
		}

		if (items != null && items.size() > 0) {
			Object[] array = items.toArray();
			return join(array, separator);
		}

		return "";
	}


	public static String join(HashMap<String, String> params, String separator) {

		if (separator == null || separator.equalsIgnoreCase("")) {
			throw new IllegalArgumentException("Missing separator!");
		}

		String output = "";
		if (params != null) {
			for (String name : params.keySet()) {
				if (output.length() > 0) {
					output = output + separator;
				}

				output = output + name + "=" + params.get(name);
			}
		}

		return output;
	}

	/**
	 * Extracts words from text removing non alpha characters
	 *
	 * @param text to extract words from
	 * @return list of found words or empty list if none found
	 */
	public static List<String> getWords(String text) {

		List<String> output = new ArrayList<>();
		if (isNullOrEmptyTrimmed(text)) {
			return output;
		}

		Pattern p = Pattern.compile("\\b\\p{L}+\\b");

		Matcher m = p.matcher(text);
		while (m.find()) {
			output.add(m.group());
		}

		return output;
	}

	/**
	 * Reduces text to max given size preserving words
	 *
	 * @param text to trim down
	 * @param size max desired size
	 * @return trimmed down text with "..." or original text if fitting size
	 */
	public static String trimTextDown(String text, int size) {

		if (text.length() <= size) {
			return text;
		}

		int pos = text.lastIndexOf(" ", size);

		if (pos < 0) {
			return text.substring(0, size);
		}

		return text.substring(0, pos);
	}

	public static String trimTextDown(String text, int size, String append) {

		if (text.length() <= size) {
			return text;
		}

		size = size - append.length();

		int pos = text.lastIndexOf(" ", size);

		if (pos < 0) {
			return text.substring(0, size) + append;
		}

		return text.substring(0, pos) + append;
	}

	public static String toStringOrNull(Object value) {

		return null != value ? value.toString() : null;
	}

	public static List<String> asListOfChars(String value) {

		List<String> list = new ArrayList<>();

		if (StringUtils.isNullOrEmptyTrimmed(value)) {
			return list;
		}

		for (int i = 0; i < value.length(); i++) {
			list.add(Character.toString(value.charAt(i)));
		}

		return list;
	}

	/**
	 * Checks if given string is a single word (doesn't accepts words with "-" as a single word!)
	 *
	 * @param word to test
	 * @return true if word, false otherwise
	 */
	public static boolean isWord(String word) {

		if (isNullOrEmptyTrimmed(word)) {
			return false;
		}

		List<String> list = getWords(word);
		return list.size() == 1;
	}

	/**
	 * Calculates matching relevance between given string and search expression
	 *
	 * @param value  to search in
	 * @param search to search for
	 * @return -1 not relevant, 0..N - where lower values represents more relevant results
	 */
	public static int relevance(String value, String search) {

		if (StringUtils.isNullOrEmptyTrimmed(value) ||
				StringUtils.isNullOrEmptyTrimmed(search)) {
			return -1;
		}

		if (search.length() > value.length()) {
			return -1;
		}

		int relevance = -1; // start at -1 ... so -1 is returned for no result
		int delta = 1; // first delta at 1 ... producing a sum of 0 when first letter is found

		int len = value.length();
		int searchLen = search.length();
		int letterIndex = 0;

		for (int searchIndex = 0; searchIndex < searchLen; searchIndex++) {

			char match = search.charAt(searchIndex);

			while (letterIndex < len) {
				char letter = value.charAt(letterIndex);
				letterIndex++;

				if (match == letter) {
					relevance = relevance + delta; // reverse to get higher value for better match
					delta = 0;
					break;
				} else {
					delta++;
				}
			}

			// we matched all characters ... and found the last one ...
			if (delta == 0 &&
					searchIndex == searchLen - 1) {
				return relevance;
			}

			if (letterIndex == len) {
				break;
			}
		}

		return -1;
	}

	/**
	 * Removes all multiple-consecutive whitespace characters (space, tab, newline) and replaces them with single space.
	 * Also removes leading and trailing spaces.
	 *
	 * @param input
	 * @return
	 */
	public static String sanitizeWhitespace(String input) {

		input = input.replaceAll("\\s+", " ");
		return input.trim();
	}

	/**
	 * Removes all multiple-consecutive whitespace characters (space, tab, newline) and replaces them with single space.
	 * Also removes leading and trailing spaces.
	 *
	 * @param input
	 * @return
	 */
	public static byte[] sanitizeWhitespace(byte[] input) {

		try {
			String stringFileData = new String(input, "utf-8");
			return sanitizeWhitespace(stringFileData).getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String linesBeforeEmptyLine(String lines) {

		if (lines == null) {
			return null;
		}

		BufferedReader reader = new BufferedReader(new StringReader(lines));
		StringBuilder out = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					return out.toString();
				}

				// not first line?
				if (out.length() != 0) {
					out.append("\r\n");
				}
				out.append(line);
			}
		} catch (IOException e) {
			// should not happen - we are reading from a string
		}
		return out.toString();
	}
}