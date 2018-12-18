package com.emarsys.core.util.batch;

import com.emarsys.core.Mapper;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.shard.specification.FilterByShardIds;
import com.emarsys.core.util.Assert;

import java.util.List;

public class BatchingShardTrigger implements Runnable {
    private final Repository<ShardModel, SqlSpecification> repository;
    private final SqlSpecification querySpecification;
    private final Mapper<List<ShardModel>, List<List<ShardModel>>> chunker;
    private final Mapper<List<ShardModel>, RequestModel> merger;
    private final RequestManager requestManager;

    public BatchingShardTrigger(
            Repository<ShardModel, SqlSpecification> repository,
            SqlSpecification querySpecification,
            Mapper<List<ShardModel>, List<List<ShardModel>>> chunker,
            Mapper<List<ShardModel>, RequestModel> merger,
            RequestManager requestManager) {
        Assert.notNull(repository, "Repository must not be null!");
        Assert.notNull(querySpecification, "QuerySpecification must not be null!");
        Assert.notNull(chunker, "Chunker must not be null!");
        Assert.notNull(merger, "Merger must not be null!");
        Assert.notNull(requestManager, "RequestManager must not be null!");

        this.repository = repository;
        this.querySpecification = querySpecification;
        this.chunker = chunker;
        this.merger = merger;
        this.requestManager = requestManager;
    }

    @Override
    public void run() {
        List<ShardModel> shards = repository.query(querySpecification);
        if (!shards.isEmpty()) {
            List<List<ShardModel>> chunks = chunker.map(shards);

            for (List<ShardModel> chunk : chunks) {
                requestManager.submit(merger.map(chunk), null);
                repository.remove(new FilterByShardIds(chunk));
            }
        }
    }
}