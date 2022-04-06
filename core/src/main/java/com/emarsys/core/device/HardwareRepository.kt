package com.emarsys.core.device

import android.content.ContentValues
import android.database.Cursor
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.helper.DbHelper
import com.emarsys.core.database.repository.AbstractSqliteRepository
import com.emarsys.core.handler.ConcurrentHandlerHolder

class HardwareRepository(dbHelper: DbHelper, concurrentHandlerHolder: ConcurrentHandlerHolder) :
    AbstractSqliteRepository<HardwareIdentification?>(
        DatabaseContract.HARDWARE_IDENTIFICATION_TABLE_NAME, dbHelper,
        concurrentHandlerHolder
    ) {

    override fun contentValuesFromItem(item: HardwareIdentification?): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(
            DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_HARDWARE_ID,
            item?.hardwareId
        )
        contentValues.put(
            DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_ENCRYPTED_HARDWARE_ID,
            item?.encryptedHardwareId
        )
        contentValues.put(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_SALT, item?.salt)
        contentValues.put(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_IV, item?.iv)
        return contentValues
    }

    override fun itemFromCursor(cursor: Cursor): HardwareIdentification {
        val hardwareId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_HARDWARE_ID))
        val encryptedHardwareId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_ENCRYPTED_HARDWARE_ID))
        val salt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_SALT))
        val iv = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_IV))
        return HardwareIdentification(hardwareId, encryptedHardwareId, salt, iv)
    }

}