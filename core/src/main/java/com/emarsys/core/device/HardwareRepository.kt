package com.emarsys.core.device

import android.content.ContentValues
import android.database.Cursor
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.helper.DbHelper
import com.emarsys.core.database.repository.AbstractSqliteRepository

class HardwareRepository(dbHelper: DbHelper) : AbstractSqliteRepository<Hardware?>(DatabaseContract.HARDWARE_INFORMATION_TABLE_NAME, dbHelper) {

    override fun contentValuesFromItem(item: Hardware?): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(DatabaseContract.HARDWARE_COLUMN_NAME_HARDWARE_ID, item?.hardwareId)
        return contentValues
    }

    override fun itemFromCursor(cursor: Cursor): Hardware {
        val hardwareId = cursor.getString(cursor.getColumnIndex(DatabaseContract.HARDWARE_COLUMN_NAME_HARDWARE_ID))
        return Hardware(hardwareId)
    }

}