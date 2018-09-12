package com.emarsys.predict.shard

import com.emarsys.core.shard.ShardModel
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

class PredictShardListChunkerTest {

    lateinit var chunker: PredictShardListChunker

    @Before
    fun init() {
        chunker = PredictShardListChunker()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMap_shards_mustNotBeNull() {
        chunker.map(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMap_shards_mustNotBeEmpty() {
        chunker.map(listOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMap_shards_mustNotContainNullElements() {
        chunker.map(listOf(
                Mockito.mock(ShardModel::class.java),
                null,
                Mockito.mock(ShardModel::class.java)
        ))
    }

    @Test
    fun testMap_withSingleElementInput() {
        val shard = mock(ShardModel::class.java)
        val input = listOf(shard)

        val expected = listOf(listOf(shard))
        val result = chunker.map(input)

        assertEquals(expected, result)
    }

    @Test
    fun testMap_createsSingleElementLists() {
        val size = 10
        val input = (0 until size).map { mock(ShardModel::class.java) }

        val expected = input.map { listOf(it) }
        val result = chunker.map(input)

        assertEquals(expected, result)
    }

}