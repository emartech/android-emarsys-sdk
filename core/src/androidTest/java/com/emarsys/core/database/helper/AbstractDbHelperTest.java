package com.emarsys.core.database.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.database.CoreSQLiteDatabase;
import com.emarsys.core.database.trigger.TriggerKey;
import com.emarsys.core.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class AbstractDbHelperTest {

    private static class DummyDbHelper extends AbstractDbHelper {

        public DummyDbHelper(
                Context context,
                String databaseName,
                int databaseVersion,
                HashMap<TriggerKey, List<Runnable>> triggerMap) {
            super(context, databaseName, databaseVersion, triggerMap);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    private Context context;
    private AbstractDbHelper dbHelper;
    private HashMap<TriggerKey, List<Runnable>> triggerMap;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        context = InstrumentationRegistry.getTargetContext();
        triggerMap = new HashMap<>();
        dbHelper = new DummyDbHelper(
                context,
                "name",
                1,
                triggerMap
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_context_mustNotBeNull() {
        new DummyDbHelper(null, "name", 1, triggerMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_databaseName_mustNotBeNull() {
        new DummyDbHelper(context, null, 1, triggerMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_triggerMap_mustNotBeNull() {
        new DummyDbHelper(context, "name", 1, null);
    }

    @Test
    public void testGetReadableCoreDatabase_returnsWrappedDatabase() {
        CoreSQLiteDatabase db = dbHelper.getReadableCoreDatabase();
        SQLiteDatabase expected = dbHelper.getReadableDatabase();
        SQLiteDatabase result = db.getBackingDatabase();

        assertEquals(expected, result);
    }

    @Test
    public void testGetWritableCoreDatabase_returnsWrappedDatabase() {
        CoreSQLiteDatabase db = dbHelper.getWritableCoreDatabase();
        SQLiteDatabase expected = dbHelper.getWritableDatabase();
        SQLiteDatabase result = db.getBackingDatabase();

        assertEquals(expected, result);
    }

}