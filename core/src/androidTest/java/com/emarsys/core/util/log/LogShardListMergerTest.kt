package com.emarsys.core.util.log

import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.testUtil.RandomTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils
import io.kotlintest.shouldBe
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class LogShardListMergerTest {

    companion object {
        const val ID = "id"
        const val TIMESTAMP = 125L
        const val TTL = Long.MAX_VALUE
    }

    lateinit var merger: LogShardListMerger

    lateinit var timestampProvider: TimestampProvider
    lateinit var uuidProvider: UUIDProvider

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        timestampProvider = Mockito.mock(TimestampProvider::class.java)
        MockitoTestUtils.whenever(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP)

        uuidProvider = Mockito.mock(UUIDProvider::class.java)
        MockitoTestUtils.whenever(uuidProvider.provideId()).thenReturn(ID)

        merger = LogShardListMerger(timestampProvider, uuidProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMap_shards_mustNotBeNull() {
        merger.map(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMap_shards_mustNotContainNullElements() {
        merger.map(listOf(
                Mockito.mock(ShardModel::class.java),
                null,
                Mockito.mock(ShardModel::class.java)
        ))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMap_shards_mustContainAtLeastOneElement() {
        merger.map(listOf())
    }

    @Test
    fun testMap_singletonList() {
        val shardData = RandomTestUtils.randomMap()
        val type = "log_crash"
        val shard = ShardModel("id", type, shardData, 1234, 4321)

        val logData = shardData + mapOf("type" to type)
        val requestPayload = mapOf("logs" to listOf(logData))
        val expectedRequestModel = requestModel(requestPayload)

        assertEquals(expectedRequestModel, merger.map(listOf(shard)))
    }

    @Test
    fun testMap_multipleElementsInList() {
        val shards = (1..5).map { randomShardModel() }

        val logDatas = shards.map { it.data +  mapOf("type" to it.type)}

        val expectedRequestModel = requestModel(mapOf("logs" to logDatas))

        merger.map(shards) shouldBe expectedRequestModel
    }

    private fun randomShardModel() = ShardModel(
            RandomTestUtils.randomString(),
            "log_${RandomTestUtils.randomString()}",
            RandomTestUtils.randomMap(),
            RandomTestUtils.randomInt().toLong(),
            RandomTestUtils.randomInt().toLong())

    private fun requestModel(payload: Map<String, Any>) = RequestModel(
            "https://ems-log-dealer.herokuapp.com/v1/log",
            RequestMethod.POST,
            payload,
            mapOf(),
            TIMESTAMP,
            TTL,
            ID
    )
}