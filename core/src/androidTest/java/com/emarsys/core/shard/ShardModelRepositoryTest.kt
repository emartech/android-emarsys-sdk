package com.emarsys.core.shard

import android.content.Context
import android.database.Cursor
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_DATA
import com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_ID
import com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_TIMESTAMP
import com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_TTL
import com.emarsys.core.database.DatabaseContract.SHARD_COLUMN_TYPE
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.util.serialization.SerializationUtils.serializableToBlob
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.io.Serializable

@RunWith(AndroidJUnit4::class)
class ShardModelRepositoryTest {

    private lateinit var shardModel: ShardModel
    private lateinit var repository: ShardModelRepository
    private lateinit var payload: Map<String, Serializable>
    private lateinit var context: Context

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

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

        repository = ShardModelRepository(CoreDbHelper(context, mapOf()))

        payload = mutableMapOf<String, Serializable>().apply {
            this["payload1"] = "payload_value1"
            this["payload2"] = "payload_value2"
        }

        shardModel = ShardModel(SHARD_ID, TYPE, payload, TIMESTAMP, TTL)
    }

    @Test
    fun testContentValuesFromItem() {
        val result = repository.contentValuesFromItem(shardModel)
        Assert.assertEquals(shardModel.id, result.getAsString(SHARD_COLUMN_ID))
        Assert.assertEquals(shardModel.type, result.getAsString(SHARD_COLUMN_TYPE))
        Assert.assertArrayEquals(serializableToBlob(shardModel.data), result.getAsByteArray(SHARD_COLUMN_DATA))
        Assert.assertEquals(shardModel.timestamp, result.getAsLong(SHARD_COLUMN_TIMESTAMP))
        Assert.assertEquals(shardModel.ttl, result.getAsLong(SHARD_COLUMN_TTL))
    }

    @Test
    fun testItemFromCursor() {
        val cursor = mock(Cursor::class.java)

        `when`(cursor.getColumnIndex(SHARD_COLUMN_ID)).thenReturn(0)
        `when`(cursor.getString(0)).thenReturn(SHARD_ID)

        `when`(cursor.getColumnIndex(SHARD_COLUMN_TYPE)).thenReturn(1)
        `when`(cursor.getString(1)).thenReturn(TYPE)

        `when`(cursor.getColumnIndex(SHARD_COLUMN_DATA)).thenReturn(2)
        `when`(cursor.getBlob(2)).thenReturn(serializableToBlob(payload))

        `when`(cursor.getColumnIndex(SHARD_COLUMN_TIMESTAMP)).thenReturn(3)
        `when`(cursor.getLong(3)).thenReturn(TIMESTAMP)

        `when`(cursor.getColumnIndex(SHARD_COLUMN_TTL)).thenReturn(4)
        `when`(cursor.getLong(4)).thenReturn(TTL)

        Assert.assertEquals(shardModel, repository.itemFromCursor(cursor))
    }
}