package com.emarsys.core.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.emarsys.core.database.trigger.TriggerEvent;
import com.emarsys.core.database.trigger.TriggerKey;
import com.emarsys.core.database.trigger.TriggerType;
import com.emarsys.core.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DelegatingCoreSQLiteDatabase implements CoreSQLiteDatabase {

    private final SQLiteDatabase database;
    private final Map<TriggerKey, List<Runnable>> registeredTriggers;
    private boolean locked;

    public DelegatingCoreSQLiteDatabase(
            SQLiteDatabase database,
            Map<TriggerKey, List<Runnable>> triggerMap) {
        Assert.notNull(database, "Database must not be null!");
        Assert.notNull(triggerMap, "TriggerMap must not be null!");
        this.database = database;
        this.registeredTriggers = triggerMap;
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
    public Cursor query(
            boolean distinct,
            String table,
            String[] columns,
            String selection,
            String[] selectionArgs,
            String groupBy,
            String having,
            String orderBy,
            String limit) {
        return database.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    @Override
    public void execSQL(String sql) {
        database.execSQL(sql);
    }

    @Override
    public void registerTrigger(
            String table,
            TriggerType triggerType,
            TriggerEvent triggerEvent,
            Runnable trigger) {
        Assert.notNull(table, "Table must not be null!");
        Assert.notNull(triggerType, "TriggerType must not be null!");
        Assert.notNull(triggerEvent, "TriggerEvent must not be null!");
        Assert.notNull(trigger, "Trigger must not be null!");

        TriggerKey key = new TriggerKey(table, triggerType, triggerEvent);
        List<Runnable> runnables = registeredTriggers.get(key);

        if (runnables == null) {
            runnables = new ArrayList<>();
            runnables.add(trigger);
        } else {
            runnables.add(trigger);
        }

        registeredTriggers.put(key, runnables);
    }

    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        runTriggers(table, TriggerType.BEFORE, TriggerEvent.INSERT);
        long rowId = database.insert(table, nullColumnHack, values);
        runTriggers(table, TriggerType.AFTER, TriggerEvent.INSERT);
        return rowId;
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        runTriggers(table, TriggerType.BEFORE, TriggerEvent.DELETE);
        int rowsAffected = database.delete(table, whereClause, whereArgs);
        runTriggers(table, TriggerType.AFTER, TriggerEvent.DELETE);
        return rowsAffected;
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

    private void runTriggers(String tableName, TriggerType triggerType, TriggerEvent triggerEvent) {
        if (!locked) {
            locked = true;
            List<Runnable> runnables = registeredTriggers.get(new TriggerKey(tableName, triggerType, triggerEvent));
            if (runnables != null) {
                for (Runnable runnable : runnables) {
                    runnable.run();
                }
            }
            locked = false;
        }
    }
}
