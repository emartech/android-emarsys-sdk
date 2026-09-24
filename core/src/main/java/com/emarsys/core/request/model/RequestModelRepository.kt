package com.emarsys.core.request.model

import android.content.ContentValues
import android.database.Cursor
import com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_HEADERS
import com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_METHOD
import com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_PAYLOAD
import com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_REQUEST_ID
import com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_TIMESTAMP
import com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_TTL
import com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_URL
import com.emarsys.core.database.DatabaseContract.REQUEST_TABLE_NAME
import com.emarsys.core.database.helper.DbHelper
import com.emarsys.core.database.repository.AbstractSqliteRepository
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.util.serialization.SerializationException
import com.emarsys.core.util.serialization.SerializationUtils

class RequestModelRepository(
    dbHelper: DbHelper,
    concurrentHandlerHolder: ConcurrentHandlerHolder
) : AbstractSqliteRepository<RequestModel>(REQUEST_TABLE_NAME, dbHelper, concurrentHandlerHolder) {

    override fun contentValuesFromItem(item: RequestModel): ContentValues {
        return ContentValues().apply {
            put(REQUEST_COLUMN_NAME_REQUEST_ID, item.id)
            put(REQUEST_COLUMN_NAME_METHOD, item.method.name)
            put(REQUEST_COLUMN_NAME_URL, item.url.toString())
            put(REQUEST_COLUMN_NAME_HEADERS, SerializationUtils.serializableToBlob(item.headers))
            put(REQUEST_COLUMN_NAME_PAYLOAD, SerializationUtils.serializableToBlob(item.payload))
            put(REQUEST_COLUMN_NAME_TIMESTAMP, item.timestamp)
            put(REQUEST_COLUMN_NAME_TTL, item.ttl)
        }
    }

    override fun itemFromCursor(cursor: Cursor): RequestModel {
        val requestId =
            cursor.getString(cursor.getColumnIndexOrThrow(REQUEST_COLUMN_NAME_REQUEST_ID))
        val method = RequestMethod.valueOf(
            cursor.getString(
                cursor.getColumnIndexOrThrow(REQUEST_COLUMN_NAME_METHOD)
            )
        )
        val url = cursor.getString(cursor.getColumnIndexOrThrow(REQUEST_COLUMN_NAME_URL))

        var headers: Map<String, String> = HashMap()
        try {
            headers = SerializationUtils.blobToSerializable(
                cursor.getBlob(
                    cursor.getColumnIndexOrThrow(REQUEST_COLUMN_NAME_HEADERS)
                )
            ) as? Map<String, String> ?: HashMap()
        } catch (ignored: SerializationException) {
        }

        var payload: Map<String, Any?>? = null
        try {
            val rawPayload = SerializationUtils.blobToSerializable(
                cursor.getBlob(
                    cursor.getColumnIndexOrThrow(REQUEST_COLUMN_NAME_PAYLOAD)
                )
            )
            payload = if (rawPayload is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                rawPayload as Map<String, Any?>
            } else if (rawPayload != null) {
                HashMap()
            } else {
                null
            }
        } catch (ignored: SerializationException) {
            payload = HashMap()
        }

        val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(REQUEST_COLUMN_NAME_TIMESTAMP))
        val ttl = cursor.getLong(cursor.getColumnIndexOrThrow(REQUEST_COLUMN_NAME_TTL))

        return RequestModel(url, method, payload, headers, timestamp, ttl, requestId)
    }
}
