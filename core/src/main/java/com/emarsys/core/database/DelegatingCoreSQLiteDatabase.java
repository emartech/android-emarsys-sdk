package com.emarsys.core.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DelegatingCoreSQLiteDatabase implements CoreSQLiteDatabase {

    private final SQLiteDatabase database;

    public DelegatingCoreSQLiteDatabase(SQLiteDatabase database) {
        this.database = database;
    }

    @Override
    public SQLiteDatabase getBackingDatabase() {
        return database;
    }

    @Override
    public Cursor rawQuery(String query, String[] selectionArgs) {
        return database.rawQuery(query, selectionArgs);
    }

    @Override
    public void execSQL(String sql) {
        database.execSQL(sql);
    }

    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return database.insert(table, nullColumnHack, values);
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        return database.delete(table, whereClause, whereArgs);
    }

    @Override
    public void beginTransaction() {
        database.beginTransaction();
    }

    @Override
    public void setTransactionSuccessful() {
        database.setTransactionSuccessful();
    }

    @Override
    public void endTransaction() {
        database.endTransaction();
    }

}
