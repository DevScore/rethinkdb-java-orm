package com.rethinkdb.orm.entities;

import com.rethinkdb.orm.annotations.Id;

import java.util.Arrays;

public class EntityArray {

	@Id
	public int[] id;

	public float[] floats;
	public double[] doubles;
	public byte[] bytes;
	public short[] shorts;
	public int[] ints;
	public long[] longs;
	public boolean[] booleans;

	@Override
	public String toString() {
		return "EntityArray{" +
				"id=" + Arrays.toString(id) +
				", floats=" + Arrays.toString(floats) +
				", doubles=" + Arrays.toString(doubles) +
				", bytes=" + Arrays.toString(bytes) +
				", shorts=" + Arrays.toString(shorts) +
				", ints=" + Arrays.toString(ints) +
				", longs=" + Arrays.toString(longs) +
				", booleans=" + Arrays.toString(booleans) +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EntityArray that = (EntityArray) o;

		if (!Arrays.equals(id, that.id)) return false;
		if (!Arrays.equals(floats, that.floats)) return false;
		if (!Arrays.equals(doubles, that.doubles)) return false;
		if (!Arrays.equals(bytes, that.bytes)) return false;
		if (!Arrays.equals(shorts, that.shorts)) return false;
		if (!Arrays.equals(ints, that.ints)) return false;
		if (!Arrays.equals(longs, that.longs)) return false;
		return Arrays.equals(booleans, that.booleans);

	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(id);
		result = 31 * result + Arrays.hashCode(floats);
		result = 31 * result + Arrays.hashCode(doubles);
		result = 31 * result + Arrays.hashCode(bytes);
		result = 31 * result + Arrays.hashCode(shorts);
		result = 31 * result + Arrays.hashCode(ints);
		result = 31 * result + Arrays.hashCode(longs);
		result = 31 * result + Arrays.hashCode(booleans);
		return result;
	}
}
