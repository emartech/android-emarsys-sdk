package com.emarsys.predict.shard;

import com.emarsys.core.Mapper;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class PredictShardListChunker implements Mapper<List<ShardModel>, List<List<ShardModel>>> {

    @Override
    public List<List<ShardModel>> map(List<ShardModel> shards) {
        Assert.notNull(shards, "Shards must not be null!");
        Assert.notEmpty(shards, "Shards must not be empty!");
        Assert.elementsNotNull(shards, "Shard elements must not be null!");

        List<List<ShardModel>> result = new ArrayList<>();

        for (ShardModel shard : shards) {
            List<ShardModel> chunk = new ArrayList<>();
            chunk.add(shard);
            result.add(chunk);
        }

        return result;
    }

}
