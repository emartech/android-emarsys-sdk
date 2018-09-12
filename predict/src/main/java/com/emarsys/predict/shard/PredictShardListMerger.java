package com.emarsys.predict.shard;

import android.net.Uri;

import com.emarsys.core.Mapper;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.util.Assert;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PredictShardListMerger implements Mapper<List<ShardModel>, RequestModel> {

    private static final String PREDICT_BASE_URL = "https://recommender.scarabresearch.com/merchants";

    private final String merchantId;
    private final UUIDProvider uuidProvider;
    private final TimestampProvider timestampProvider;

    public PredictShardListMerger(String merchantId, TimestampProvider timestampProvider, UUIDProvider uuidProvider) {
        Assert.notNull(merchantId, "MerchantId must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(uuidProvider, "UuidProvider must not be null!");

        this.merchantId = merchantId;
        this.uuidProvider = uuidProvider;
        this.timestampProvider = timestampProvider;
    }

    @Override
    public RequestModel map(List<ShardModel> shards) {
        Assert.notNull(shards, "Shards must not be null!");
        Assert.notEmpty(shards, "Shards must not be empty!");
        Assert.elementsNotNull(shards, "Shard elements must not be null!");

        return new RequestModel.Builder(timestampProvider, uuidProvider)
                .url(createUrl(shards))
                .method(RequestMethod.GET)
                .ttl(getTtl(shards, timestampProvider))
                .build();
    }

    private String createUrl(List<ShardModel> shards) {
        Map<String, Object> shardData = mergeShardData(shards);

        Uri.Builder uriBuilder = Uri.parse(PREDICT_BASE_URL)
                .buildUpon()
                .appendPath(merchantId);

        for (String key : shardData.keySet()) {
            uriBuilder.appendQueryParameter(key, shardData.get(key).toString());
        }

        return uriBuilder.build().toString();
    }

    private Map<String, Object> mergeShardData(List<ShardModel> shards) {
        Map<String, Object> result = new LinkedHashMap<>();

        for(ShardModel shard : shards) {
            result.putAll(shard.getData());
        }

        return result;
    }

    private long getTtl(List<ShardModel> shards, TimestampProvider timestampProvider) {
        long now = timestampProvider.provideTimestamp();
        long ttl = Long.MAX_VALUE;

        for (ShardModel shard : shards) {
            long currentTtl = shard.getTtl() - (now - shard.getTimestamp());
            if (currentTtl < ttl) {
                ttl = currentTtl;
            }
        }

        return ttl;
    }
}
