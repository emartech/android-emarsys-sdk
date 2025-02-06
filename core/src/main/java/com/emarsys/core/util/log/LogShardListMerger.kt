package com.emarsys.core.util.log;

import com.emarsys.core.Mapper;
import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.endpoint.Endpoint;
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


    private final TimestampProvider timestampProvider;
    private final UUIDProvider uuidProvider;
    private final DeviceInfo deviceInfo;
    private final String applicationCode;
    private final String merchantId;

    public LogShardListMerger(TimestampProvider timestampProvider, UUIDProvider uuidProvider, DeviceInfo deviceInfo, String applicationCode, String merchantId) {
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(uuidProvider, "UuidProvider must not be null!");
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");

        this.timestampProvider = timestampProvider;
        this.uuidProvider = uuidProvider;
        this.deviceInfo = deviceInfo;
        this.applicationCode = applicationCode;
        this.merchantId = merchantId;
    }

    @Override
    public RequestModel map(List<ShardModel> shards) {
        Assert.notNull(shards, "Shards must not be null!");
        Assert.notEmpty(shards, "Shards must not be empty!");
        Assert.elementsNotNull(shards, "Shard elements must not be null!");

        return new RequestModel.Builder(timestampProvider, uuidProvider)
                .url(Endpoint.LOG_URL)
                .method(RequestMethod.POST)
                .payload(createPayload(shards))
                .build();
    }

    private Map<String, Object> createPayload(List<ShardModel> shards) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> datas = new ArrayList<>(shards.size());
        Map<String, String> deviceInfo = createDeviceInfo();
        for (ShardModel shard : shards) {
            Map<String, Object> data = new HashMap<>();
            data.put("type", shard.getType());
            data.put("deviceInfo", deviceInfo);
            data.putAll(shard.getData());
            datas.add(data);
        }

        result.put("logs", datas);
        return result;
    }

    private Map<String, String> createDeviceInfo() {
        Map<String, String> data = new HashMap<>();
        data.put("platform", deviceInfo.getPlatform());
        data.put("appVersion", deviceInfo.getApplicationVersion());
        data.put("sdkVersion", deviceInfo.getSdkVersion());
        data.put("osVersion", deviceInfo.getOsVersion());
        data.put("model", deviceInfo.getModel());
        data.put("hwId", deviceInfo.getClientId());
        data.put("applicationCode", applicationCode);
        data.put("merchantId", merchantId);
        return data;
    }

}
