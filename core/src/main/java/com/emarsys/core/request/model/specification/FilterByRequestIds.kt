package com.emarsys.core.request.model.specification

import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.repository.AbstractSqlSpecification
import com.emarsys.core.util.DatabaseUtil

class FilterByRequestIds(private val args: Array<String>) : AbstractSqlSpecification() {

    override val selection: String
        get() = DatabaseUtil.generateInStatement(DatabaseContract.REQUEST_COLUMN_NAME_REQUEST_ID, args)

    override val selectionArgs: Array<String>
        get() = args
}