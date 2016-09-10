package com.rethinkdb.orm.entities;

import com.rethinkdb.orm.annotations.Id;
import com.rethinkdb.orm.annotations.Timestamp;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class EntityOffsetDate {
	@Id
	public String id;
	public OffsetDateTime now;
	public OffsetDateTime past;
	public OffsetDateTime epoch;
	public OffsetDateTime iso;

	@Timestamp
	public long timestamp;

	public Map<String, @Timestamp Long> timestamps;
	public List<@Timestamp Long> timestampList;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EntityOffsetDate that = (EntityOffsetDate) o;

		if (timestamp != that.timestamp) return false;
		if (id != null ? !id.equals(that.id) : that.id != null) return false;
		if (now != null ? !now.equals(that.now) : that.now != null) return false;
		if (past != null ? !past.equals(that.past) : that.past != null) return false;
		if (epoch != null ? !epoch.equals(that.epoch) : that.epoch != null) return false;
		if (iso != null ? !iso.equals(that.iso) : that.iso != null) return false;
		return timestamps != null ? timestamps.equals(that.timestamps) : that.timestamps == null;

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (now != null ? now.hashCode() : 0);
		result = 31 * result + (past != null ? past.hashCode() : 0);
		result = 31 * result + (epoch != null ? epoch.hashCode() : 0);
		result = 31 * result + (iso != null ? iso.hashCode() : 0);
		result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
		result = 31 * result + (timestamps != null ? timestamps.hashCode() : 0);
		return result;
	}
}
