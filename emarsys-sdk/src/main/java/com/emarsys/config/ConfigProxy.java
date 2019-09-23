package com.emarsys.config;

import com.emarsys.core.Callable;
import com.emarsys.core.RunnerProxy;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.util.Assert;

public class ConfigProxy implements ConfigApi {

    private final ConfigInternal configInternal;
    private final RunnerProxy runnerProxy;

    public ConfigProxy(RunnerProxy runnerProxy, ConfigInternal configInternal) {
        Assert.notNull(runnerProxy, "RunnerProxy must not be null!");
        Assert.notNull(configInternal, "ConfigInternal must not be null!");
        this.configInternal = configInternal;
        this.runnerProxy = runnerProxy;
    }

    @Override
    public int getContactFieldId() {
        return runnerProxy.logException(new Callable<Integer>() {
            @Override
            public Integer call() {
                return configInternal.getContactFieldId();
            }
        });
    }

    @Override
    public void changeApplicationCode(final String applicationCode, final CompletionListener completionListener) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                configInternal.changeApplicationCode(applicationCode, completionListener);
            }
        });
    }

    @Override
    public String getApplicationCode() {
        return runnerProxy.logException(new Callable<String>() {
            @Override
            public String call() {
                return configInternal.getApplicationCode();
            }
        });
    }

    @Override
    public void changeMerchantId(final String merchantId) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                configInternal.changeMerchantId(merchantId);
            }
        });
    }
}
