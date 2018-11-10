package com.emarsys.core.request.model.specification;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.repository.SqlSpecification;

public class QueryLatestRequestModel implements SqlSpecification {

    public QueryLatestRequestModel() {
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
