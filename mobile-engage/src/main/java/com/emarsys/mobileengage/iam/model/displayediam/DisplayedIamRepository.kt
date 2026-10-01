package com.emarsys.mobileengage.iam.model.displayediam

import android.content.ContentValues
import android.database.Cursor
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.helper.DbHelper
import com.emarsys.core.database.repository.AbstractSqliteRepository
import com.emarsys.core.handler.ConcurrentHandlerHolder

class DisplayedIamRepository(
    dbHelper: DbHelper,
    concurrentHandlerHolder: ConcurrentHandlerHolder
) : AbstractSqliteRepository<DisplayedIam>(
    DatabaseContract.DISPLAYED_IAM_TABLE_NAME,
    dbHelper,
    concurrentHandlerHolder
) {
    override fun contentValuesFromItem(item: DisplayedIam): ContentValues {
        return ContentValues().apply {
            put(DatabaseContract.DISPLAYED_IAM_COLUMN_NAME_CAMPAIGN_ID, item.campaignId)
            put(DatabaseContract.DISPLAYED_IAM_COLUMN_NAME_TIMESTAMP, item.timestamp)
        }
    }

    override fun itemFromCursor(cursor: Cursor): DisplayedIam {
        val campaignId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.DISPLAYED_IAM_COLUMN_NAME_CAMPAIGN_ID))
        val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.DISPLAYED_IAM_COLUMN_NAME_TIMESTAMP))
        return DisplayedIam(campaignId, timestamp)
    }
}
