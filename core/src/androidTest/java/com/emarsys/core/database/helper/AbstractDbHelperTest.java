package com.emarsys.core.database.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.database.CoreSQLiteDatabase;
import com.emarsys.core.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.Assert.assertEquals;

public class AbstractDbHelperTest {

    private static class DummyDbHelper extends AbstractDbHelper {

        public DummyDbHelper(Context context, String databaseName, int databaseVersion) {
            super(context, databaseName, databaseVersion);
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

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        context = InstrumentationRegistry.getTargetContext();
        dbHelper = new DummyDbHelper(
                context,
                "name",
                1
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_contextMustNotBeNull() {
        new DummyDbHelper(null, "name", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_databaseNameMustNotBeNull() {
        new DummyDbHelper(context, null, 1);
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