package com.emarsys.core.util.log;

import com.emarsys.core.Mapper;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogShardListMerger implements Mapper<List<ShardModel>, RequestModel> {

    private static final String LOG_URL = "https://ems-log-dealer.herokuapp.com/v1/log";

    private final TimestampProvider timestampProvider;
    private final UUIDProvider uuidProvider;

    public LogShardListMerger(TimestampProvider timestampProvider, UUIDProvider uuidProvider) {
        this.timestampProvider = timestampProvider;
        this.uuidProvider = uuidProvider;
    }

    @Override
    public RequestModel map(List<ShardModel> shards) {
        Assert.notNull(shards, "Shards must not be null!");
        Assert.notEmpty(shards, "Shards must not be empty!");
        Assert.elementsNotNull(shards, "Shard elements must not be null!");

        return new RequestModel.Builder(timestampProvider, uuidProvider)
                .url(LOG_URL)
                .method(RequestMethod.POST)
                .payload(createPayload(shards))
                .build();
    }

    private Map<String, Object> createPayload(List<ShardModel> shards) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> datas = new ArrayList<>(shards.size());

        for (ShardModel shard : shards) {
            Map<String, Object> data = new HashMap<>();
            data.put("type", shard.getType());
            data.putAll(shard.getData());
            datas.add(data);
        }

        result.put("logs", datas);
        return result;
    }

}
