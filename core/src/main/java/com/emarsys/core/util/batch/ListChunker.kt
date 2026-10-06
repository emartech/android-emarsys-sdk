package com.emarsys.core.util.batch

import com.emarsys.core.Mapper
import com.emarsys.core.util.Assert

class ListChunker<T>(chunkSize: Int) : Mapper<List<T>, List<List<T>>> {

    private val chunkSize: Int

    init {
        Assert.positiveInt(chunkSize, "Chunk size must be greater than 0!")
        this.chunkSize = chunkSize
    }

    override fun map(shards: List<T>): List<List<T>> {
        Assert.notEmpty(shards, "Shards must not be empty!")

        val result = mutableListOf<List<T>>()
        val length = shards.size

        var chunkStartIndex = 0
        while (chunkStartIndex < length) {
            val chunkLength = if (chunkStartIndex + chunkSize < length) chunkSize else length - chunkStartIndex
            result.add(shards.subList(chunkStartIndex, chunkStartIndex + chunkLength))
            chunkStartIndex += chunkSize
        }

        return result
    }
}
