package com.emarsys.core.shard

import android.content.ContentValues
import android.database.Cursor
import com.emarsys.core.Mockable
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.repository.AbstractSqliteRepository
import com.emarsys.core.util.serialization.SerializationException
import com.emarsys.core.util.serialization.SerializationUtils
import java.util.*

@Mockable
class ShardModelRepository(coreDbHelper: CoreDbHelper) : AbstractSqliteRepository<ShardModel>(DatabaseContract.SHARD_TABLE_NAME, coreDbHelper) {
    override fun contentValuesFromItem(item: ShardModel): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(DatabaseContract.SHARD_COLUMN_ID, item.id)
        contentValues.put(DatabaseContract.SHARD_COLUMN_TYPE, item.type)
        contentValues.put(DatabaseContract.SHARD_COLUMN_DATA, SerializationUtils.serializableToBlob(item.data))
        contentValues.put(DatabaseContract.SHARD_COLUMN_TIMESTAMP, item.timestamp)
        contentValues.put(DatabaseContract.SHARD_COLUMN_TTL, item.ttl)
        return contentValues
    }

    override fun itemFromCursor(cursor: Cursor): ShardModel {

        val id = cursor.getString(cursor.getColumnIndex(DatabaseContract.SHARD_COLUMN_ID))
        val type = cursor.getString(cursor.getColumnIndex(DatabaseContract.SHARD_COLUMN_TYPE))
        var data: Map<String?, Any?> = HashMap()
        try {
            data = SerializationUtils
                    .blobToSerializable(cursor.getBlob(cursor.getColumnIndex(DatabaseContract.SHARD_COLUMN_DATA))) as Map<String?, Any?>
        } catch (ignored: SerializationException) {
        } catch (ignored: ClassCastException) {
        }
        val timeStamp = cursor.getLong(cursor.getColumnIndex(DatabaseContract.SHARD_COLUMN_TIMESTAMP))
        val ttl = cursor.getLong(cursor.getColumnIndex(DatabaseContract.SHARD_COLUMN_TTL))
        return ShardModel(id, type, data, timeStamp, ttl)
    }
}