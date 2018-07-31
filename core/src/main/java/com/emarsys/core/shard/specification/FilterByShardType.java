package com.emarsys.core.shard.specification;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.util.Assert;


public class FilterByShardType implements SqlSpecification {

    private final String type;

    public FilterByShardType(String type) {
        Assert.notNull(type, "Type must not be null!");
        this.type = type;
    }

    @Override
    public String getSql() {
        return "SELECT * FROM " + DatabaseContract.SHARD_TABLE_NAME + " WHERE " + DatabaseContract.SHARD_COLUMN_TYPE + " LIKE ? ORDER BY ROWID ASC;";
    }

    @Override
    public String[] getArgs() {
        return new String[]{type};
    }

}
