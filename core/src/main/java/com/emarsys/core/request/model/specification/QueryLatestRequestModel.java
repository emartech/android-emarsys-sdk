package com.emarsys.core.request.model.specification;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.repository.AbstractSqlSpecification;

public class QueryLatestRequestModel extends AbstractSqlSpecification {

    public QueryLatestRequestModel() {
    }

    @Override
    public String getSelection() {
        return String.format("SELECT * FROM %s ORDER BY ROWID ASC LIMIT 1;", DatabaseContract.REQUEST_TABLE_NAME);
    }

    @Override
    public String[] getSelectionArgs() {
        return null;
    }

}
