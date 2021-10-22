package com.emarsys.core.database.helper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.emarsys.core.Mockable
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.trigger.TriggerKey

@Mockable
class CoreDbHelper(context: Context, triggerMap: MutableMap<TriggerKey, MutableList<Runnable>>) : AbstractDbHelper(context, DATABASE_NAME, DATABASE_VERSION, triggerMap) {
    companion object {
        const val DATABASE_VERSION = 5
        const val DATABASE_NAME = "EmarsysCore.db"
    }

    override fun onCreate(db: SQLiteDatabase) {
        onUpgrade(db, 0, DATABASE_VERSION)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        for (i in oldVersion until newVersion) {
            for (sqlCommand in DatabaseContract.MIGRATION[i]) {
                db.execSQL(sqlCommand)
            }
        }
    }
}