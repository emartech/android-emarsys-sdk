package com.emarsys.config;

import com.emarsys.core.api.experimental.FlipperFeature;
import com.emarsys.core.api.result.CompletionListener;

import java.util.List;

public interface ConfigInternal {

    void setContactFieldId(int contactFieldId);

    int getContactFieldId();

    void changeApplicationCode(String applicationCode, CompletionListener completionListener);

    String getApplicationCode();

    void changeMerchantId(String merchantId);

    String getMerchantId();

    List<FlipperFeature> getExperimentalFeatures();
}
