package com.emarsys.core.request.model.specification

import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.repository.AbstractSqlSpecification

class FilterByUrlPattern(private val pattern: String) : AbstractSqlSpecification() {
    override val selection: String
        get() = DatabaseContract.REQUEST_COLUMN_NAME_URL + " LIKE ?"
    override val selectionArgs: Array<String>
        get() = arrayOf(pattern)
}
