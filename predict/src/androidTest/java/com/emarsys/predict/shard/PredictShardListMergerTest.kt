package com.emarsys.predict.shard

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.predict.provider.PredictRequestModelBuilderProvider
import com.emarsys.predict.request.PredictRequestContext
import com.emarsys.predict.request.PredictRequestModelBuilder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class PredictShardListMergerTest {

    private companion object {
        const val ID = "id"
        const val TIMESTAMP = 125L
        const val VISITOR_ID = "888999888"
        const val CONTACT_ID = "12345"
        const val OS_VERSION = "1.0.0"
        const val PLATFORM = "android"
        const val MERCHANT_ID = "merchantId555"
    }

    private lateinit var mockPredictRequestContext: PredictRequestContext
    private lateinit var merger: PredictShardListMerger

    private lateinit var mockStore: KeyValueStore
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUuidProvider: UUIDProvider
    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var mockPredictRequestModelBuilderProvider: PredictRequestModelBuilderProvider
    private lateinit var mockPredictRequestModelBuilder: PredictRequestModelBuilder

    private lateinit var shard1: ShardModel
    private lateinit var shard2: ShardModel
    private lateinit var shard3: ShardModel


    @Before
    fun init() {
        mockStore = mockk(relaxed = true)
        mockTimestampProvider = mockk(relaxed = true)
        mockUuidProvider = mockk(relaxed = true)
        mockDeviceInfo = mockk(relaxed = true)

        mockPredictRequestContext = mockk(relaxed = true)
        every { mockPredictRequestContext.deviceInfo } returns mockDeviceInfo
        every { mockPredictRequestContext.uuidProvider } returns mockUuidProvider
        every { mockPredictRequestContext.timestampProvider } returns mockTimestampProvider
        every { mockPredictRequestContext.keyValueStore } returns mockStore
        every { mockPredictRequestContext.merchantId } returns MERCHANT_ID


        mockPredictRequestModelBuilder = mockk(relaxed = true)
        every { mockPredictRequestModelBuilder.withLimit(any()) } returns mockPredictRequestModelBuilder
        every { mockPredictRequestModelBuilder.withShardData(any()) } returns mockPredictRequestModelBuilder


        mockPredictRequestModelBuilderProvider =
            mockk(relaxed = true)
        every { mockPredictRequestModelBuilderProvider.providePredictRequestModelBuilder() } returns
                mockPredictRequestModelBuilder

        every { mockTimestampProvider.provideTimestamp() } returns TIMESTAMP
        every { mockUuidProvider.provideId() } returns ID
        every { mockDeviceInfo.osVersion } returns OS_VERSION
        every { mockDeviceInfo.platform } returns PLATFORM

        merger = PredictShardListMerger(
            mockPredictRequestContext,
            mockPredictRequestModelBuilderProvider
        )

        shard1 = ShardModel("id1", "type1", mapOf("q1" to 1, "q2" to "b"), 100, 100)
        shard2 = ShardModel("id2", "type2", mapOf("q3" to "c"), 110, 100)
        shard3 = ShardModel("id3", "type3", mapOf("<>," to "\"`;/?:^%#@&=\$+{}<>,| "), 120, 100)
    }

    @Test
    fun testConstructor_predictRequestContext_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            PredictShardListMerger(null, mockPredictRequestModelBuilderProvider)
        }
    }

    @Test
    fun testConstructor_predictRequestModelBuilderProvider_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            PredictShardListMerger(mockPredictRequestContext, null)
        }
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
                    mockk(relaxed = true),
                    null,
                    mockk(relaxed = true)
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
    fun testMap_singletonList_withMultipleParams() {
        every { mockStore.getString("predict_visitor_id") } returns null
        every { mockStore.getString("predict_contact_id") } returns null
        val expected = mapOf(
            "cp" to 1,
            "q1" to 1,
            "q2" to "b"
        )
        merger.map(listOf(shard1))

        verify { mockPredictRequestModelBuilder.withShardData(expected) }
        verify { mockPredictRequestModelBuilder.build() }
    }

    @Test
    fun testMap_multipleShards_shouldInclude_visitorId_andContactFieldValue() {
        every { mockStore.getString("predict_visitor_id") } returns VISITOR_ID
        every { mockStore.getString("predict_contact_id") } returns CONTACT_ID
        val expected = mapOf(
            "cp" to 1,
            "vi" to VISITOR_ID,
            "ci" to CONTACT_ID,
            "q1" to 1,
            "q2" to "b",
            "q3" to "c"
        )

        merger.map(listOf(shard1, shard2))

        verify { mockPredictRequestModelBuilder.withShardData(expected) }
        verify { mockPredictRequestModelBuilder.build() }
    }

    @Test
    fun testMap_withoutVisitorIdOrContactId() {
        every { mockStore.getString("predict_visitor_id") } returns null
        every { mockStore.getString("predict_contact_id") } returns null
        val expected = mapOf(
            "cp" to 1,
            "<>," to "\"`;/?:^%#@&=\$+{}<>,| "
        )

        merger.map(listOf(shard3))

        verify { mockPredictRequestModelBuilder.withShardData(expected) }
        verify { mockPredictRequestModelBuilder.build() }
    }

    @Test
    fun testMap_withVisitorIdPresent() {
        every { mockStore.getString("predict_visitor_id") } returns VISITOR_ID
        every { mockStore.getString("predict_contact_id") } returns null

        val expected = mapOf(
            "cp" to 1,
            "q3" to "c",
            "vi" to "888999888"
        )

        merger.map(listOf(shard2))

        verify { mockPredictRequestModelBuilder.withShardData(expected) }
        verify { mockPredictRequestModelBuilder.build() }
    }

    @Test
    fun testMap_withContactIdPresent() {
        every { mockStore.getString("predict_contact_id") } returns CONTACT_ID
        every { mockStore.getString("predict_visitor_id") } returns null

        val expected = mapOf(
            "cp" to 1,
            "q3" to "c"
        )

        merger.map(listOf(shard2))

        verify { mockPredictRequestModelBuilder.withShardData(expected) }
        verify { mockPredictRequestModelBuilder.build() }
    }

    @Test
    fun testMap_withBoth_visitorIdAndContactIdPresent() {
        every { mockStore.getString("predict_visitor_id") } returns VISITOR_ID
        every { mockStore.getString("predict_contact_id") } returns CONTACT_ID

        val expected = mapOf(
            "cp" to 1,
            "q3" to "c",
            "vi" to "888999888"
        )

        merger.map(listOf(shard2))

        verify { mockPredictRequestModelBuilder.withShardData(expected) }
        verify { mockPredictRequestModelBuilder.build() }
    }

    @Test
    fun testMap_shouldUseRequestModelBuilder() {
        val expected: RequestModel = mockk(relaxed = true)

        every { mockPredictRequestModelBuilder.build() } returns expected

        val result = merger.map(listOf(shard1))

        verify { mockPredictRequestModelBuilder.withShardData(any()) }
        verify { mockPredictRequestModelBuilder.build() }
        result shouldBe expected
    }
}