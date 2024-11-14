package com.emarsys.core.device

import android.content.ContentValues
import android.database.Cursor
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.helper.DbHelper
import com.emarsys.core.database.repository.AbstractSqliteRepository
import com.emarsys.core.handler.ConcurrentHandlerHolder

class ClientRepository(dbHelper: DbHelper, concurrentHandlerHolder: ConcurrentHandlerHolder) :
    AbstractSqliteRepository<ClientIdentification?>(
        DatabaseContract.CLIENT_IDENTIFICATION_TABLE_NAME, dbHelper,
        concurrentHandlerHolder
    ) {

    override fun contentValuesFromItem(item: ClientIdentification?): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(
            DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_CLIENT_ID,
            item?.clientId
        )
        contentValues.put(
            DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_ENCRYPTED_CLIENT_ID,
            item?.encryptedClientId
        )
        contentValues.put(DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_SALT, item?.salt)
        contentValues.put(DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_IV, item?.iv)
        return contentValues
    }

    override fun itemFromCursor(cursor: Cursor): ClientIdentification {
        val clientId =
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_CLIENT_ID))
        val encryptedClientId =
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_ENCRYPTED_CLIENT_ID))
        val salt =
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_SALT))
        val iv =
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_IV))
        return ClientIdentification(clientId, encryptedClientId, salt, iv)
    }

}