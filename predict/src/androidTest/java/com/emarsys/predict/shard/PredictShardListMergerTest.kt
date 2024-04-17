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
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.mockito.whenever
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.any
import org.mockito.Mockito.anyMap
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class PredictShardListMergerTest : AnnotationSpec() {

    private companion object {
        const val ID = "id"
        const val TIMESTAMP = 125L
        const val TTL = Long.MAX_VALUE
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
        mockStore = mock(KeyValueStore::class.java)
        mockTimestampProvider = mock(TimestampProvider::class.java)
        mockUuidProvider = mock(UUIDProvider::class.java)
        mockDeviceInfo = mock(DeviceInfo::class.java)

        mockPredictRequestContext = mock(PredictRequestContext::class.java).apply {
            whenever(deviceInfo).thenReturn(mockDeviceInfo)
            whenever(uuidProvider).thenReturn(mockUuidProvider)
            whenever(timestampProvider).thenReturn(mockTimestampProvider)
            whenever(keyValueStore).thenReturn(mockStore)
            whenever(merchantId).thenReturn(MERCHANT_ID)
        }

        mockPredictRequestModelBuilder = mock(PredictRequestModelBuilder::class.java).apply {
            whenever(withLimit(any())).thenReturn(this)
            whenever(withShardData(anyMap())).thenReturn(this)
        }

        mockPredictRequestModelBuilderProvider =
            mock(PredictRequestModelBuilderProvider::class.java).apply {
                whenever(providePredictRequestModelBuilder()).thenReturn(
                    mockPredictRequestModelBuilder
                )
            }


        whenever(mockTimestampProvider.provideTimestamp()).thenReturn(TIMESTAMP)
        whenever(mockUuidProvider.provideId()).thenReturn(ID)
        whenever(mockDeviceInfo.osVersion).thenReturn(OS_VERSION)
        whenever(mockDeviceInfo.platform).thenReturn(PLATFORM)

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
                    mock(ShardModel::class.java),
                    null,
                    mock(ShardModel::class.java)
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
        val expected = mapOf(
            "cp" to 1,
            "q1" to 1,
            "q2" to "b"
        )
        merger.map(listOf(shard1))

        verify(mockPredictRequestModelBuilder).withShardData(expected)
        verify(mockPredictRequestModelBuilder).build()
    }

    @Test
    fun testMap_multipleShards() {
        val expected = mapOf(
            "cp" to 1,
            "q1" to 1,
            "q2" to "b",
            "q3" to "c"
        )
        merger.map(listOf(shard1, shard2))

        verify(mockPredictRequestModelBuilder).withShardData(expected)
        verify(mockPredictRequestModelBuilder).build()
    }

    @Test
    fun testMap_withoutVisitorIdOrContactId() {
        val expected = mapOf(
            "cp" to 1,
            "<>," to "\"`;/?:^%#@&=\$+{}<>,| "
        )

        merger.map(listOf(shard3))

        verify(mockPredictRequestModelBuilder).withShardData(expected)
        verify(mockPredictRequestModelBuilder).build()
    }

    @Test
    fun testMap_withVisitorIdPresent() {
        whenever(mockStore.getString("predict_visitor_id")).thenReturn(VISITOR_ID)

        val expected = mapOf(
            "cp" to 1,
            "q3" to "c",
            "vi" to "888999888"
        )

        merger.map(listOf(shard2))

        verify(mockPredictRequestModelBuilder).withShardData(expected)
        verify(mockPredictRequestModelBuilder).build()
    }

    @Test
    fun testMap_withContactIdPresent() {
        whenever(mockStore.getString("predict_contact_id")).thenReturn(CONTACT_ID)

        val expected = mapOf(
            "cp" to 1,
            "q3" to "c"
        )

        merger.map(listOf(shard2))

        verify(mockPredictRequestModelBuilder).withShardData(expected)
        verify(mockPredictRequestModelBuilder).build()
    }

    @Test
    fun testMap_withBoth_visitorIdAndContactIdPresent() {
        whenever(mockStore.getString("predict_visitor_id")).thenReturn(VISITOR_ID)
        whenever(mockStore.getString("predict_contact_id")).thenReturn(CONTACT_ID)

        val expected = mapOf(
            "cp" to 1,
            "q3" to "c",
            "vi" to "888999888"
        )

        merger.map(listOf(shard2))

        verify(mockPredictRequestModelBuilder).withShardData(expected)
        verify(mockPredictRequestModelBuilder).build()
    }

    @Test
    fun testMap_shouldUseRequestModelBuilder() {
        val expected = mock(RequestModel::class.java)

        whenever(mockPredictRequestModelBuilder.build()).thenReturn(expected)

        val result = merger.map(listOf(shard1))

        verify(mockPredictRequestModelBuilder).withShardData(anyMap())
        verify(mockPredictRequestModelBuilder).build()
        result shouldBe expected
    }
}