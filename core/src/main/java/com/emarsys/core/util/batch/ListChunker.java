package com.emarsys.core.util.batch;

import com.emarsys.core.Mapper;
import com.emarsys.core.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class ListChunker<T> implements Mapper<List<T>, List<List<T>>> {

    private final int chunkSize;

    public ListChunker(int chunkSize) {
        if (chunkSize < 1) {
            throw new IllegalArgumentException("Chunk size must be greater than 0!");
        }
        this.chunkSize = chunkSize;
    }

    @Override
    public List<List<T>> map(List<T> shards) {
        Assert.notNull(shards, "Shards must not be null!");
        Assert.notEmpty(shards, "Shards must not be empty!");
        Assert.elementsNotNull(shards, "Shard elements must not be null!");

        List<List<T>> result = new ArrayList<>();
        int length = shards.size();

        for (int chunkStartIndex = 0; chunkStartIndex < length; chunkStartIndex += chunkSize) {
            int chunkLength = chunkStartIndex + chunkSize < length ? chunkSize : length - chunkStartIndex;
            result.add(shards.subList(chunkStartIndex, chunkStartIndex + chunkLength));
        }

        return result;
    }

}
