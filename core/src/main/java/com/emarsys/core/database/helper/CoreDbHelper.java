package com.emarsys.core.database.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.trigger.TriggerKey;

import java.util.List;
import java.util.Map;

public class CoreDbHelper extends AbstractDbHelper {
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "EmarsysCore.db";

    public CoreDbHelper(Context context, Map<TriggerKey, List<Runnable>> triggerMap) {
        super(context, DATABASE_NAME, DATABASE_VERSION, triggerMap);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 0, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion; i < newVersion; ++i) {
            for (String sqlCommand : DatabaseContract.MIGRATION[i]) {
                db.execSQL(sqlCommand);
            }
        }
    }
}
