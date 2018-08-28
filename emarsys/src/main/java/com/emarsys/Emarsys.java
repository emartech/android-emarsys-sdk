package com.emarsys;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.emarsys.config.EmarsysConfig;
import com.emarsys.core.util.Assert;
import com.emarsys.di.DefaultDependencyContainer;
import com.emarsys.di.DependencyContainer;
import com.emarsys.di.DependencyInjection;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.predict.PredictInternal;

import java.util.Map;

class Emarsys {

    private static MobileEngageInternal mobileEngageInternal;
    private static PredictInternal predictInternal;
    private static DependencyContainer container;

    public static void setup(@NonNull EmarsysConfig config) {
        Assert.notNull(config, "Config must not be null!");

        DependencyInjection.setup(new DefaultDependencyContainer(config.getMobileEngageConfig()));
        container = DependencyInjection.getContainer();
        initializeFields();
    }

    public static void setCustomer(@NonNull String customerId) {
        Assert.notNull(customerId, "CustomerId must not be null!");
        mobileEngageInternal.appLogin(3, customerId);
        predictInternal.setCustomer(customerId);
    }

    public static void trackCustomEvent(@NonNull String eventName, @Nullable Map<String, String> eventAttributes) {
        Assert.notNull(eventName, "EventName must not be null!");
    }

    private static void initializeFields() {
        mobileEngageInternal = container.getMobileEngageInternal();
        predictInternal = container.getPredictInternal();
    }

}