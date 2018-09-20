package com.emarsys.predict.shard;

import android.net.Uri;

import com.emarsys.core.Mapper;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.storage.KeyValueStore;
import com.emarsys.core.util.Assert;
import com.emarsys.predict.PredictInternal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PredictShardListMerger implements Mapper<List<ShardModel>, RequestModel> {

    private final String merchantId;
    private final KeyValueStore keyValueStore;
    private final UUIDProvider uuidProvider;
    private final TimestampProvider timestampProvider;

    public PredictShardListMerger(
            String merchantId,
            KeyValueStore keyValueStore,
            TimestampProvider timestampProvider,
            UUIDProvider uuidProvider) {
        Assert.notNull(merchantId, "MerchantId must not be null!");
        Assert.notNull(keyValueStore, "KeyValueStore must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(uuidProvider, "UuidProvider must not be null!");

        this.merchantId = merchantId;
        this.keyValueStore = keyValueStore;
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
                .build();
    }

    private String createUrl(List<ShardModel> shards) {
        Map<String, Object> shardData = mergeShardData(shards);

        Uri.Builder uriBuilder = Uri.parse(PredictInternal.BASE_URL)
                .buildUpon()
                .appendPath(merchantId);

        for (String key : shardData.keySet()) {
            uriBuilder.appendQueryParameter(key, shardData.get(key).toString());
        }

        return uriBuilder.build().toString();
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

        String visitorId = keyValueStore.getString(PredictInternal.VISITOR_ID_KEY);
        if (visitorId != null) {
            result.put("vi", visitorId);
        }

        String customerId = keyValueStore.getString(PredictInternal.CUSTOMER_ID_KEY);
        if (customerId != null) {
            result.put("ci", customerId);
        }
    }
}
