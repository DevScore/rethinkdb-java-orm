package com.rethinkdb.orm.entities;

import com.rethinkdb.orm.annotations.Id;
import com.rethinkdb.orm.annotations.Indexed;
import com.rethinkdb.orm.types.Hash;

import java.util.List;

public class EntityHash {

	@Id
	public Hash id;

	@Indexed
	public Hash hash;

	@Indexed
	public List<Hash> hashList;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EntityHash that = (EntityHash) o;

		if (id != null ? !id.equals(that.id) : that.id != null) return false;
		if (hash != null ? !hash.equals(that.hash) : that.hash != null) return false;
		return hashList != null ? hashList.equals(that.hashList) : that.hashList == null;

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (hash != null ? hash.hashCode() : 0);
		result = 31 * result + (hashList != null ? hashList.hashCode() : 0);
		return result;
	}
}
