package com.emarsys.config;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.config.MobileEngageConfig;

public class EmarsysConfig {

    private final MobileEngageConfig mobileEngageConfig;


    public EmarsysConfig(MobileEngageConfig mobileEngageConfig) {
        Assert.notNull(mobileEngageConfig, "MobileEngageConfig must not be null!");
        this.mobileEngageConfig = mobileEngageConfig;
    }

    public MobileEngageConfig getMobileEngageConfig() {
        return mobileEngageConfig;
    }
}
