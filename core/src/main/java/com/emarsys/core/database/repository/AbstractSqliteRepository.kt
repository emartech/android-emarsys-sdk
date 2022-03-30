package com.emarsys.core.database.repository

import android.content.ContentValues
import android.database.Cursor
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.helper.DbHelper
import com.emarsys.core.handler.ConcurrentHandlerHolder


private inline fun <T> CoreSQLiteDatabase.inTransaction(statement: CoreSQLiteDatabase.() -> T): T {
    this.beginTransaction()
    val result: T
    try {
        result = statement(this)
        this.setTransactionSuccessful()
    } finally {
        this.endTransaction()
    }
    return result
}

abstract class AbstractSqliteRepository<T>(
    var tableName: String,
    var dbHelper: DbHelper,
    var concurrentHandlerHolder: ConcurrentHandlerHolder
) : Repository<T, SqlSpecification> {

    abstract fun contentValuesFromItem(item: T): ContentValues
    abstract fun itemFromCursor(cursor: Cursor): T

    override fun add(item: T) {
        val contentValues = contentValuesFromItem(item)
        val database = dbHelper.writableCoreDatabase
        database.inTransaction {
            insert(tableName, null, contentValues)
        }
    }

    override fun update(item: T, specification: SqlSpecification): Int {
        val values = contentValuesFromItem(item)
        val database = dbHelper.writableCoreDatabase
        var result = 0

        database.inTransaction {
            result = update(
                tableName,
                values,
                specification.selection,
                specification.selectionArgs
            )
        }

        return result
    }

    override fun query(specification: SqlSpecification): List<T> {
        val database = dbHelper.readableCoreDatabase

        database.query(
            specification.isDistinct,
            tableName,
            specification.columns,
            specification.selection,
            specification.selectionArgs,
            specification.groupBy,
            specification.having,
            specification.orderBy,
            specification.limit
        ).use { cursor -> return mapCursorToResultList(cursor) }


    }

    override fun remove(specification: SqlSpecification) {
        val database = dbHelper.writableCoreDatabase

        return database.inTransaction {
            delete(
                tableName,
                specification.selection,
                specification.selectionArgs
            )
        }
    }

    override fun isEmpty(): Boolean {
        val database = dbHelper.readableCoreDatabase
        database.rawQuery(
            "SELECT COUNT(*) FROM $tableName;",
            null
        ).use { cursor ->
            cursor.moveToFirst()
            val count = cursor.getInt(cursor.getColumnIndexOrThrow("COUNT(*)"))
            return count == 0
        }
    }

    private fun mapCursorToResultList(cursor: Cursor): List<T> {
        val result: MutableList<T> = mutableListOf()
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val item = itemFromCursor(cursor)
                if (item != null) {
                    result.add(item)
                }
                cursor.moveToNext()
            }
        }
        return result
    }
}