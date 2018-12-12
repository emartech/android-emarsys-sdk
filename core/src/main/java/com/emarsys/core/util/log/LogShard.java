package com.emarsys.core.util.log;

import com.emarsys.core.shard.ShardModel;

import java.util.Map;

public abstract class LogShard extends ShardModel {

    public static final String TYPE = "log";

    public LogShard(String id, Map<String, Object> data, long timestamp, long ttl) {
        super(id, TYPE, data, timestamp, ttl);
    }

}
