package com.emarsys.mobileengage;

import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.Assert;

public class MobileEngageRequestContext {
    private final String applicationCode;
    private final int contactFieldId;
    private final DeviceInfo deviceInfo;
    private final TimestampProvider timestampProvider;
    private final UUIDProvider uuidProvider;
    private final Storage<String> clientStateStorage;
    private final Storage<String> contactTokenStorage;
    private final Storage<String> refreshTokenStorage;
    private final Storage<String> contactFieldValueStorage;

    public MobileEngageRequestContext(
            String applicationCode,
            int contactFieldId,
            DeviceInfo deviceInfo,
            TimestampProvider timestampProvider,
            UUIDProvider uuidProvider,
            Storage<String> clientStateStorage,
            Storage<String> contactTokenStorage,
            Storage<String> refreshTokenStorage,
            Storage<String> contactFieldValueStorage) {
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(uuidProvider, "UUIDProvider must not be null!");
        Assert.notNull(clientStateStorage, "ClientStateStorage must not be null!");
        Assert.notNull(contactTokenStorage, "ContactTokenStorage must not be null!");
        Assert.notNull(refreshTokenStorage, "RefreshTokenStorage must not be null!");
        Assert.notNull(contactFieldValueStorage, "ContactFieldValueStorage must not be null!");

        this.applicationCode = applicationCode;
        this.contactFieldId = contactFieldId;
        this.deviceInfo = deviceInfo;
        this.timestampProvider = timestampProvider;
        this.uuidProvider = uuidProvider;
        this.clientStateStorage = clientStateStorage;
        this.contactTokenStorage = contactTokenStorage;
        this.refreshTokenStorage = refreshTokenStorage;
        this.contactFieldValueStorage = contactFieldValueStorage;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public int getContactFieldId() {
        return contactFieldId;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public TimestampProvider getTimestampProvider() {
        return timestampProvider;
    }

    public UUIDProvider getUUIDProvider() {
        return uuidProvider;
    }

    public Storage<String> getClientStateStorage() {
        return clientStateStorage;
    }

    public Storage<String> getContactTokenStorage() {
        return contactTokenStorage;
    }

    public Storage<String> getRefreshTokenStorage() {
        return refreshTokenStorage;
    }

    public Storage<String> getContactFieldValueStorage() {
        return contactFieldValueStorage;
    }

    @Override
    public String toString() {
        return "MobileEngageRequestContext{" +
                "applicationCode='" + applicationCode + '\'' +
                ", contactFieldId=" + contactFieldId +
                ", deviceInfo=" + deviceInfo +
                ", timestampProvider=" + timestampProvider +
                ", uuidProvider=" + uuidProvider +
                ", clientStateStorage=" + clientStateStorage +
                ", contactTokenStorage=" + contactTokenStorage +
                ", refreshTokenStorage=" + refreshTokenStorage +
                ", contactFieldValueStorage=" + contactFieldValueStorage +
                '}';
    }
}