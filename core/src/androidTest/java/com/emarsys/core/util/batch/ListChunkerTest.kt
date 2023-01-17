package com.emarsys.core.util.batch

import com.emarsys.core.shard.ShardModel
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class ListChunkerTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var chunker: ListChunker<Any>

    @Before
    fun init() {
        chunker = ListChunker(1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun constructor_chunkSize_mustBeGreaterThanZero() {
        ListChunker<Any>(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMap_shards_mustNotBeNull() {
        chunker.map(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMap_shards_mustNotBeEmpty() {
        chunker.map(listOf<Any>())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMap_shards_mustNotContainNullElements() {
        chunker.map(listOf(
                mock(ShardModel::class.java),
                null,
                mock(ShardModel::class.java)
        ))
    }

    @Test
    fun testMap_1_withSingleElementInput() {
        val shard = mock(ShardModel::class.java)
        val input = listOf(shard)

        val expected = listOf(listOf(shard))
        val result = chunker.map(input)

        assertEquals(expected, result)
    }

    @Test
    fun testMap_1_createsSingleElementLists() {
        val input = createShardModels(10)

        val expected = input.map { listOf(it) }
        val result = chunker.map(input)

        assertEquals(expected, result)
    }

    @Test
    fun testMap_largeChunkSize_withSingleElementInput() {
        chunker = ListChunker(Int.MAX_VALUE)

        val input = createShardModels(1)

        val expected = listOf(input)
        val result = chunker.map(input)

        assertEquals(expected, result)
    }

    @Test
    fun testMap_3_withMultipleElementInput() {
        chunker = ListChunker(3)

        val input = createShardModels(10)

        val expected = input.chunked(3)
        val result = chunker.map(input)

        assertEquals(expected, result)
    }

    private fun createShardModels(size: Int): List<Any> =
            (0 until size).map { mock(Any::class.java) }


}