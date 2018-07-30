package com.emarsys.core.shard

import android.content.Context
import android.database.Cursor
import android.support.test.InstrumentationRegistry

import com.emarsys.core.testUtil.DatabaseTestUtils
import com.emarsys.core.testUtil.TimeoutUtils

import org.junit.Before
import org.junit.Rule
import org.junit.Test

import java.io.Serializable

import com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_DATA
import com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_TIMESTAMP
import com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_TTL
import com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_TYPE
import com.emarsys.core.util.serialization.SerializationUtils.serializableToBlob

import com.nhaarman.mockito_kotlin.whenever

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.mockito.Mockito.mock

class ShardModelRepositoryTest {

    private lateinit var shardModel: ShardModel
    private lateinit var repository: ShardModelRepository
    private lateinit var payload: Map<String, Serializable>
    private lateinit var context: Context

    @Rule
    @JvmField
    val timeout = TimeoutUtils.getTimeoutRule()

    companion object {
        const val TYPE: String = "type1"
        const val TIMESTAMP: Long = 1234L
        const val TTL: Long = 4321L
    }

    @Before
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()

        context = InstrumentationRegistry.getContext()

        repository = ShardModelRepository(context)

        payload = mutableMapOf<String, Serializable>().apply {
            this["payload1"] = "payload_value1"
            this["payload2"] = "payload_value2"
        }

        shardModel = ShardModel(TYPE, payload, TIMESTAMP, TTL)
    }

    @Test
    fun testContentValuesFromItem() {
        val result = repository.contentValuesFromItem(shardModel)
        assertEquals(shardModel.getType(), result.getAsString(SHARD_COLUMN_TYPE))
        assertArrayEquals(serializableToBlob(shardModel.getData()), result.getAsByteArray(SHARD_COLUMN_DATA))
        assertEquals(shardModel.getTimestamp(), result.getAsLong(SHARD_COLUMN_TIMESTAMP) as Long)
        assertEquals(shardModel.getTtl(), result.getAsLong(SHARD_COLUMN_TTL) as Long)
    }

    @Test
    fun testItemFromCursor() {
        val cursor = mock(Cursor::class.java)

        whenever(cursor.getColumnIndex(SHARD_COLUMN_TYPE)).thenReturn(0)
        whenever(cursor.getString(0)).thenReturn(TYPE)

        whenever(cursor.getColumnIndex(SHARD_COLUMN_DATA)).thenReturn(1)
        whenever(cursor.getBlob(1)).thenReturn(serializableToBlob(payload))

        whenever(cursor.getColumnIndex(SHARD_COLUMN_TIMESTAMP)).thenReturn(2)
        whenever(cursor.getLong(2)).thenReturn(TIMESTAMP)

        whenever(cursor.getColumnIndex(SHARD_COLUMN_TTL)).thenReturn(3)
        whenever(cursor.getLong(3)).thenReturn(TTL)

        assertEquals(shardModel, repository.itemFromCursor(cursor))
    }
}