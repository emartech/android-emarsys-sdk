package com.emarsys.core.database.repository.specification;

import com.emarsys.core.database.repository.AbstractSqlSpecification;
import com.emarsys.core.util.Assert;

public class QueryAll extends AbstractSqlSpecification {

    private final String tableName;

    public QueryAll(String tableName) {
        Assert.notNull(tableName, "TableName must not be null!");
        this.tableName = tableName;
    }

    @Override
    public String getSelection() {
        return String.format("SELECT * FROM %s;", tableName);
    }

    @Override
    public String[] getSelectionArgs() {
        return null;
    }

}
