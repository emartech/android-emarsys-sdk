package com.emarsys.mobileengage.iam.model.buttonclicked;

import android.content.ContentValues;
import android.database.Cursor;

import com.emarsys.core.database.helper.DbHelper;
import com.emarsys.core.database.repository.AbstractSqliteRepository;

import static com.emarsys.core.database.DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_BUTTON_ID;
import static com.emarsys.core.database.DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_CAMPAIGN_ID;
import static com.emarsys.core.database.DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_TIMESTAMP;
import static com.emarsys.core.database.DatabaseContract.BUTTON_CLICKED_TABLE_NAME;


public class ButtonClickedRepository extends AbstractSqliteRepository<ButtonClicked> {

    public ButtonClickedRepository(DbHelper dbHelper) {
        super(BUTTON_CLICKED_TABLE_NAME, dbHelper);
    }

    @Override
    protected ContentValues contentValuesFromItem(ButtonClicked item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BUTTON_CLICKED_COLUMN_NAME_CAMPAIGN_ID, item.getCampaignId());
        contentValues.put(BUTTON_CLICKED_COLUMN_NAME_BUTTON_ID, item.getButtonId());
        contentValues.put(BUTTON_CLICKED_COLUMN_NAME_TIMESTAMP, item.getTimestamp());
        return contentValues;
    }

    @Override
    protected ButtonClicked itemFromCursor(Cursor cursor) {
        String campaignId = cursor.getString(cursor.getColumnIndex(BUTTON_CLICKED_COLUMN_NAME_CAMPAIGN_ID));
        String buttonId = cursor.getString(cursor.getColumnIndex(BUTTON_CLICKED_COLUMN_NAME_BUTTON_ID));
        long timestamp = cursor.getLong(cursor.getColumnIndex(BUTTON_CLICKED_COLUMN_NAME_TIMESTAMP));
        return new ButtonClicked(campaignId, buttonId, timestamp);
    }

}
