package com.emarsys.mobileengage;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestIdProvider;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

public class RequestContext {
    private final MobileEngageConfig config;
    private final DeviceInfo deviceInfo;
    private final AppLoginStorage appLoginStorage;
    private final MeIdStorage meIdStorage;
    private final MeIdSignatureStorage meIdSignatureStorage;
    private final TimestampProvider timestampProvider;
    private final RequestIdProvider requestIdProvider;
    private AppLoginParameters appLoginParameters;

    public RequestContext(
            MobileEngageConfig config,
            DeviceInfo deviceInfo,
            AppLoginStorage appLoginStorage,
            MeIdStorage meIdStorage,
            MeIdSignatureStorage meIdSignatureStorage,
            TimestampProvider timestampProvider,
            RequestIdProvider requestIdProvider) {
        Assert.notNull(config, "Config must not be null!");
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");
        Assert.notNull(appLoginStorage, "AppLoginStorage must not be null!");
        Assert.notNull(meIdStorage, "MeIdStorage must not be null!");
        Assert.notNull(meIdSignatureStorage, "MeIdSignatureStorage must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(requestIdProvider, "RequestIdProvider must not be null!");
        this.config = config;
        this.deviceInfo = deviceInfo;
        this.appLoginStorage = appLoginStorage;
        this.meIdStorage = meIdStorage;
        this.meIdSignatureStorage = meIdSignatureStorage;
        this.timestampProvider = timestampProvider;
        this.requestIdProvider = requestIdProvider;
    }

    public String getApplicationCode() {
        return config.getApplicationCode();
    }

    public MobileEngageConfig getConfig() {
        return config;
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

    public RequestIdProvider getRequestIdProvider() {
        return requestIdProvider;
    }

    public AppLoginParameters getAppLoginParameters() {
        return appLoginParameters;
    }

    public void setAppLoginParameters(AppLoginParameters appLoginParameters) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Setting appLoginParameters: %s", appLoginParameters);
        this.appLoginParameters = appLoginParameters;
    }
}