package com.emarsys.core.request.model.specification

import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.repository.AbstractSqlSpecification
import com.emarsys.core.util.DatabaseUtil

class FilterByRequestIds(private val args: Array<String>) : AbstractSqlSpecification() {

    private val sql: String = DatabaseUtil.generateInStatement(DatabaseContract.REQUEST_COLUMN_NAME_REQUEST_ID, args)

    override fun getSelection(): String {
        return sql
    }

    override fun getSelectionArgs(): Array<String> {
        return args
    }
}