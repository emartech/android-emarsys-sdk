package com.emarsys.core.request.model.specification;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.repository.AbstractSqlSpecification;
import com.emarsys.core.request.model.CompositeRequestModel;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.DatabaseUtil;

public class FilterByRequestId extends AbstractSqlSpecification {

    private final String[] args;
    private final String sql;

    public FilterByRequestId(RequestModel model) {
        this.args = createRequestIds(model);
        this.sql = DatabaseUtil.generateInStatement(DatabaseContract.REQUEST_COLUMN_NAME_REQUEST_ID, args);
    }

    @Override
    public String getSelection() {
        return sql;
    }

    @Override
    public String[] getSelectionArgs() {
        return args;
    }

    private String[] createRequestIds(RequestModel model) {
        String[] args;
        if (model instanceof CompositeRequestModel) {
            CompositeRequestModel compositeRequestModel = (CompositeRequestModel) model;
            args = compositeRequestModel.getOriginalRequestIds();
        } else {
            args = new String[]{model.getId()};
        }
        return args;
    }
}
