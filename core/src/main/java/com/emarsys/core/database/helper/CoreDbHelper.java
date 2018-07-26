package com.emarsys.core.database.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.util.log.CoreTopic;
import com.emarsys.core.util.log.EMSLogger;

public class CoreDbHelper extends AbstractDbHelper {
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "EmarsysCoreQueue.db";

    public CoreDbHelper(Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        EMSLogger.log(CoreTopic.OFFLINE, "Creating new database");
        onUpgrade(db, 0, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        EMSLogger.log(CoreTopic.OFFLINE, "Upgrading existing database, old version: %s, new version: %s", oldVersion, newVersion);
        for (int i = oldVersion; i<newVersion; ++i){
            db.execSQL(DatabaseContract.MIGRATION[i]);
        }
    }
}
