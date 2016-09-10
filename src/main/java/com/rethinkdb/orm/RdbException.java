package com.rethinkdb.orm;

public class RdbException extends RuntimeException {

	public RdbException(Error error, String msg) {
		this.error = error;
		this.msg = msg;
	}

	public static RdbException parseDbError(String errorMsg) {

		if (errorMsg.startsWith("Duplicate primary key ")) {
			throw new RdbException(Error.DuplicatePrimaryKey, errorMsg);
		}
		throw new RdbException(Error.Undefined, errorMsg);
	}

	public enum Error {
		Undefined,
		DuplicatePrimaryKey,
		UnexpectedPropertyType,
		UnexpectedReturnType,
		ClassMappingError,
		IndexNotDefined,
		ConfigMismatch,
		Timeout,
		NoDatabaseConnection
	}

	public final Error error;

	public final String msg;

	@Override
	public String toString() {
		return "RdbException{" +
				"error=" + error +
				", msg='" + msg + '\'' +
				'}';
	}
}
