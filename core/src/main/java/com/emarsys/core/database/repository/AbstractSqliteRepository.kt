package com.emarsys.core.database.repository

import android.content.ContentValues
import android.database.Cursor
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.helper.DbHelper

private inline fun CoreSQLiteDatabase.inTransaction(statement: CoreSQLiteDatabase.() -> Unit) {
    try {
        this.beginTransaction()
        statement(this)
        this.setTransactionSuccessful()
    } finally {
        this.endTransaction()
    }
}

abstract class AbstractSqliteRepository<T>(var tableName: String, var dbHelper: DbHelper) : Repository<T, SqlSpecification> {
    abstract fun contentValuesFromItem(item: T): ContentValues
    abstract fun itemFromCursor(cursor: Cursor?): T

    override fun add(item: T) {
        val contentValues = contentValuesFromItem(item)
        val database = dbHelper.writableCoreDatabase
        database.inTransaction {
            insert(tableName, null, contentValues)
        }
    }

    override fun query(specification: SqlSpecification): List<T> {
        val database = dbHelper.readableCoreDatabase
        database.query(specification.isDistinct,
                tableName,
                specification.columns,
                specification.selection,
                specification.selectionArgs,
                specification.groupBy,
                specification.having,
                specification.orderBy,
                specification.limit).use { cursor -> return mapCursorToResultList(cursor) }
    }

    override fun remove(specification: SqlSpecification) {
        val database = dbHelper.writableCoreDatabase
        database.inTransaction {
            delete(
                    tableName,
                    specification.selection,
                    specification.selectionArgs)
        }
    }

    override fun isEmpty(): Boolean {
        val database = dbHelper.readableCoreDatabase
        database.rawQuery("SELECT COUNT(*) FROM $tableName;",
                null).use { cursor ->
            cursor.moveToFirst()
            val count = cursor.getInt(cursor.getColumnIndex("COUNT(*)"))
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