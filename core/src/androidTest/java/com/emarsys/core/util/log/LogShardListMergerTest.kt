package com.emarsys.core.util.log

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.testUtil.RandomTestUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class LogShardListMergerTest  {

    private companion object {
        const val ID = "id"
        const val TIMESTAMP = 125L
        const val TTL = Long.MAX_VALUE
        const val APPLICATION_CODE = "applicationCode"
        const val MERCHANT_ID = "merchantId"
    }

    private lateinit var merger: LogShardListMerger

    private lateinit var timestampProvider: TimestampProvider
    private lateinit var uuidProvider: UUIDProvider
    private lateinit var deviceInfo: DeviceInfo


    @Before
    fun setUp() {
        timestampProvider = mock()
        whenever(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP)

        uuidProvider = mock()
        whenever(uuidProvider.provideId()).thenReturn(ID)

        deviceInfo = mock {
            on { platform } doReturn "android"
            on { applicationVersion } doReturn "1.0.0"
            on { osVersion } doReturn "8.0"
            on { model } doReturn "Pixel"
            on { clientId } doReturn "clientId"
            on { sdkVersion } doReturn "1.6.1"
        }

        merger = LogShardListMerger(
            timestampProvider,
            uuidProvider,
            deviceInfo,
            APPLICATION_CODE,
            MERCHANT_ID
        )
    }

    @Test
    fun testMap_shards_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            merger.map(null)
        }
    }

    @Test
    fun testMap_shards_mustNotContainNullElements() {
        shouldThrow<IllegalArgumentException> {
            merger.map(
                listOf(
                    Mockito.mock(ShardModel::class.java),
                    null,
                    Mockito.mock(ShardModel::class.java)
                )
            )
        }
    }

    @Test
    fun testMap_shards_mustContainAtLeastOneElement() {
        shouldThrow<IllegalArgumentException> {
            merger.map(listOf())
        }
    }

    @Test
    fun testConstructor_timestampProvider_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            LogShardListMerger(null, uuidProvider, deviceInfo, APPLICATION_CODE, MERCHANT_ID)
        }
    }

    @Test
    fun testConstructor_uuidProvider_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            LogShardListMerger(timestampProvider, null, deviceInfo, APPLICATION_CODE, MERCHANT_ID)
        }
    }

    @Test
    fun testConstructor_deviceInfo_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            LogShardListMerger(timestampProvider, uuidProvider, null, APPLICATION_CODE, MERCHANT_ID)
        }
    }

    @Test
    fun testMap_singletonList() {
        val shardData = RandomTestUtils.randomMap()
        val type = "log_crash"
        val shard = ShardModel("id", type, shardData, 1234, 4321)

        val logData = shardData + mapOf("type" to type) + mapOf("deviceInfo" to createDeviceInfo())
        val requestPayload = mapOf("logs" to listOf(logData))
        val expectedRequestModel = requestModel(requestPayload)

        merger.map(listOf(shard)) shouldBe expectedRequestModel
    }

    @Test
    fun testMap_multipleElementsInList() {
        val shards = (1..5).map { randomShardModel() }

        val logDatas =
            shards.map { it.data + mapOf("type" to it.type) + mapOf("deviceInfo" to createDeviceInfo()) }

        val expectedRequestModel = requestModel(mapOf("logs" to logDatas))

        merger.map(shards) shouldBe expectedRequestModel
    }

    private fun randomShardModel() = ShardModel(
        RandomTestUtils.randomString(),
        "log_${RandomTestUtils.randomString()}",
        RandomTestUtils.randomMap(),
        RandomTestUtils.randomInt().toLong(),
        RandomTestUtils.randomInt().toLong()
    )

    private fun requestModel(payload: Map<String, Any>) = RequestModel(
        "https://log-dealer.eservice.emarsys.net/v1/log",
        RequestMethod.POST,
        payload,
        mapOf(),
        TIMESTAMP,
        TTL,
        ID
    )

    private fun createDeviceInfo(): Map<String, String> {
        return mapOf(
            "platform" to "android",
            "appVersion" to "1.0.0",
            "sdkVersion" to "1.6.1",
            "osVersion" to "8.0",
            "model" to "Pixel",
            "hwId" to "clientId",
            "applicationCode" to APPLICATION_CODE,
            "merchantId" to MERCHANT_ID
        )
    }

}