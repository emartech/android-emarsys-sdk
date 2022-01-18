package com.emarsys.mobileengage.iam.model.displayediam;

import android.content.ContentValues;
import android.database.Cursor;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.helper.DbHelper;
import com.emarsys.core.database.repository.AbstractSqliteRepository;
import com.emarsys.core.handler.ConcurrentHandlerHolder;

public class DisplayedIamRepository extends AbstractSqliteRepository<DisplayedIam> {

    public DisplayedIamRepository(DbHelper dbHelper, ConcurrentHandlerHolder concurrentHandlerHolder) {
        super(DatabaseContract.DISPLAYED_IAM_TABLE_NAME, dbHelper, concurrentHandlerHolder);
    }

    @Override
    public ContentValues contentValuesFromItem(DisplayedIam item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.DISPLAYED_IAM_COLUMN_NAME_CAMPAIGN_ID, item.getCampaignId());
        contentValues.put(DatabaseContract.DISPLAYED_IAM_COLUMN_NAME_TIMESTAMP, item.getTimestamp());
        return contentValues;
    }

    @Override
    public DisplayedIam itemFromCursor(Cursor cursor) {
        String campaignId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.DISPLAYED_IAM_COLUMN_NAME_CAMPAIGN_ID));
        long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.DISPLAYED_IAM_COLUMN_NAME_TIMESTAMP));
        return new DisplayedIam(campaignId, timestamp);
    }

}
