package com.emarsys.core.request.model.specification;

import com.emarsys.core.database.repository.AbstractSqlSpecification;

public class QueryLatestRequestModel extends AbstractSqlSpecification {

    public QueryLatestRequestModel() {
    }

    @Override
    public String getOrderBy() {
        return "ROWID ASC";
    }

    @Override
    public String getLimit() {
        return "1";
    }
}
