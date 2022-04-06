package com.emarsys.mobileengage.iam.model.buttonclicked

import android.content.ContentValues
import android.database.Cursor
import com.emarsys.core.Mockable
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.helper.DbHelper
import com.emarsys.core.database.repository.AbstractSqliteRepository
import com.emarsys.core.handler.ConcurrentHandlerHolder

@Mockable
class ButtonClickedRepository(
    dbHelper: DbHelper,
    concurrentHandlerHolder: ConcurrentHandlerHolder
) : AbstractSqliteRepository<ButtonClicked>(
    DatabaseContract.BUTTON_CLICKED_TABLE_NAME,
    dbHelper,
    concurrentHandlerHolder
) {
    override fun contentValuesFromItem(item: ButtonClicked): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_CAMPAIGN_ID, item.campaignId)
        contentValues.put(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_BUTTON_ID, item.buttonId)
        contentValues.put(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_TIMESTAMP, item.timestamp)
        return contentValues
    }

    override fun itemFromCursor(cursor: Cursor): ButtonClicked {
        val campaignId =
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_CAMPAIGN_ID))
        val buttonId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_BUTTON_ID))
        val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_TIMESTAMP))
        return ButtonClicked(campaignId, buttonId, timestamp)
    }
}