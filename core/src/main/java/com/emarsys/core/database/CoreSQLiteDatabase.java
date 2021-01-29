package com.emarsys.core.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.emarsys.core.database.trigger.TriggerEvent;
import com.emarsys.core.database.trigger.TriggerType;

public interface CoreSQLiteDatabase {

    SQLiteDatabase getBackingDatabase();

    Cursor rawQuery(String sql, String[] selectionArgs);

    Cursor query(
            boolean distinct,
            String table,
            String[] columns,
            String selection,
            String[] selectionArgs,
            String groupBy,
            String having,
            String orderBy,
            String limit);

    void execSQL(String sql);

    long insert(String table, String nullColumnHack, ContentValues values);

    int update(String table, ContentValues values, String whereClause, String[] whereArgs);

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