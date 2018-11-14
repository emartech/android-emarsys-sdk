package com.emarsys.core.shard.specification;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.repository.AbstractSqlSpecification;
import com.emarsys.core.util.Assert;


public class FilterByShardType extends AbstractSqlSpecification {

    private final String type;

    public FilterByShardType(String type) {
        Assert.notNull(type, "Type must not be null!");
        this.type = type;
    }

    @Override
    public String getSelection() {
        return DatabaseContract.SHARD_COLUMN_TYPE + " LIKE ?";
    }

    @Override
    public String getOrderBy() {
        return "ROWID ASC";
    }

    @Override
    public String[] getSelectionArgs() {
        return new String[]{type};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterByShardType that = (FilterByShardType) o;

        return type != null ? type.equals(that.type) : that.type == null;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }
}
