package com.emarsys.predict.shard;

import com.emarsys.core.Mapper;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.shard.specification.FilterByShardType;
import com.emarsys.core.util.Assert;

import java.util.List;

public class PredictShardTrigger implements Runnable {

    private final Repository<ShardModel, SqlSpecification> repository;
    private final Mapper<List<ShardModel>, List<List<ShardModel>>> chunker;
    private final Mapper<List<ShardModel>, RequestModel> merger;
    private final RequestManager requestManager;

    public PredictShardTrigger(
            Repository<ShardModel, SqlSpecification> repository,
            Mapper<List<ShardModel>, List<List<ShardModel>>> chunker,
            Mapper<List<ShardModel>, RequestModel> merger,
            RequestManager requestManager) {
        Assert.notNull(repository, "Repository must not be null!");
        Assert.notNull(chunker, "Chunker must not be null!");
        Assert.notNull(merger, "Merger must not be null!");
        Assert.notNull(requestManager, "RequestManager must not be null!");

        this.repository = repository;
        this.chunker = chunker;
        this.merger = merger;
        this.requestManager = requestManager;
    }

    @Override
    public void run() {
        List<ShardModel> shards = repository.query(new FilterByShardType("predict_%"));
        List<List<ShardModel>> chunks = chunker.map(shards);

        for (List<ShardModel> chunk : chunks) {
            requestManager.submit(merger.map(chunk));
        }
    }

}
