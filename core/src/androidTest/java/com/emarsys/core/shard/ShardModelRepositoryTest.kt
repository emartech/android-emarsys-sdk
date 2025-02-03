package com.emarsys.core.shard

import android.content.Context
import android.database.Cursor
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_DATA
import com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_ID
import com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_TIMESTAMP
import com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_TTL
import com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_TYPE
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.util.serialization.SerializationUtils.serializableToBlob
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.Serializable

class ShardModelRepositoryTest  {

    private lateinit var shardModel: ShardModel
    private lateinit var repository: ShardModelRepository
    private lateinit var payload: Map<String, Serializable>
    private lateinit var context: Context


    companion object {
        const val TYPE: String = "type1"
        const val TIMESTAMP: Long = 1234L
        const val TTL: Long = 4321L
        const val SHARD_ID = "shard_id"
    }

    @Before
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()
        context = InstrumentationRegistry.getTargetContext()
        val concurrentHandlerHolder: ConcurrentHandlerHolder =
            ConcurrentHandlerHolderFactory.create()
        repository =
            ShardModelRepository(CoreDbHelper(context, mutableMapOf()), concurrentHandlerHolder)

        payload = mutableMapOf<String, Serializable>().apply {
            this["payload1"] = "payload_value1"
            this["payload2"] = "payload_value2"
        }

        shardModel = ShardModel(SHARD_ID, TYPE, payload, TIMESTAMP, TTL)
    }

    @Test
    fun testContentValuesFromItem() {
        val result = repository.contentValuesFromItem(shardModel)
        result.getAsString(SHARD_COLUMN_ID) shouldBe shardModel.id
        result.getAsString(SHARD_COLUMN_TYPE) shouldBe shardModel.type
        result.getAsLong(SHARD_COLUMN_TIMESTAMP) shouldBe shardModel.timestamp
        result.getAsLong(SHARD_COLUMN_TTL) shouldBe shardModel.ttl
        Assert.assertArrayEquals(
            serializableToBlob(shardModel.data),
            result.getAsByteArray(SHARD_COLUMN_DATA)
        )
    }

    @Test
    fun testItemFromCursor() {
        val cursor: Cursor = mock()

        whenever(cursor.getColumnIndexOrThrow(SHARD_COLUMN_ID)).thenReturn(0)
        whenever(cursor.getString(0)).thenReturn(SHARD_ID)

        whenever(cursor.getColumnIndexOrThrow(SHARD_COLUMN_TYPE)).thenReturn(1)
        whenever(cursor.getString(1)).thenReturn(TYPE)

        whenever(cursor.getColumnIndexOrThrow(SHARD_COLUMN_DATA)).thenReturn(2)
        whenever(cursor.getBlob(2)).thenReturn(serializableToBlob(payload))

        whenever(cursor.getColumnIndexOrThrow(SHARD_COLUMN_TIMESTAMP)).thenReturn(3)
        whenever(cursor.getLong(3)).thenReturn(TIMESTAMP)

        whenever(cursor.getColumnIndexOrThrow(SHARD_COLUMN_TTL)).thenReturn(4)
        whenever(cursor.getLong(4)).thenReturn(TTL)

        repository.itemFromCursor(cursor) shouldBe shardModel
    }
}