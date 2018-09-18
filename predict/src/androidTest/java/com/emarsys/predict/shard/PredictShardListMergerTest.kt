package com.emarsys.predict.shard

import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.KeyValueStore
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class PredictShardListMergerTest {

    companion object {
        const val ID = "id"
        const val TIMESTAMP = 125L
        const val TTL = Long.MAX_VALUE
        const val VISITOR_ID = "888999888"
        const val CUSTOMER_ID = "12345"
    }

    lateinit var merger: PredictShardListMerger

    lateinit var merchantId: String
    lateinit var store: KeyValueStore
    lateinit var timestampProvider: TimestampProvider
    lateinit var uuidProvider: UUIDProvider

    lateinit var shard1: ShardModel
    lateinit var shard2: ShardModel
    lateinit var shard3: ShardModel

    @Before
    fun init() {
        merchantId = "merchantId555"
        store = mock(KeyValueStore::class.java)
        timestampProvider = mock(TimestampProvider::class.java)
        uuidProvider = mock(UUIDProvider::class.java)

        merger = PredictShardListMerger(merchantId, store, timestampProvider, uuidProvider)
        `when`(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP)
        `when`(uuidProvider.provideId()).thenReturn(ID)

        shard1 = ShardModel("id1", "type1", mapOf("q1" to 1, "q2" to "b"), 100, 100)
        shard2 = ShardModel("id2", "type2", mapOf("q3" to "c"), 110, 100)
        shard3 = ShardModel("id3", "type3", mapOf("<>," to "\"`;/?:^%#@&=\$+{}<>,| "), 120, 100)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_merchantId_mustNotBeNull() {
        PredictShardListMerger(null, store, timestampProvider, uuidProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_keyValueStore_mustNotBeNull() {
        PredictShardListMerger(merchantId, null, timestampProvider, uuidProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_timestampProvider_mustNotBeNull() {
        PredictShardListMerger(merchantId,  store,null, uuidProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_uuidProvider_mustNotBeNull() {
        PredictShardListMerger(merchantId, store, timestampProvider, null)
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
        val expectedRequestModel = RequestModel(
                "https://recommender.scarabresearch.com/merchants/merchantId555?cp=1&q1=1&q2=b",
                RequestMethod.GET,
                null,
                mapOf(),
                TIMESTAMP,
                TTL,
                ID
        )
        assertEquals(expectedRequestModel, merger.map(listOf(shard1)))
    }

    @Test
    fun testMap_multipleShards() {
        val expectedRequestModel = RequestModel(
                "https://recommender.scarabresearch.com/merchants/merchantId555?cp=1&q1=1&q2=b&q3=c",
                RequestMethod.GET,
                null,
                mapOf(),
                TIMESTAMP,
                TTL,
                ID
        )
        assertEquals(expectedRequestModel, merger.map(listOf(shard1, shard2)))
    }

    @Test
    fun testMap_withUrlEncoded_queryParams() {
        val expectedRequestModel = RequestModel(
                "https://recommender.scarabresearch.com/merchants/merchantId555?cp=1&%3C%3E%2C=%22%60%3B%2F%3F%3A%5E%25%23%40%26%3D%24%2B%7B%7D%3C%3E%2C%7C%20",
                RequestMethod.GET,
                null,
                mapOf(),
                TIMESTAMP,
                TTL,
                ID
        )
        assertEquals(expectedRequestModel, merger.map(listOf(shard3)))
    }

    @Test
    fun testMap_withVisitorIdPresent() {
        `when`(store.getString("predict_visitor_id")).thenReturn(VISITOR_ID)

        val expectedRequestModel = RequestModel(
                "https://recommender.scarabresearch.com/merchants/merchantId555?cp=1&vi=888999888&q3=c",
                RequestMethod.GET,
                null,
                mapOf(),
                TIMESTAMP,
                TTL,
                ID
        )
        assertEquals(expectedRequestModel, merger.map(listOf(shard2)))
    }

    @Test
    fun testMap_withCustomerIdPresent() {
        `when`(store.getString("predict_customer_id")).thenReturn(CUSTOMER_ID)

        val expectedRequestModel = RequestModel(
                "https://recommender.scarabresearch.com/merchants/merchantId555?cp=1&ci=12345&q3=c",
                RequestMethod.GET,
                null,
                mapOf(),
                TIMESTAMP,
                TTL,
                ID
        )
        assertEquals(expectedRequestModel, merger.map(listOf(shard2)))
    }

    @Test
    fun testMap_withBoth_visitorIdAndCustomerIdPresent() {
        `when`(store.getString("predict_visitor_id")).thenReturn(VISITOR_ID)
        `when`(store.getString("predict_customer_id")).thenReturn(CUSTOMER_ID)

        val expectedRequestModel = RequestModel(
                "https://recommender.scarabresearch.com/merchants/merchantId555?cp=1&vi=888999888&ci=12345&q3=c",
                RequestMethod.GET,
                null,
                mapOf(),
                TIMESTAMP,
                TTL,
                ID
        )
        assertEquals(expectedRequestModel, merger.map(listOf(shard2)))
    }

}