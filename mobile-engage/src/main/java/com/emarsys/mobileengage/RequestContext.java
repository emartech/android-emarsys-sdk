package com.emarsys.mobileengage;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
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
    private AppLoginParameters appLoginParameters;

    public RequestContext(
            String applicationCode,
            String applicationPassword,
            int contactFieldId,
            DeviceInfo deviceInfo,
            AppLoginStorage appLoginStorage,
            MeIdStorage meIdStorage,
            MeIdSignatureStorage meIdSignatureStorage,
            TimestampProvider timestampProvider,
            UUIDProvider uuidProvider) {
        Assert.notNull(applicationCode, "ApplicationCode must not be null!");
        Assert.notNull(applicationPassword, "ApplicationPassword must not be null!");
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");
        Assert.notNull(appLoginStorage, "AppLoginStorage must not be null!");
        Assert.notNull(meIdStorage, "MeIdStorage must not be null!");
        Assert.notNull(meIdSignatureStorage, "MeIdSignatureStorage must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(uuidProvider, "UUIDProvider must not be null!");
        this.applicationCode = applicationCode;
        this.applicationPassword = applicationPassword;
        this.contactFieldId = contactFieldId;
        this.deviceInfo = deviceInfo;
        this.appLoginStorage = appLoginStorage;
        this.meIdStorage = meIdStorage;
        this.meIdSignatureStorage = meIdSignatureStorage;
        this.timestampProvider = timestampProvider;
        this.uuidProvider = uuidProvider;
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

    public AppLoginParameters getAppLoginParameters() {
        return appLoginParameters;
    }

    public void setAppLoginParameters(AppLoginParameters appLoginParameters) {
        this.appLoginParameters = appLoginParameters;
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
                ", appLoginParameters=" + appLoginParameters +
                '}';
    }
}