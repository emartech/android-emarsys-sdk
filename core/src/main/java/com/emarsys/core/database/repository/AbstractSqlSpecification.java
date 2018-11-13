package com.emarsys.core.database.repository;

public abstract class AbstractSqlSpecification implements SqlSpecification {
    @Override
    public boolean isDistinct() {
        return false;
    }

    @Override
    public String[] getColumns() {
        return null;
    }

    @Override
    public String getSelection() {
        return null;
    }

    @Override
    public String[] getSelectionArgs() {
        return null;
    }

    @Override
    public String getGroupBy() {
        return null;
    }

    @Override
    public String getHaving() {
        return null;
    }

    @Override
    public String getOrderBy() {
        return null;
    }

    @Override
    public String getLimit() {
        return null;
    }
}
