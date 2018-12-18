package com.emarsys.predict.shard

import com.emarsys.core.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class PredictShardListMergerTest {

    companion object {
        const val ID = "id"
        const val TIMESTAMP = 125L
        const val TTL = Long.MAX_VALUE
        const val VISITOR_ID = "888999888"
        const val CUSTOMER_ID = "12345"
        const val OS_VERSION = "1.0.0"
        const val PLATFORM = "android"
    }

    lateinit var merger: PredictShardListMerger

    lateinit var merchantId: String
    lateinit var store: KeyValueStore
    lateinit var timestampProvider: TimestampProvider
    lateinit var uuidProvider: UUIDProvider
    lateinit var deviceInfo: DeviceInfo

    lateinit var shard1: ShardModel
    lateinit var shard2: ShardModel
    lateinit var shard3: ShardModel

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun init() {
        merchantId = "merchantId555"
        store = mock(KeyValueStore::class.java)
        timestampProvider = mock(TimestampProvider::class.java)
        uuidProvider = mock(UUIDProvider::class.java)
        deviceInfo = mock(DeviceInfo::class.java)

        whenever(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP)
        whenever(uuidProvider.provideId()).thenReturn(ID)
        whenever(deviceInfo.osVersion).thenReturn(OS_VERSION)
        whenever(deviceInfo.platform).thenReturn(PLATFORM)

        merger = PredictShardListMerger(merchantId, store, timestampProvider, uuidProvider, deviceInfo)

        shard1 = ShardModel("id1", "type1", mapOf("q1" to 1, "q2" to "b"), 100, 100)
        shard2 = ShardModel("id2", "type2", mapOf("q3" to "c"), 110, 100)
        shard3 = ShardModel("id3", "type3", mapOf("<>," to "\"`;/?:^%#@&=\$+{}<>,| "), 120, 100)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_merchantId_mustNotBeNull() {
        PredictShardListMerger(null, store, timestampProvider, uuidProvider, deviceInfo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_keyValueStore_mustNotBeNull() {
        PredictShardListMerger(merchantId, null, timestampProvider, uuidProvider, deviceInfo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_timestampProvider_mustNotBeNull() {
        PredictShardListMerger(merchantId, store, null, uuidProvider, deviceInfo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_uuidProvider_mustNotBeNull() {
        PredictShardListMerger(merchantId, store, timestampProvider, null, deviceInfo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_deviceInfo_mustNotBeNull() {
        PredictShardListMerger(merchantId, store, timestampProvider, uuidProvider, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMap_shards_mustNotBeNull() {
        merger.map(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMap_shards_mustNotContainNullElements() {
        merger.map(listOf(
                mock(ShardModel::class.java),
                null,
                mock(ShardModel::class.java)
        ))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMap_shards_mustContainAtLeastOneElement() {
        merger.map(listOf())
    }

    @Test
    fun testMap_singletonList() {
        val expectedRequestModel = requestModel("https://recommender.scarabresearch.com/merchants/merchantId555?cp=1&q1=1&q2=b")

        assertEquals(expectedRequestModel, merger.map(listOf(shard1)))
    }

    @Test
    fun testMap_multipleShards() {
        val expectedRequestModel = requestModel("https://recommender.scarabresearch.com/merchants/merchantId555?cp=1&q1=1&q2=b&q3=c")

        assertEquals(expectedRequestModel, merger.map(listOf(shard1, shard2)))
    }

    @Test
    fun testMap_withUrlEncoded_queryParams() {
        val expectedRequestModel = requestModel("https://recommender.scarabresearch.com/merchants/merchantId555?cp=1&%3C%3E%2C=%22%60%3B%2F%3F%3A%5E%25%23%40%26%3D%24%2B%7B%7D%3C%3E%2C%7C%20")

        assertEquals(expectedRequestModel, merger.map(listOf(shard3)))
    }

    @Test
    fun testMap_withVisitorIdPresent() {
        whenever(store.getString("predict_visitor_id")).thenReturn(VISITOR_ID)

        val expectedRequestModel = requestModel("https://recommender.scarabresearch.com/merchants/merchantId555?cp=1&vi=888999888&q3=c")

        assertEquals(expectedRequestModel, merger.map(listOf(shard2)))
    }

    @Test
    fun testMap_withCustomerIdPresent() {
        whenever(store.getString("predict_customer_id")).thenReturn(CUSTOMER_ID)

        val expectedRequestModel = requestModel("https://recommender.scarabresearch.com/merchants/merchantId555?cp=1&ci=12345&q3=c")

        assertEquals(expectedRequestModel, merger.map(listOf(shard2)))
    }

    @Test
    fun testMap_withBoth_visitorIdAndCustomerIdPresent() {
        whenever(store.getString("predict_visitor_id")).thenReturn(VISITOR_ID)
        whenever(store.getString("predict_customer_id")).thenReturn(CUSTOMER_ID)

        val expectedRequestModel = requestModel("https://recommender.scarabresearch.com/merchants/merchantId555?cp=1&vi=888999888&ci=12345&q3=c")

        assertEquals(expectedRequestModel, merger.map(listOf(shard2)))
    }

    private fun requestModel(url: String) = RequestModel(
            url,
            RequestMethod.GET,
            null,
            mapOf("User-Agent" to "EmarsysSDK|osversion:$OS_VERSION|platform:$PLATFORM"),
            TIMESTAMP,
            TTL,
            ID
    )

}