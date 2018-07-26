package com.emarsys.core.shard;

import com.emarsys.core.util.Assert;

import java.io.Serializable;
import java.util.Map;

public class ShardModel {
    String type;
    Map<String, Serializable> data;
    long timestamp;
    long ttl;

    public ShardModel(String type, Map<String, Serializable> data, long timestamp, long ttl) {
        Assert.notNull(type, "Type must not be null!");
        Assert.notNull(data, "Data must not be null!");
        this.type = type;
        this.data = data;
        this.timestamp = timestamp;
        this.ttl = ttl;
    }

    public String getType() {
        return type;
    }

    public Map<String, Serializable> getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getTtl() {
        return ttl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShardModel that = (ShardModel) o;

        if (timestamp != that.timestamp) return false;
        if (ttl != that.ttl) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return data != null ? data.equals(that.data) : that.data == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (ttl ^ (ttl >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ShardModel{" +
                "type='" + type + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                ", ttl=" + ttl +
                '}';
    }
}
