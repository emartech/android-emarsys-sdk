package com.emarsys.testUtil

import android.database.sqlite.SQLiteDatabase
import android.support.test.InstrumentationRegistry

object DatabaseTestUtils {

    val EMARSYS_CORE_QUEUE_DB = "EmarsysCoreQueue.db"
    val EMARSYS_MOBILE_ENGAGE_DB = "EmarsysMobileEngage.db"

    @JvmStatic
    fun deleteCoreDatabase() {
        InstrumentationRegistry.getContext().deleteDatabase(EMARSYS_CORE_QUEUE_DB)
    }

    @JvmStatic
    fun deleteMobileEngageDatabase() {
        InstrumentationRegistry.getContext().deleteDatabase(EMARSYS_MOBILE_ENGAGE_DB)
    }

    @JvmStatic
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