package com.emarsys.core.shard.specification;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.DatabaseUtil;

import java.util.List;

public class FilterByShardIds implements SqlSpecification {

    private final String[] args;
    private final String sql;

    public FilterByShardIds(List<ShardModel> shardModels) {
        Assert.notNull(shardModels, "ShardModels must not be null!");
        this.args = extractIds(shardModels);
        this.sql = DatabaseUtil.generateInStatement(DatabaseContract.SHARD_COLUMN_ID, args);
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public String[] getArgs() {
        return args;
    }

    private String[] extractIds(List<ShardModel> shardModels) {
        String[] args = new String[shardModels.size()];
        for (int i = 0; i < shardModels.size(); i++) {
            args[i] = shardModels.get(i).getId();
        }
        return args;
    }

}
