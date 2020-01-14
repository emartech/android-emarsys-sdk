package com.emarsys.core.request.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.helper.DbHelper;
import com.emarsys.core.database.repository.AbstractSqliteRepository;
import com.emarsys.core.util.serialization.SerializationException;

import java.util.HashMap;
import java.util.Map;

import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_HEADERS;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_METHOD;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_PAYLOAD;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_REQUEST_ID;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_TIMESTAMP;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_TTL;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_URL;
import static com.emarsys.core.util.serialization.SerializationUtils.blobToSerializable;
import static com.emarsys.core.util.serialization.SerializationUtils.serializableToBlob;

public class RequestModelRepository extends AbstractSqliteRepository<RequestModel> {

    public RequestModelRepository(DbHelper coreDbHelper) {
        super(DatabaseContract.REQUEST_TABLE_NAME, coreDbHelper);
    }

    @Override
    public ContentValues contentValuesFromItem(RequestModel item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(REQUEST_COLUMN_NAME_REQUEST_ID, item.getId());
        contentValues.put(REQUEST_COLUMN_NAME_METHOD, item.getMethod().name());
        contentValues.put(REQUEST_COLUMN_NAME_URL, item.getUrl().toString());
        contentValues.put(REQUEST_COLUMN_NAME_HEADERS, serializableToBlob(item.getHeaders()));
        contentValues.put(REQUEST_COLUMN_NAME_PAYLOAD, serializableToBlob(item.getPayload()));
        contentValues.put(REQUEST_COLUMN_NAME_TIMESTAMP, item.getTimestamp());
        contentValues.put(REQUEST_COLUMN_NAME_TTL, item.getTtl());
        return contentValues;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RequestModel itemFromCursor(Cursor cursor) {
        String requestId = cursor.getString(cursor.getColumnIndex(REQUEST_COLUMN_NAME_REQUEST_ID));
        RequestMethod method = RequestMethod.valueOf(cursor.getString(cursor.getColumnIndex(REQUEST_COLUMN_NAME_METHOD)));
        String url = cursor.getString(cursor.getColumnIndex(REQUEST_COLUMN_NAME_URL));

        Map<String, String> headers = new HashMap<>();
        try {
            headers = (Map<String, String>) blobToSerializable(cursor.getBlob(cursor.getColumnIndex(REQUEST_COLUMN_NAME_HEADERS)));
        } catch (SerializationException | ClassCastException ignored) {
        }

        Map<String, Object> payload = new HashMap<>();
        try {
            payload = (Map<String, Object>) blobToSerializable(cursor.getBlob(cursor.getColumnIndex(REQUEST_COLUMN_NAME_PAYLOAD)));
        } catch (SerializationException | ClassCastException ignored) {
        }

        long timeStamp = cursor.getLong(cursor.getColumnIndex(REQUEST_COLUMN_NAME_TIMESTAMP));
        long ttl = cursor.getLong(cursor.getColumnIndex(REQUEST_COLUMN_NAME_TTL));

        return new RequestModel(url, method, payload, headers, timeStamp, ttl, requestId);
    }

}
