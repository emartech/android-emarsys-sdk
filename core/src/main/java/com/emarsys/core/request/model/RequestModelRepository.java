package com.emarsys.core.request.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.helper.CoreDbHelper;
import com.emarsys.core.database.repository.AbstractSqliteRepository;
import com.emarsys.core.util.log.CoreTopic;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.core.util.serialization.SerializationException;

import java.util.HashMap;
import java.util.Map;

import static com.emarsys.core.database.DatabaseContract.COLUMN_NAME_HEADERS;
import static com.emarsys.core.database.DatabaseContract.COLUMN_NAME_METHOD;
import static com.emarsys.core.database.DatabaseContract.COLUMN_NAME_PAYLOAD;
import static com.emarsys.core.database.DatabaseContract.COLUMN_NAME_REQUEST_ID;
import static com.emarsys.core.database.DatabaseContract.COLUMN_NAME_TIMESTAMP;
import static com.emarsys.core.database.DatabaseContract.COLUMN_NAME_TTL;
import static com.emarsys.core.database.DatabaseContract.COLUMN_NAME_URL;
import static com.emarsys.core.util.serialization.SerializationUtils.blobToSerializable;
import static com.emarsys.core.util.serialization.SerializationUtils.serializableToBlob;

public class RequestModelRepository extends AbstractSqliteRepository<RequestModel> {

    public RequestModelRepository(Context context) {
        super(DatabaseContract.REQUEST_TABLE_NAME, new CoreDbHelper(context));
    }

    @Override
    protected ContentValues contentValuesFromItem(RequestModel item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME_REQUEST_ID, item.getId());
        contentValues.put(COLUMN_NAME_METHOD, item.getMethod().name());
        contentValues.put(COLUMN_NAME_URL, item.getUrl().toString());
        contentValues.put(COLUMN_NAME_HEADERS, serializableToBlob(item.getHeaders()));
        contentValues.put(COLUMN_NAME_PAYLOAD, serializableToBlob(item.getPayload()));
        contentValues.put(COLUMN_NAME_TIMESTAMP, item.getTimestamp());
        contentValues.put(COLUMN_NAME_TTL, item.getTtl());
        return contentValues;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected RequestModel itemFromCursor(Cursor cursor) {
        String requestId = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_REQUEST_ID));
        RequestMethod method = RequestMethod.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_METHOD)));
        String url = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_URL));

        Map<String, String> headers = new HashMap<>();
        try {
            headers = (Map<String, String>) blobToSerializable(cursor.getBlob(cursor.getColumnIndex(COLUMN_NAME_HEADERS)));
        } catch (SerializationException | ClassCastException e) {
            EMSLogger.log(CoreTopic.UTIL, "Exception: %s", e);
        }

        Map<String, Object> payload = new HashMap<>();
        try {
            payload = (Map<String, Object>) blobToSerializable(cursor.getBlob(cursor.getColumnIndex(COLUMN_NAME_PAYLOAD)));
        } catch (SerializationException | ClassCastException e) {
            EMSLogger.log(CoreTopic.UTIL, "Exception: %s", e);
        }

        long timeStamp = cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_TIMESTAMP));
        long ttl = cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_TTL));

        return new RequestModel(url, method, payload, headers, timeStamp, ttl, requestId);
    }

}
