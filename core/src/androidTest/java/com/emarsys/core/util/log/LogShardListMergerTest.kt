package com.emarsys.core.util.log

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.testUtil.RandomTestUtils
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class LogShardListMergerTest {

    private companion object {
        const val ID = "id"
        const val TIMESTAMP = 125L
        const val TTL = Long.MAX_VALUE
        const val APPLICATION_CODE = "applicationCode"
        const val MERCHANT_ID = "merchantId"
    }

    private lateinit var merger: LogShardListMerger

    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUuidProvider: UUIDProvider
    private lateinit var mockDeviceInfo: DeviceInfo


    @Before
    fun setUp() {
        mockTimestampProvider = mockk(relaxed = true)
        every { mockTimestampProvider.provideTimestamp() } returns TIMESTAMP

        mockUuidProvider = mockk(relaxed = true)
        every { mockUuidProvider.provideId() } returns ID

        mockDeviceInfo = mockk(relaxed = true)
        every { mockDeviceInfo.platform } returns "android"
        every { mockDeviceInfo.applicationVersion } returns "1.0.0"
        every { mockDeviceInfo.osVersion } returns "8.0"
        every { mockDeviceInfo.model } returns "Pixel"
        every { mockDeviceInfo.clientId } returns "clientId"
        every { mockDeviceInfo.sdkVersion } returns "1.6.1"
        every { mockDeviceInfo.isDebugMode } returns true


        merger = LogShardListMerger(
            mockTimestampProvider,
            mockUuidProvider,
            mockDeviceInfo,
            APPLICATION_CODE,
            MERCHANT_ID
        )
    }

    @Test
    fun testMap_shards_mustContainAtLeastOneElement() {
        shouldThrow<IllegalArgumentException> {
            merger.map(listOf())
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

        val logData =
            shards.map { mapOf("type" to it.type) + mapOf("deviceInfo" to createDeviceInfo()) + it.data }

        val expectedRequestModel = requestModel(mapOf("logs" to logData))

        val result = merger.map(shards)

        result.payload?.keys?.forEach { key ->
            expectedRequestModel.payload?.get(key) shouldBe result.payload?.get(key)
        }
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
            "isDebugMode" to "true",
            "applicationCode" to APPLICATION_CODE,
            "merchantId" to MERCHANT_ID
        )
    }

}