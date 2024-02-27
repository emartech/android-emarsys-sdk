package com.emarsys.core.util.batch

import com.emarsys.core.shard.ShardModel
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.mock

class ListChunkerTest : AnnotationSpec() {


    private lateinit var chunker: ListChunker<Any>

    @Before
    fun init() {
        chunker = ListChunker(1)
    }

    @Test
    fun constructor_chunkSize_mustBeGreaterThanZero() {
        shouldThrow<IllegalArgumentException> {
            ListChunker<Any>(0)
        }
    }

    @Test
    fun testMap_shards_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            chunker.map(null)
        }
    }

    @Test
    fun testMap_shards_mustNotBeEmpty() {
        shouldThrow<IllegalArgumentException> {
            chunker.map(listOf<Any>())
        }
    }

    @Test
    fun testMap_shards_mustNotContainNullElements() {
        shouldThrow<IllegalArgumentException> {
            chunker.map(
                listOf(
                    mock(ShardModel::class.java),
                    null,
                    mock(ShardModel::class.java)
                )
            )
        }
    }

    @Test
    fun testMap_1_withSingleElementInput() {
        val shard = mock(ShardModel::class.java)
        val input = listOf(shard)

        val expected = listOf(listOf(shard))
        val result = chunker.map(input)

        result shouldBe expected
    }

    @Test
    fun testMap_1_createsSingleElementLists() {
        val input = createShardModels(10)

        val expected = input.map { listOf(it) }
        val result = chunker.map(input)

        result shouldBe expected
    }

    @Test
    fun testMap_largeChunkSize_withSingleElementInput() {
        chunker = ListChunker(Int.MAX_VALUE)

        val input = createShardModels(1)

        val expected = listOf(input)
        val result = chunker.map(input)

        result shouldBe expected
    }

    @Test
    fun testMap_3_withMultipleElementInput() {
        chunker = ListChunker(3)

        val input = createShardModels(10)

        val expected = input.chunked(3)
        val result = chunker.map(input)

        result shouldBe expected
    }

    private fun createShardModels(size: Int): List<Any> =
        (0 until size).map { mock(Any::class.java) }


}