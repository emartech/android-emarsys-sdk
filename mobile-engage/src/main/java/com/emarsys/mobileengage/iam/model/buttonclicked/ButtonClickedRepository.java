package com.emarsys.mobileengage.iam.model.buttonclicked;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.emarsys.core.database.repository.AbstractSqliteRepository;
import com.emarsys.mobileengage.database.MobileEngageDbHelper;

import static com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedContract.COLUMN_NAME_BUTTON_ID;
import static com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedContract.COLUMN_NAME_CAMPAIGN_ID;
import static com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedContract.COLUMN_NAME_TIMESTAMP;
import static com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedContract.TABLE_NAME;

public class ButtonClickedRepository extends AbstractSqliteRepository<ButtonClicked> {

    public ButtonClickedRepository(Context context) {
        super(TABLE_NAME, new MobileEngageDbHelper(context));
    }

    @Override
    protected ContentValues contentValuesFromItem(ButtonClicked item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME_CAMPAIGN_ID, item.getCampaignId());
        contentValues.put(COLUMN_NAME_BUTTON_ID, item.getButtonId());
        contentValues.put(COLUMN_NAME_TIMESTAMP, item.getTimestamp());
        return contentValues;
    }

    @Override
    protected ButtonClicked itemFromCursor(Cursor cursor) {
        String campaignId = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CAMPAIGN_ID));
        String buttonId = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_BUTTON_ID));
        long timestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_TIMESTAMP));
        return new ButtonClicked(campaignId, buttonId, timestamp);
    }

}
