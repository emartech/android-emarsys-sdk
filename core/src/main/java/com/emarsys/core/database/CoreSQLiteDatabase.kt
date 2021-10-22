package com.emarsys.core.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerType

interface CoreSQLiteDatabase {
    val backingDatabase: SQLiteDatabase?
    fun rawQuery(sql: String, selectionArgs: Array<String>?): Cursor
    fun query(
            distinct: Boolean,
            table: String,
            columns: Array<String>?,
            selection: String?,
            selectionArgs: Array<String>?,
            groupBy: String?,
            having: String?,
            orderBy: String?,
            limit: String?): Cursor

    fun execSQL(sql: String)
    fun insert(table: String, nullColumnHack: String?, values: ContentValues): Long
    fun update(table: String, values: ContentValues, whereClause: String?, whereArgs: Array<String>?): Int
    fun delete(table: String, whereClause: String?, whereArgs: Array<String>?): Int
    fun registerTrigger(
            table: String,
            triggerType: TriggerType,
            triggerEvent: TriggerEvent,
            trigger: Runnable)

    fun beginTransaction()
    fun setTransactionSuccessful()
    fun endTransaction()
}