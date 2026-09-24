package com.emarsys.core.request.model.specification;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.repository.AbstractSqlSpecification;
import com.emarsys.core.util.Assert;

public class FilterByUrlPattern extends AbstractSqlSpecification {

    private final String pattern;

    public FilterByUrlPattern(String pattern) {
        Assert.notNull(pattern, "Pattern must not be null!");
        this.pattern = pattern;
    }

    @Override
    public String getSelection() {
        return DatabaseContract.REQUEST_COLUMN_NAME_URL + " LIKE ?";
    }

    @Override
    public String[] getSelectionArgs() {
        return new String[]{pattern};
    }
}
