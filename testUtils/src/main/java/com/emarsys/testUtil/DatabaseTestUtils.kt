package com.emarsys.testUtil

import android.database.sqlite.SQLiteDatabase

object DatabaseTestUtils {

    @JvmStatic
    fun deleteCoreDatabase(): Boolean {
        return InstrumentationRegistry.getTargetContext().deleteDatabase("EmarsysCore.db")
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