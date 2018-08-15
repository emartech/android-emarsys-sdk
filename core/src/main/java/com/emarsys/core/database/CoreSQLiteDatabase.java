package com.emarsys.core.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.emarsys.core.database.trigger.TriggerEvent;
import com.emarsys.core.database.trigger.TriggerType;

public interface CoreSQLiteDatabase {

    SQLiteDatabase getBackingDatabase();

    Cursor rawQuery(String sql, String[] selectionArgs);

    void execSQL(String sql);

    long insert(String table, String nullColumnHack, ContentValues values);

    int delete(String table, String whereClause, String[] whereArgs);

    void registerTrigger(
            String table,
            TriggerType triggerType,
            TriggerEvent triggerEvent,
            Runnable trigger);

    void beginTransaction();

    void setTransactionSuccessful();

    void endTransaction();

}