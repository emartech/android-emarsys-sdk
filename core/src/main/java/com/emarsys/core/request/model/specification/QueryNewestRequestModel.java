package com.emarsys.core.request.model.specification;

import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.database.DatabaseContract;

public class QueryNewestRequestModel implements SqlSpecification {

    public QueryNewestRequestModel() {
    }

    @Override
    public String getSql() {
        return String.format("SELECT * FROM %s ORDER BY ROWID ASC LIMIT 1;", DatabaseContract.REQUEST_TABLE_NAME);
    }

    @Override
    public String[] getArgs() {
        return null;
    }

}
