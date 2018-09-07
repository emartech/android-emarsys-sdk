package com.emarsys.core.shard;

import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.util.Assert;

import java.util.HashMap;
import java.util.Map;

public class ShardModel {
    final private String id;
    final private String type;
    final private Map<String, Object> data;
    final private long timestamp;
    final private long ttl;

    public ShardModel(String id, String type, Map<String, Object> data, long timestamp, long ttl) {
        Assert.notNull(type, "Type must not be null!");
        Assert.notNull(data, "Data must not be null!");
        Assert.notNull(id, "ID must not be null!");
        this.id = id;
        this.type = type;
        this.data = data;
        this.timestamp = timestamp;
        this.ttl = ttl;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getData() {
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
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return data != null ? data.equals(that.data) : that.data == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (ttl ^ (ttl >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ShardModel{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                ", ttl=" + ttl +
                '}';
    }

    public static class Builder {
        private String id;
        private String type;
        private Map<String, Object> payload;
        private long timestamp;
        private long ttl;

        public Builder(TimestampProvider timestampProvider, UUIDProvider uuidProvider) {
            Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
            Assert.notNull(uuidProvider, "UuidProvider must not be null!");
            timestamp = timestampProvider.provideTimestamp();
            ttl = Long.MAX_VALUE;
            id = uuidProvider.provideId();
            payload = new HashMap<>();
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder ttl(Long ttl) {
            this.ttl = ttl;
            return this;
        }

        public Builder payloadEntry(String key, Object value) {
            this.payload.put(key, value);
            return this;
        }

        public ShardModel build() {
            return new ShardModel(id, type, payload, timestamp, ttl);
        }
    }

}
