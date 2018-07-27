package com.emarsys.core.shard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.helper.CoreDbHelper;
import com.emarsys.core.database.repository.AbstractSqliteRepository;
import com.emarsys.core.util.log.CoreTopic;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.core.util.serialization.SerializationException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_DATA;
import static com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_TIMESTAMP;
import static com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_TTL;
import static com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_TYPE;
import static com.emarsys.core.util.serialization.SerializationUtils.blobToSerializable;
import static com.emarsys.core.util.serialization.SerializationUtils.serializableToBlob;

public class ShardModelRepository extends AbstractSqliteRepository<ShardModel> {

    public ShardModelRepository(Context context) {
        super(DatabaseContract.SHARD_TABLE_NAME, new CoreDbHelper(context));
    }

    @Override
    protected ContentValues contentValuesFromItem(ShardModel item) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(SHARD_COLUMN_TYPE, item.getType());
        contentValues.put(SHARD_COLUMN_DATA, serializableToBlob(item.getData()));
        contentValues.put(SHARD_COLUMN_TIMESTAMP, item.getTimestamp());
        contentValues.put(SHARD_COLUMN_TTL, item.getTtl());

        return contentValues;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ShardModel itemFromCursor(Cursor cursor) {
        String type = cursor.getString(cursor.getColumnIndex(SHARD_COLUMN_TYPE));

        Map<String, Serializable> data = new HashMap<>();
        try {
            data = (Map<String, Serializable>) blobToSerializable(cursor.getBlob(cursor.getColumnIndex(SHARD_COLUMN_DATA)));
        } catch (SerializationException | ClassCastException e) {
            EMSLogger.log(CoreTopic.UTIL, "Exception: %s", e);
        }

        long timeStamp = cursor.getLong(cursor.getColumnIndex(SHARD_COLUMN_TIMESTAMP));
        long ttl = cursor.getLong(cursor.getColumnIndex(SHARD_COLUMN_TTL));

        return new ShardModel(type, data, timeStamp, ttl);
    }


}
