package com.emarsys.core.database.repository.specification;

import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.util.Assert;

public class QueryAll implements SqlSpecification {

    private final String tableName;

    public QueryAll(String tableName) {
        Assert.notNull(tableName, "TableName must not be null!");
        this.tableName = tableName;
    }

    @Override
    public String getSql() {
        return String.format("SELECT * FROM %s;", tableName);
    }

    @Override
    public String[] getArgs() {
        return null;
    }

}
