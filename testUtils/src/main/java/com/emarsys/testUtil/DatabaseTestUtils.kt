package com.emarsys.testUtil

import android.database.sqlite.SQLiteDatabase

object DatabaseTestUtils {

    @JvmStatic
    fun deleteCoreDatabase(): Boolean {
        return InstrumentationRegistry.getTargetContext().deleteDatabase("EmarsysCore.db")
    }

    @JvmStatic
    fun deleteCoreDatabaseExceptClientId() {
        val context = InstrumentationRegistry.getTargetContext()
        val dbFile = context.getDatabasePath("EmarsysCore.db")
        if (!dbFile.exists()) return

        SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name != 'hardware_identification' AND name != 'android_metadata';",
                null
            ).use { cursor ->
                val tables = mutableListOf<String>()
                while (cursor.moveToNext()) tables.add(cursor.getString(0))
                tables.forEach { table -> db.execSQL("DELETE FROM $table;") }
            }
        }
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