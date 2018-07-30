package com.emarsys.core.request.model.specification;

import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.util.Assert;

public class FilterByUrlPattern implements SqlSpecification {

    private final String pattern;

    public FilterByUrlPattern(String pattern) {
        Assert.notNull(pattern, "Pattern must not be null!");
        this.pattern = pattern;
    }

    @Override
    public String getSql() {
        return "SELECT * FROM " + DatabaseContract.REQUEST_TABLE_NAME + " WHERE " + DatabaseContract.REQUEST_COLUMN_NAME_URL + " LIKE ?;";
    }

    @Override
    public String[] getArgs() {
        return new String[]{pattern};
    }
}
