package com.emarsys.core.database.helper

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.DelegatingCoreSQLiteDatabase
import com.emarsys.core.database.trigger.TriggerKey
import com.emarsys.core.util.Assert

abstract class AbstractDbHelper(
        context: Context,
        databaseName: String,
        databaseVersion: Int,
        private val triggerMap: MutableMap<TriggerKey, MutableList<Runnable>>) : SQLiteOpenHelper(context, databaseName, null, databaseVersion), DbHelper {

    override val readableCoreDatabase: CoreSQLiteDatabase
        get() = DelegatingCoreSQLiteDatabase(super.getReadableDatabase(), triggerMap)
    override val writableCoreDatabase: CoreSQLiteDatabase
        get() = DelegatingCoreSQLiteDatabase(super.getWritableDatabase(), triggerMap)


}