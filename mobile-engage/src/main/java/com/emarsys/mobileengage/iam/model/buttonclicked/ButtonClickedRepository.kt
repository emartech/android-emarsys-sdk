package com.emarsys.mobileengage.iam.model.buttonclicked

import android.content.ContentValues
import android.database.Cursor
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.helper.DbHelper
import com.emarsys.core.database.repository.AbstractSqliteRepository

class ButtonClickedRepository(dbHelper: DbHelper) : AbstractSqliteRepository<ButtonClicked>(DatabaseContract.BUTTON_CLICKED_TABLE_NAME, dbHelper) {
    override fun contentValuesFromItem(item: ButtonClicked): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_CAMPAIGN_ID, item.campaignId)
        contentValues.put(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_BUTTON_ID, item.buttonId)
        contentValues.put(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_TIMESTAMP, item.timestamp)
        return contentValues
    }

    override fun itemFromCursor(cursor: Cursor): ButtonClicked {
        val campaignId = cursor.getString(cursor.getColumnIndex(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_CAMPAIGN_ID))
        val buttonId = cursor.getString(cursor.getColumnIndex(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_BUTTON_ID))
        val timestamp = cursor.getLong(cursor.getColumnIndex(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_TIMESTAMP))
        return ButtonClicked(campaignId, buttonId, timestamp)
    }
}