package com.emarsys.core.database.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.emarsys.core.database.CoreSQLiteDatabase;
import com.emarsys.core.database.DelegatingCoreSQLiteDatabase;
import com.emarsys.core.database.trigger.TriggerKey;
import com.emarsys.core.util.Assert;

import java.util.List;
import java.util.Map;

public abstract class AbstractDbHelper extends SQLiteOpenHelper implements DbHelper {

    private final Map<TriggerKey, List<Runnable>> triggerMap;

    public AbstractDbHelper(
            Context context,
            String databaseName,
            int databaseVersion,
            Map<TriggerKey, List<Runnable>> triggerMap) {
        super(context, databaseName, null, databaseVersion);
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(databaseName, "DatabaseName must not be null!");
        Assert.notNull(triggerMap, "TriggerMap must not be null!");
        this.triggerMap = triggerMap;
    }

    public abstract void onCreate(SQLiteDatabase db);

    public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    @Override
    public CoreSQLiteDatabase getReadableCoreDatabase() {
        return new DelegatingCoreSQLiteDatabase(super.getReadableDatabase(), triggerMap);
    }

    @Override
    public CoreSQLiteDatabase getWritableCoreDatabase() {
        return new DelegatingCoreSQLiteDatabase(super.getWritableDatabase(), triggerMap);
    }
}