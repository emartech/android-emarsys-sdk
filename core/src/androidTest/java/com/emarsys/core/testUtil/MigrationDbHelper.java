package com.emarsys.core.testUtil;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.UUID;


public class MigrationDbHelper extends SQLiteOpenHelper {

    String name;
    String createSql;

    public MigrationDbHelper(Context context, String name, String createSql) {
        super(context, name, null, 1);
        this.name = name;
        this.createSql = createSql;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void cleanUp() {
        getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + name + " ;", new Object[]{});
    }

    public static String nextTableName() {
        return "Migration_" + UUID.randomUUID().toString().replace("-", "_");
    }
}
