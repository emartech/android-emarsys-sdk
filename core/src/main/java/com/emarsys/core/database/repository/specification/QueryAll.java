package com.emarsys.core.database.repository.specification;

import com.emarsys.core.database.repository.SqlSpecification;

public class QueryAll implements SqlSpecification {

    private final String tableName;

    public QueryAll(String tableName) {
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
