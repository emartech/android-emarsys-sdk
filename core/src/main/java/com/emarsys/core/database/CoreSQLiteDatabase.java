package com.emarsys.core.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public interface CoreSQLiteDatabase {

    SQLiteDatabase getBackingDatabase();

    Cursor rawQuery(String sql, String[] selectionArgs);

    void execSQL(String sql);

    long insert(String table, String nullColumnHack, ContentValues values);

    int delete(String table, String whereClause, String[] whereArgs);

    void beginTransaction();

    void setTransactionSuccessful();

    void endTransaction();

}