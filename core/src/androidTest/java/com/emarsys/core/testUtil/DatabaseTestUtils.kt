package com.emarsys.core.testUtil

import android.database.sqlite.SQLiteDatabase
import android.support.test.InstrumentationRegistry

import com.emarsys.core.database.helper.CoreDbHelper

object DatabaseTestUtils {

    val EMARSYS_CORE_QUEUE_DB = CoreDbHelper.DATABASE_NAME

    fun deleteCoreDatabase() {
        InstrumentationRegistry.getContext().deleteDatabase(EMARSYS_CORE_QUEUE_DB)
    }

    fun dropAllTables(db: SQLiteDatabase) {
        db.rawQuery("SELECT 'DROP TABLE ' || name || ';' FROM sqlite_master WHERE type='table';", null).use {
            it.moveToFirst()
            while (!it.isAfterLast) {
                db.execSQL(it.getString(0))
                it.moveToNext()
            }
        }
    }
}