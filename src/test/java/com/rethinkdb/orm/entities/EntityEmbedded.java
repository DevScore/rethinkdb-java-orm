package com.rethinkdb.orm.entities;

@SuppressWarnings("SameParameterValue")
public class EntityEmbedded {

	public EntityEmbedded() {
	}

	public EntityEmbedded(int first, String second, EntityEmbedded embedded) {
		this.first = first;
		this.second = second;
		this.embedded = embedded;
	}

	public Integer first;
	public String second;
	public EntityEmbedded embedded;
	public EntityEmbedded[] embeddedArray;

	//	 This getter causes exception on RDB.create()
	public Integer getFirst() {
		return first;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EntityEmbedded that = (EntityEmbedded) o;
		if (first != null ? !first.equals(that.first) : that.first != null) return false;
		return second != null ? second.equals(that.second) : that.second == null;

	}

	@Override
	public int hashCode() {
		int result = first != null ? first.hashCode() : 0;
		result = 31 * result + (second != null ? second.hashCode() : 0);
		return result;
	}
}
