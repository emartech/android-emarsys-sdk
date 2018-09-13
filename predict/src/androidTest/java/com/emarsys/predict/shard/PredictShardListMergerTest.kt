package com.emarsys.predict.shard

import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class PredictShardListMergerTest {

    companion object {
        const val ID = "id"
        const val TIMESTAMP = 125L
        const val TTL = Long.MAX_VALUE
    }

    lateinit var merger: PredictShardListMerger

    lateinit var merchantId: String
    lateinit var timestampProvider: TimestampProvider
    lateinit var uuidProvider: UUIDProvider

    lateinit var shard1: ShardModel
    lateinit var shard2: ShardModel
    lateinit var shard3: ShardModel

    @Before
    fun init() {
        merchantId = "merchantId555"
        timestampProvider = mock(TimestampProvider::class.java)
        uuidProvider = mock(UUIDProvider::class.java)

        merger = PredictShardListMerger(merchantId, timestampProvider, uuidProvider)
        `when`(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP)
        `when`(uuidProvider.provideId()).thenReturn(ID)

        shard1 = ShardModel("id1", "type1", mapOf("q1" to 1, "q2" to "b"), 100, 100)
        shard2 = ShardModel("id2", "type2", mapOf("q3" to "c"), 110, 100)
        shard3 = ShardModel("id3", "type3", mapOf("q4" to "a b c", "q5" to "i:id"), 120, 100)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_merchantId_mustNotBeNull() {
        PredictShardListMerger(null, timestampProvider, uuidProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_timestampProvider_mustNotBeNull() {
        PredictShardListMerger(merchantId, null, uuidProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_uuidProvider_mustNotBeNull() {
        PredictShardListMerger(merchantId, timestampProvider, null)
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
                "https://recommender.scarabresearch.com/merchants/merchantId555?cp=1&q4=a%20b%20c&q5=i%3Aid",
                RequestMethod.GET,
                null,
                mapOf(),
                TIMESTAMP,
                TTL,
                ID
        )
        assertEquals(expectedRequestModel, merger.map(listOf(shard3)))
    }

}