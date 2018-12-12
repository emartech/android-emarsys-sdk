package com.emarsys.core.util.log;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.util.Assert;

import java.util.Collections;
import java.util.List;

public class LogRepository implements Repository<LogShard, SqlSpecification> {

    private final Repository<ShardModel, SqlSpecification> shardRepository;

    public LogRepository(Repository<ShardModel, SqlSpecification> shardRepository) {
        Assert.notNull(shardRepository, "ShardRepository must not be null!");

        this.shardRepository = shardRepository;
    }

    @Override
    public void add(LogShard item) {
        Assert.notNull(item, "LogShard must not be null!");

        shardRepository.add(item);
    }

    @Override
    public void remove(SqlSpecification specification) {

    }

    @Override
    public List<LogShard> query(SqlSpecification specification) {
        return Collections.emptyList();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
