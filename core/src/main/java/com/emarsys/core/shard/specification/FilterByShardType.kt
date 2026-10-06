package com.emarsys.core.shard.specification

import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.repository.AbstractSqlSpecification

class FilterByShardType(private val type: String) : AbstractSqlSpecification() {

    companion object {
        const val SHARD_TYPE_PREDICT = "predict_%"
        const val SHARD_TYPE_LOG = "log_%"
    }

    override val selection: String
        get() = DatabaseContract.SHARD_COLUMN_TYPE + " LIKE ?"
    override val orderBy: String
        get() = "ROWID ASC"
    override val selectionArgs: Array<String>
        get() = arrayOf(type)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        return type == (other as FilterByShardType).type
    }

    override fun hashCode(): Int = type.hashCode()
}
