package com.emarsys.core.shard.specification;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.repository.AbstractSqlSpecification;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.DatabaseUtil;

import java.util.Arrays;
import java.util.List;

public class FilterByShardIds extends AbstractSqlSpecification {

    private final String[] args;
    private final String sql;

    public FilterByShardIds(List<ShardModel> shardModels) {
        Assert.notNull(shardModels, "ShardModels must not be null!");
        this.args = extractIds(shardModels);
        this.sql = DatabaseUtil.generateInStatement(DatabaseContract.SHARD_COLUMN_ID, args);
    }

    @Override
    public String getSelection() {
        return sql;
    }

    @Override
    public String[] getSelectionArgs() {
        return args;
    }

    private String[] extractIds(List<ShardModel> shardModels) {
        String[] args = new String[shardModels.size()];
        for (int i = 0; i < shardModels.size(); i++) {
            args[i] = shardModels.get(i).getId();
        }
        return args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterByShardIds that = (FilterByShardIds) o;

        if (!Arrays.equals(args, that.args)) return false;
        return sql != null ? sql.equals(that.sql) : that.sql == null;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(args);
        result = 31 * result + (sql != null ? sql.hashCode() : 0);
        return result;
    }
}
