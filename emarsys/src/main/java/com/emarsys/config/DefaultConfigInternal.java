package com.emarsys.config;

import com.emarsys.core.api.experimental.FlipperFeature;
import com.emarsys.core.api.result.CompletionListener;

import java.util.List;

public class DefaultConfigInternal implements ConfigInternal {

    @Override
    public void setContactFieldId(int contactFieldId) {

    }

    @Override
    public int getContactFieldId() {
        return 0;
    }

    @Override
    public void changeApplicationCode(String applicationCode, CompletionListener completionListener) {

    }

    @Override
    public String getApplicationCode() {
        return null;
    }

    @Override
    public void changeMerchantId(String merchantId) {

    }

    @Override
    public String getMerchantId() {
        return null;
    }

    @Override
    public List<FlipperFeature> getExperimentalFeatures() {
        return null;
    }
}
