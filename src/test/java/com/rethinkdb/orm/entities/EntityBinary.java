package com.rethinkdb.orm.entities;

import com.rethinkdb.orm.annotations.Binary;
import com.rethinkdb.orm.annotations.Id;
import com.rethinkdb.orm.annotations.Indexed;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EntityBinary {

	@Id
	@Binary
	public byte[] id;

	@Binary
	@Indexed
	public byte[] binary;

	@Indexed
	public List<@Binary byte[]> binaryList;

	@Indexed
	public Map<String, @Binary byte[]> binaryMap;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EntityBinary that = (EntityBinary) o;

		if (!Arrays.equals(id, that.id)) {
			return false;
		}
		if (!Arrays.equals(binary, that.binary)) {
			return false;
		}

		if (binaryList != null && that.binaryList != null) {
			for (int i = 0; i < this.binaryList.size(); i++) {
				if (!Arrays.equals(this.binaryList.get(i), that.binaryList.get(i))) {
					return false;
				}
			}
		}

		if (binaryMap != null && that.binaryMap != null) {
			for (String key : binaryMap.keySet()) {
				if (!Arrays.equals(this.binaryMap.get(key), that.binaryMap.get(key))) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(id);
		result = 31 * result + Arrays.hashCode(binary);
		result = 31 * result + (binaryList != null ? binaryList.hashCode() : 0);
		return result;
	}
}
