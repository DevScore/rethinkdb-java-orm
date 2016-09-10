package com.rethinkdb.orm.types;

import com.rethinkdb.orm.utils.MurmurHash3;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Hash {

	private final byte[] hashBytes;

	private Hash(byte[] hashBytes) {
		if (hashBytes.length > 32) {
			throw new IllegalArgumentException("Error: hash too long. Max hash length is 32 bytes. Requested length: "+hashBytes.length);
		}
		this.hashBytes = hashBytes;
	}

	private Hash(Long hashLong) {
		this.hashBytes = longToByteArray(hashLong);
	}

	private Hash(String hexStringHash) {
		this.hashBytes = DatatypeConverter.parseHexBinary(hexStringHash);
	}

	// CREATOR METHODS

	public static Hash wrap(byte[] hashBytes) {
		return new Hash(hashBytes);
	}

	public static Hash wrap(Long hashLong) {
		return new Hash(hashLong);
	}

	public static Hash wrap(String hexStringHash) {
		return new Hash(hexStringHash);
	}

	public static Hash wrap(long[] hashLongs) {
		return new Hash(longsToByteArray(hashLongs));
	}

	public static Hash hashContents(byte[] contents) {
		return new Hash(byteArrayToHash(contents));
	}

	// GETTERS

	public byte[] getHash() {
		return hashBytes;
	}

	public Long getHashAsLong() {
//		if (hashBytes.length != 8) {
//			throw new IllegalStateException("Requested Long, but internal byte array is " + hashBytes.length + " bytes long.");
//		}
		return byteArrayToLong(hashBytes);
	}

	public String getHashAsHexString() {
		return DatatypeConverter.printHexBinary(hashBytes);
	}

	private static byte[] longToByteArray(long value) {
		ByteBuffer bb = ByteBuffer.allocate(8);
		return bb.putLong(value).array();
	}

	private static byte[] longsToByteArray(long[] value) {
		ByteBuffer bb = ByteBuffer.allocate(8 * value.length);
		for (long val : value) {
			bb.putLong(val);
		}
		return bb.array();
	}

	private static long byteArrayToLong(byte[] value) {
		ByteBuffer bb = ByteBuffer.wrap(value);
		return bb.getLong();
	}

	private static byte[] byteArrayToHash(byte[] value) {
		return longsToByteArray(new MurmurHash3().add(value).hash());
	}

	public static Long getHash(String value) {

		try {
			return new MurmurHash3()
					.add(value.getBytes("utf-8"))
					.hash()[0];
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();  // should not happen - UTF-8 is always available
			return null;
		}
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Hash hash = (Hash) o;

		return Arrays.equals(hashBytes, hash.hashBytes);

	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(hashBytes);
	}
}
