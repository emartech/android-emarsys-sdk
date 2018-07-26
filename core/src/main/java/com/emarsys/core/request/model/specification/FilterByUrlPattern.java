package com.emarsys.core.request.model.specification;

import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.database.DatabaseContract;

public class FilterByUrlPattern implements SqlSpecification {

    private final String pattern;

    public FilterByUrlPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getSql() {
        return "SELECT * FROM " + DatabaseContract.REQUEST_TABLE_NAME + " WHERE " + DatabaseContract.COLUMN_NAME_URL + " LIKE ?;";
    }

    @Override
    public String[] getArgs() {
        return new String[]{pattern};
    }
}
