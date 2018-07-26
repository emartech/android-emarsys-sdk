package com.emarsys.mobileengage.iam.model.buttonclicked;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.testUtil.DatabaseTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Date;

import static com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedContract.COLUMN_NAME_BUTTON_ID;
import static com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedContract.COLUMN_NAME_CAMPAIGN_ID;
import static com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedContract.COLUMN_NAME_TIMESTAMP;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ButtonClickedRepositoryTest {

    static {
        mock(Cursor.class);
    }

    private ButtonClickedRepository repository;
    private ButtonClicked buttonClicked1;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        DatabaseTestUtils.deleteMobileEngageDatabase();

        Context context = InstrumentationRegistry.getContext();
        repository = new ButtonClickedRepository(context);
        buttonClicked1 = new ButtonClicked("campaign1", "button1", new Date().getTime());
    }

    @Test
    public void testContentValuesFromItem() {
        ContentValues expected = new ContentValues();
        expected.put(COLUMN_NAME_CAMPAIGN_ID, buttonClicked1.getCampaignId());
        expected.put(COLUMN_NAME_BUTTON_ID, buttonClicked1.getButtonId());
        expected.put(COLUMN_NAME_TIMESTAMP, buttonClicked1.getTimestamp());

        ContentValues result = repository.contentValuesFromItem(buttonClicked1);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testItemFromCursor() {
        Cursor cursor = mock(Cursor.class);

        when(cursor.getColumnIndex(COLUMN_NAME_CAMPAIGN_ID)).thenReturn(0);
        when(cursor.getString(0)).thenReturn(buttonClicked1.getCampaignId());
        when(cursor.getColumnIndex(COLUMN_NAME_BUTTON_ID)).thenReturn(1);
        when(cursor.getString(1)).thenReturn(buttonClicked1.getButtonId());
        when(cursor.getColumnIndex(COLUMN_NAME_TIMESTAMP)).thenReturn(2);
        when(cursor.getLong(2)).thenReturn(buttonClicked1.getTimestamp());

        ButtonClicked result = repository.itemFromCursor(cursor);
        ButtonClicked expected = buttonClicked1;

        Assert.assertEquals(expected, result);
    }

}