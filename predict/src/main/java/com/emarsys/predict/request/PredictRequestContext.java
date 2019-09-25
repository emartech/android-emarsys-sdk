package com.emarsys.predict.request;

import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.storage.KeyValueStore;
import com.emarsys.core.util.Assert;

public class PredictRequestContext {
    private final String merchantId;
    private final DeviceInfo deviceInfo;
    private final TimestampProvider timestampProvider;
    private final UUIDProvider uuidProvider;
    private final KeyValueStore keyValueStore;

    public PredictRequestContext(String merchantId, DeviceInfo deviceInfo, TimestampProvider timestampProvider, UUIDProvider uuidProvider, KeyValueStore keyValueStore) {
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");
        Assert.notNull(timestampProvider, "TimeStampProvider must not be null!");
        Assert.notNull(uuidProvider, "UUIDProvider must not be null!");
        Assert.notNull(keyValueStore, "KeyValue must not be null!");

        this.merchantId = merchantId;
        this.deviceInfo = deviceInfo;
        this.timestampProvider = timestampProvider;
        this.uuidProvider = uuidProvider;
        this.keyValueStore = keyValueStore;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public TimestampProvider getTimestampProvider() {
        return timestampProvider;
    }

    public UUIDProvider getUuidProvider() {
        return uuidProvider;
    }

    public KeyValueStore getKeyValueStore() {
        return keyValueStore;
    }
}
