package com.rethinkdb.orm.entities;

import com.rethinkdb.orm.annotations.*;

import java.util.*;

public class EntityOne {

	@Id
	public String userId;

	@TableName
	public String tableName;

	@Indexed
	public int one;

	@Indexed
	public String two;

	@FieldName("third")  // explicitly set the name of the bin	public double three;
	public double three;
	public float four;
	private short five;
	private byte six;

	@Indexed
	public boolean seven;

	@IgnoreNull
	public Date eight;

	@Indexed
	public List<String> nine;
	public List<Float> floatList;
	public Float[] floatArray;
	public float[] floatPrimitiveArray;
	public List<EntityEmbedded> embeddedList;
	public List<EntityEnum> enumList;
	public Set<EntityEmbedded> embeddedSet;

	public Map<String, EntityEmbedded> mapObject;
	public Map<String, Float> mapFloat;
	public Map<String, @Binary byte[]> mapBinary;

	public EntityEnum eleven;
	public Set<String> twelve;
	public byte[] thirteen;

	public EntityEmbedded embedded;

	@AnyProperty
	public final Map<String, Object> unmapped = new HashMap<>();

	// un-mappable class must be ignored
	@Ignore
	public Calendar calendar;

	@Ignore
	public String ignored;

	public short getFive() {
		return five;
	}

	public void setFive(short five) {
		this.five = five;
	}

	public byte getSix() {
		return six;
	}

	public void setSix(byte six) {
		this.six = six;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EntityOne entityOne = (EntityOne) o;

		if (one != entityOne.one) return false;
		if (Double.compare(entityOne.three, three) != 0) return false;
		if (Float.compare(entityOne.four, four) != 0) return false;
		if (five != entityOne.five) return false;
		if (six != entityOne.six) return false;
		if (seven != entityOne.seven) return false;
		if (userId != null ? !userId.equals(entityOne.userId) : entityOne.userId != null) return false;
		if (tableName != null ? !tableName.equals(entityOne.tableName) : entityOne.tableName != null) return false;
		if (two != null ? !two.equals(entityOne.two) : entityOne.two != null) return false;
		if (eight != null ? !eight.equals(entityOne.eight) : entityOne.eight != null) return false;
		if (nine != null ? !nine.equals(entityOne.nine) : entityOne.nine != null) return false;
		if (floatList != null ? !floatList.equals(entityOne.floatList) : entityOne.floatList != null) return false;
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(floatArray, entityOne.floatArray)) return false;
		if (!Arrays.equals(floatPrimitiveArray, entityOne.floatPrimitiveArray)) return false;
		if (embeddedList != null ? !embeddedList.equals(entityOne.embeddedList) : entityOne.embeddedList != null)
			return false;
		if (enumList != null ? !enumList.equals(entityOne.enumList) : entityOne.enumList != null) return false;
		if (embeddedSet != null ? !embeddedSet.equals(entityOne.embeddedSet) : entityOne.embeddedSet != null) return false;
		if (mapObject != null ? !mapObject.equals(entityOne.mapObject) : entityOne.mapObject != null) return false;
		if (mapFloat != null ? !mapFloat.equals(entityOne.mapFloat) : entityOne.mapFloat != null) return false;
		if (eleven != entityOne.eleven) return false;
		if (twelve != null ? !twelve.equals(entityOne.twelve) : entityOne.twelve != null) return false;
		if (!Arrays.equals(thirteen, entityOne.thirteen)) return false;
		if (embedded != null ? !embedded.equals(entityOne.embedded) : entityOne.embedded != null) return false;
		if (unmapped != null ? !unmapped.equals(entityOne.unmapped) : entityOne.unmapped != null) return false;
		if (calendar != null ? !calendar.equals(entityOne.calendar) : entityOne.calendar != null) return false;
		return ignored != null ? ignored.equals(entityOne.ignored) : entityOne.ignored == null;

	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = userId != null ? userId.hashCode() : 0;
		result = 31 * result + (tableName != null ? tableName.hashCode() : 0);
		result = 31 * result + one;
		result = 31 * result + (two != null ? two.hashCode() : 0);
		temp = Double.doubleToLongBits(three);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + (four != +0.0f ? Float.floatToIntBits(four) : 0);
		result = 31 * result + (int) five;
		result = 31 * result + (int) six;
		result = 31 * result + (seven ? 1 : 0);
		result = 31 * result + (eight != null ? eight.hashCode() : 0);
		result = 31 * result + (nine != null ? nine.hashCode() : 0);
		result = 31 * result + (floatList != null ? floatList.hashCode() : 0);
		result = 31 * result + Arrays.hashCode(floatArray);
		result = 31 * result + Arrays.hashCode(floatPrimitiveArray);
		result = 31 * result + (embeddedList != null ? embeddedList.hashCode() : 0);
		result = 31 * result + (enumList != null ? enumList.hashCode() : 0);
		result = 31 * result + (embeddedSet != null ? embeddedSet.hashCode() : 0);
		result = 31 * result + (mapObject != null ? mapObject.hashCode() : 0);
		result = 31 * result + (mapFloat != null ? mapFloat.hashCode() : 0);
		result = 31 * result + (eleven != null ? eleven.hashCode() : 0);
		result = 31 * result + (twelve != null ? twelve.hashCode() : 0);
		result = 31 * result + Arrays.hashCode(thirteen);
		result = 31 * result + (embedded != null ? embedded.hashCode() : 0);
		result = 31 * result + (unmapped != null ? unmapped.hashCode() : 0);
		result = 31 * result + (calendar != null ? calendar.hashCode() : 0);
		result = 31 * result + (ignored != null ? ignored.hashCode() : 0);
		return result;
	}
}
