package com.emarsys.core.util.batch;

import com.emarsys.core.Mapper;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.shard.specification.FilterByShardIds;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.predicate.Predicate;

import java.util.List;

public class BatchingShardTrigger implements Runnable {

    public enum RequestStrategy{
        PERSISTENT, TRANSIENT
    }

    private final Repository<ShardModel, SqlSpecification> repository;
    private final Predicate<List<ShardModel>> predicate;
    private final SqlSpecification querySpecification;
    private final Mapper<List<ShardModel>, List<List<ShardModel>>> chunker;
    private final Mapper<List<ShardModel>, RequestModel> merger;
    private final RequestManager requestManager;
    private final RequestStrategy requestStrategy;

    public BatchingShardTrigger(
            Repository<ShardModel, SqlSpecification> repository,
            Predicate<List<ShardModel>> predicate,
            SqlSpecification querySpecification,
            Mapper<List<ShardModel>, List<List<ShardModel>>> chunker,
            Mapper<List<ShardModel>, RequestModel> merger,
            RequestManager requestManager, RequestStrategy requestStrategy) {
        Assert.notNull(repository, "Repository must not be null!");
        Assert.notNull(predicate, "Predicate must not be null!");
        Assert.notNull(querySpecification, "QuerySpecification must not be null!");
        Assert.notNull(chunker, "Chunker must not be null!");
        Assert.notNull(merger, "Merger must not be null!");
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(requestStrategy, "RequestStrategy must not be null!");

        this.repository = repository;
        this.predicate = predicate;
        this.querySpecification = querySpecification;
        this.chunker = chunker;
        this.merger = merger;
        this.requestManager = requestManager;
        this.requestStrategy = requestStrategy;
    }

    @Override
    public void run() {
        List<ShardModel> shards = repository.query(querySpecification);
        if (predicate.evaluate(shards)) {
            List<List<ShardModel>> chunks = chunker.map(shards);

            for (List<ShardModel> chunk : chunks) {
                submit(merger.map(chunk));
                repository.remove(new FilterByShardIds(chunk));
            }
        }
    }

    private void submit(RequestModel requestModel) {
        if (requestStrategy == RequestStrategy.PERSISTENT) {
            requestManager.submit(requestModel, null);
        } else if (requestStrategy == RequestStrategy.TRANSIENT) {
            requestManager.submitNow(requestModel);
        }
    }
}
