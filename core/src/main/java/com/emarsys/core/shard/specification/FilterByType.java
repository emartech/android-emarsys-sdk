package com.emarsys.core.shard.specification;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.repository.SqlSpecification;


public class FilterByType implements SqlSpecification {

    private final String type;

    public FilterByType(String type) {
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
