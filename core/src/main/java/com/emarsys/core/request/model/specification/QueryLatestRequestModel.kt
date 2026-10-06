package com.emarsys.core.request.model.specification

import com.emarsys.core.database.repository.AbstractSqlSpecification

class QueryLatestRequestModel : AbstractSqlSpecification() {
    override val orderBy: String get() = "ROWID ASC"
    override val limit: String get() = "1"
}
