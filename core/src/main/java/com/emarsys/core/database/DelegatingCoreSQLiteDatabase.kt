package com.emarsys.core.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.emarsys.core.Mockable
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerKey
import com.emarsys.core.database.trigger.TriggerType

@Mockable
class DelegatingCoreSQLiteDatabase(
        override val backingDatabase: SQLiteDatabase,
        private val registeredTriggers: MutableMap<TriggerKey, MutableList<Runnable>>) : CoreSQLiteDatabase {
    private var locked = false

    override fun rawQuery(sql: String, selectionArgs: Array<String>?): Cursor {
        return backingDatabase.rawQuery(sql, selectionArgs)
    }

    override fun query(
            distinct: Boolean,
            table: String,
            columns: Array<String>?,
            selection: String?,
            selectionArgs: Array<String>?,
            groupBy: String?,
            having: String?,
            orderBy: String?,
            limit: String?): Cursor {
        return backingDatabase.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
    }

    override fun execSQL(sql: String) {
        backingDatabase.execSQL(sql)
    }

    override fun registerTrigger(
            table: String,
            triggerType: TriggerType,
            triggerEvent: TriggerEvent,
            trigger: Runnable) {

        val key = TriggerKey(table, triggerType, triggerEvent)
        val runnables = registeredTriggers[key] ?: mutableListOf()
        runnables.add(trigger)
        registeredTriggers[key] = runnables
    }

    override fun insert(table: String, nullColumnHack: String?, values: ContentValues): Long {
        runTriggers(table, TriggerType.BEFORE, TriggerEvent.INSERT)
        val rowId = backingDatabase.insert(table, nullColumnHack, values)
        runTriggers(table, TriggerType.AFTER, TriggerEvent.INSERT)
        return rowId
    }

    override fun update(table: String, values: ContentValues, whereClause: String?, whereArgs: Array<String>?): Int {
        runTriggers(table, TriggerType.BEFORE, TriggerEvent.UPDATE)
        val affectedRows = backingDatabase.update(table, values, whereClause, whereArgs)
        runTriggers(table, TriggerType.AFTER, TriggerEvent.UPDATE)
        return affectedRows
    }

    override fun delete(table: String, whereClause: String?, whereArgs: Array<String>?): Int {
        runTriggers(table, TriggerType.BEFORE, TriggerEvent.DELETE)
        val rowsAffected = backingDatabase.delete(table, whereClause, whereArgs)
        runTriggers(table, TriggerType.AFTER, TriggerEvent.DELETE)
        return rowsAffected
    }

    override fun beginTransaction() {
        backingDatabase.beginTransaction()
    }

    override fun setTransactionSuccessful() {
        backingDatabase.setTransactionSuccessful()
    }

    override fun endTransaction() {
        backingDatabase.endTransaction()
    }

    private fun runTriggers(tableName: String, triggerType: TriggerType, triggerEvent: TriggerEvent) {
        if (!locked) {
            locked = true
            registeredTriggers[TriggerKey(tableName, triggerType, triggerEvent)]?.forEach {
                it.run()
            }
            locked = false
        }
    }
}