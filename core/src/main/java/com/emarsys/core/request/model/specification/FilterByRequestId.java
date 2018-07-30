package com.emarsys.core.request.model.specification;

import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.request.model.CompositeRequestModel;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.DatabaseUtil;

public class FilterByRequestId implements SqlSpecification {

    private final String[] args;
    private final String sql;

    public FilterByRequestId(RequestModel model) {
        this.args = createRequestIds(model);
        this.sql = DatabaseUtil.generateInStatement("request_id", args);
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public String[] getArgs() {
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
