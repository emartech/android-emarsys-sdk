package com.emarsys.core.database.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.testUtil.MigrationDbHelper;
import com.emarsys.core.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class SqliteMigrationTest {

    private static final String COLUMN_NAME_REQUEST_ID = "request_id";
    private static final String COLUMN_NAME_METHOD = "method";
    private static final String COLUMN_NAME_URL = "url";
    private static final String COLUMN_NAME_HEADERS = "headers";
    private static final String COLUMN_NAME_PAYLOAD = "payload";
    private static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    private static final String COLUMN_NAME_TTL = "ttl";

    private MigrationDbHelper helper;
    private Context context;
    private String TABLE_NAME;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        context = InstrumentationRegistry.getContext();
        TABLE_NAME = MigrationDbHelper.nextTableName();

    }

    @After
    public void tearDown() {
        helper.cleanUp();
    }

    @Test
    public void testMigration_2() {
        helper = helperWith_tableVersion_1();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME_REQUEST_ID, "id1");
        contentValues.put(COLUMN_NAME_METHOD, "get");
        contentValues.put(COLUMN_NAME_URL, "www.url.com");
        contentValues.put(COLUMN_NAME_HEADERS, new byte[]{1, 2, 3, 4, 5});
        contentValues.put(COLUMN_NAME_PAYLOAD, new byte[]{5, 4, 3, 2, 1});
        contentValues.put(COLUMN_NAME_TIMESTAMP, 10L);

        SQLiteDatabase db = helper.getWritableDatabase();
        db.insert(TABLE_NAME, null, contentValues);

        db.execSQL(replaceTableName(DatabaseContract.UPGRADE_TO_2));

        Cursor cursor = db.rawQuery(String.format("SELECT * FROM %s;", TABLE_NAME), null);
        cursor.moveToFirst();
        long ttl = cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_TTL));
        cursor.close();
        Assert.assertEquals(Long.MAX_VALUE, ttl);
    }

    private MigrationDbHelper helperWith_tableVersion_1() {
        String SQL_CREATE_TABLE = String.format(
                "CREATE TABLE IF NOT EXISTS %s (" +
                        "%s TEXT," +
                        "%s TEXT," +
                        "%s TEXT," +
                        "%s BLOB," +
                        "%s BLOB," +
                        "%s INTEGER" +
                        ");",
                TABLE_NAME,
                COLUMN_NAME_REQUEST_ID,
                COLUMN_NAME_METHOD,
                COLUMN_NAME_URL,
                COLUMN_NAME_HEADERS,
                COLUMN_NAME_PAYLOAD,
                COLUMN_NAME_TIMESTAMP
        );
        return new MigrationDbHelper(context, TABLE_NAME, SQL_CREATE_TABLE);
    }

    private String replaceTableName(String sql) {
        return sql.replaceAll("^ALTER\\s+TABLE\\s+[\\w]+\\s+", "ALTER TABLE "+TABLE_NAME+" ");
    }
}
