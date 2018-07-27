package com.emarsys.core.shard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.testUtil.DatabaseTestUtils;
import com.emarsys.core.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.Serializable;
import java.util.HashMap;

import static com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_DATA;
import static com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_TIMESTAMP;
import static com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_TTL;
import static com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_TYPE;
import static com.emarsys.core.util.serialization.SerializationUtils.serializableToBlob;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ShardModelRepositoryTest {

    private ShardModel shardModel;
    private ShardModelRepository repository;
    Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        DatabaseTestUtils.INSTANCE.deleteCoreDatabase();

        context = InstrumentationRegistry.getContext();

        repository = new ShardModelRepository(context);

        HashMap<String, Serializable> payload = new HashMap<>();
        payload.put("payload1", "payload_value1");
        payload.put("payload2", "payload_value2");

        shardModel = new ShardModel("type1", payload, 1234, 4321);
    }

    @Test
    public void testContentValuesFromItem() {
        ContentValues result = repository.contentValuesFromItem(shardModel);
        assertEquals(shardModel.getType(), result.getAsString(SHARD_COLUMN_TYPE));
        assertArrayEquals(serializableToBlob(shardModel.getData()), result.getAsByteArray(SHARD_COLUMN_DATA));
        assertEquals(shardModel.getTimestamp(), (long)result.getAsLong(SHARD_COLUMN_TIMESTAMP));
        assertEquals(shardModel.getTtl(), (long)result.getAsLong(SHARD_COLUMN_TTL));
    }

    @Test
    public void testItemFromCursor() {
        Cursor cursor = mock(Cursor.class);

        when(cursor.getColumnIndex(SHARD_COLUMN_TYPE)).thenReturn(0);
        when(cursor.getString(0)).thenReturn(shardModel.getType());

        when(cursor.getColumnIndex(SHARD_COLUMN_DATA)).thenReturn(1);
        when(cursor.getBlob(1)).thenReturn(serializableToBlob(shardModel.getData()));

        when(cursor.getColumnIndex(SHARD_COLUMN_TIMESTAMP)).thenReturn(2);
        when(cursor.getLong(2)).thenReturn(shardModel.getTimestamp());

        when(cursor.getColumnIndex(SHARD_COLUMN_TTL)).thenReturn(3);
        when(cursor.getLong(3)).thenReturn(shardModel.getTtl());

        ShardModel result = repository.itemFromCursor(cursor);

        assertEquals(shardModel, result);
    }
}