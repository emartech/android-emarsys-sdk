package com.emarsys.core.shard.specification

import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.repository.AbstractSqlSpecification
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.util.DatabaseUtil

class FilterByShardIds(shardModels: List<ShardModel>) : AbstractSqlSpecification() {

    private val args: Array<String> = shardModels.map { it.id }.toTypedArray()
    private val sql: String = DatabaseUtil.generateInStatement(DatabaseContract.SHARD_COLUMN_ID, args)

    override val selection: String get() = sql
    override val selectionArgs: Array<String> get() = args

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as FilterByShardIds
        return args.contentEquals(that.args) && sql == that.sql
    }

    override fun hashCode(): Int {
        var result = args.contentHashCode()
        result = 31 * result + sql.hashCode()
        return result
    }
}
