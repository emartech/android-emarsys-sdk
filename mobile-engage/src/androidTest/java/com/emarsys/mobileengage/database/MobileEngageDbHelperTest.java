package com.emarsys.mobileengage.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.testUtil.DatabaseTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.Assert.assertEquals;

public class MobileEngageDbHelperTest {

    public static final String TABLE_EXISTS = "SELECT * FROM sqlite_master WHERE type='table' AND name='%s';";
    public static final String DISPLAYED_IAM_EXISTS = String.format(TABLE_EXISTS, "displayed_iam");
    public static final String BUTTON_CLICKED_EXISTS = String.format(TABLE_EXISTS, "button_clicked");
    public static final String COLUMN_NAME_SQL = "sql";

    private MobileEngageDbHelper dbHelper;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        DatabaseTestUtils.deleteMobileEngageDatabase();

        Context context = InstrumentationRegistry.getContext();
        dbHelper = new MobileEngageDbHelper(context);
    }

    @Test
    public void onCreate_createsDisplayedIamTable() throws Exception {
        SQLiteDatabase db = dbHelper.getReadableCoreDatabase().getBackingDatabase();

        Cursor cursor = db.rawQuery(DISPLAYED_IAM_EXISTS, null);

        assertEquals(1, cursor.getCount());

        cursor.moveToFirst();
        String actual = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SQL));

        String expected = "CREATE TABLE displayed_iam (campaign_id TEXT,timestamp INTEGER)";
        assertEquals(expected, actual);

        cursor.close();
    }

    @Test
    public void onCreate_createsButtonClickedTable() throws Exception {
        SQLiteDatabase db = dbHelper.getReadableCoreDatabase().getBackingDatabase();

        Cursor cursor = db.rawQuery(BUTTON_CLICKED_EXISTS, null);

        assertEquals(1, cursor.getCount());

        cursor.moveToFirst();
        String actual = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SQL));


        String expected = "CREATE TABLE button_clicked (campaign_id TEXT,button_id TEXT,timestamp INTEGER)";
        assertEquals(expected, actual);

        cursor.close();
    }

}