package com.emarsys.mobileengage;

import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;

public class RequestContext {
    private final String applicationCode;
    private final String applicationPassword;
    private final int contactFieldId;
    private final DeviceInfo deviceInfo;
    private final AppLoginStorage appLoginStorage;
    private final MeIdStorage meIdStorage;
    private final MeIdSignatureStorage meIdSignatureStorage;
    private final TimestampProvider timestampProvider;
    private final UUIDProvider uuidProvider;
    private final Storage<String> clientStateStorage;
    private final Storage<String> contactTokenStorage;
    private final Storage<String> refreshTokenStorage;
    private final Storage<String> contactFieldValueStorage;

    public RequestContext(
            String applicationCode,
            String applicationPassword,
            int contactFieldId,
            DeviceInfo deviceInfo,
            AppLoginStorage appLoginStorage,
            MeIdStorage meIdStorage,
            MeIdSignatureStorage meIdSignatureStorage,
            TimestampProvider timestampProvider,
            UUIDProvider uuidProvider,
            Storage<String> clientStateStorage,
            Storage<String> contactTokenStorage,
            Storage<String> refreshTokenStorage,
            Storage<String> contactFieldValueStorage) {
        Assert.notNull(applicationCode, "ApplicationCode must not be null!");
        Assert.notNull(applicationPassword, "ApplicationPassword must not be null!");
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");
        Assert.notNull(appLoginStorage, "AppLoginStorage must not be null!");
        Assert.notNull(meIdStorage, "MeIdStorage must not be null!");
        Assert.notNull(meIdSignatureStorage, "MeIdSignatureStorage must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(uuidProvider, "UUIDProvider must not be null!");
        Assert.notNull(clientStateStorage, "ClientStateStorage must not be null!");
        Assert.notNull(contactTokenStorage, "ContactTokenStorage must not be null!");
        Assert.notNull(refreshTokenStorage, "RefreshTokenStorage must not be null!");
        Assert.notNull(contactFieldValueStorage, "ContactFieldValueStorage must not be null!");

        this.applicationCode = applicationCode;
        this.applicationPassword = applicationPassword;
        this.contactFieldId = contactFieldId;
        this.deviceInfo = deviceInfo;
        this.appLoginStorage = appLoginStorage;
        this.meIdStorage = meIdStorage;
        this.meIdSignatureStorage = meIdSignatureStorage;
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

    public String getApplicationPassword() {
        return applicationPassword;
    }

    public int getContactFieldId() {
        return contactFieldId;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public AppLoginStorage getAppLoginStorage() {
        return appLoginStorage;
    }

    public MeIdStorage getMeIdStorage() {
        return meIdStorage;
    }

    public MeIdSignatureStorage getMeIdSignatureStorage() {
        return meIdSignatureStorage;
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
        return "RequestContext{" +
                "applicationCode='" + applicationCode + '\'' +
                ", applicationPassword='" + applicationPassword + '\'' +
                ", contactFieldId=" + contactFieldId +
                ", deviceInfo=" + deviceInfo +
                ", appLoginStorage=" + appLoginStorage +
                ", meIdStorage=" + meIdStorage +
                ", meIdSignatureStorage=" + meIdSignatureStorage +
                ", timestampProvider=" + timestampProvider +
                ", uuidProvider=" + uuidProvider +
                ", clientStateStorage=" + clientStateStorage +
                ", contactTokenStorage=" + contactTokenStorage +
                ", refreshTokenStorage=" + refreshTokenStorage +
                ", contactFieldValueStorage=" + contactFieldValueStorage +
                '}';
    }
}