package com.emarsys.predict.shard;

import com.emarsys.core.Mapper;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.storage.KeyValueStore;
import com.emarsys.core.util.Assert;
import com.emarsys.predict.DefaultPredictInternal;
import com.emarsys.predict.provider.PredictRequestModelBuilderProvider;
import com.emarsys.predict.request.PredictRequestContext;
import com.emarsys.predict.request.PredictRequestModelBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PredictShardListMerger implements Mapper<List<ShardModel>, RequestModel> {

    private final PredictRequestContext predictRequestContext;
    private final PredictRequestModelBuilder predictRequestModelBuilder;

    public PredictShardListMerger(PredictRequestContext predictRequestContext, PredictRequestModelBuilderProvider predictRequestModelBuilderProvider) {
        Assert.notNull(predictRequestContext, "PredictRequestContext must not be null!");
        Assert.notNull(predictRequestModelBuilderProvider, "PredictRequestModelBuilderProvider must not be null!");

        this.predictRequestContext = predictRequestContext;
        this.predictRequestModelBuilder = predictRequestModelBuilderProvider.providePredictRequestModelBuilder();
    }

    @Override
    public RequestModel map(List<ShardModel> shards) {
        Assert.notNull(shards, "Shards must not be null!");
        Assert.notEmpty(shards, "Shards must not be empty!");
        Assert.elementsNotNull(shards, "Shard elements must not be null!");
        Map<String, Object> shardData = mergeShardData(shards);

        return predictRequestModelBuilder.withShardData(shardData).build();
    }

    private Map<String, Object> mergeShardData(List<ShardModel> shards) {
        Map<String, Object> result = new LinkedHashMap<>();

        insertBaseParameters(result);

        for (ShardModel shard : shards) {
            result.putAll(shard.getData());
        }

        return result;
    }

    private void insertBaseParameters(Map<String, Object> result) {
        result.put("cp", 1);

        KeyValueStore keyValueStore = predictRequestContext.getKeyValueStore();

        String visitorId = keyValueStore.getString(DefaultPredictInternal.VISITOR_ID_KEY);
        if (visitorId != null) {
            result.put("vi", visitorId);
        }

        String contactId = keyValueStore.getString(DefaultPredictInternal.CONTACT_FIELD_VALUE_KEY);
        if (contactId != null) {
            result.put("ci", contactId);
        }
    }

}
