package com.emarsys.config;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.emarsys.core.api.experimental.FlipperFeature;
import com.emarsys.core.api.result.CompletionListener;

import java.util.List;

public interface ConfigApi {

    void setContactFieldId(@NonNull int contactFieldId);

    @NonNull
    int getContactFieldId();

    void changeApplicationCode(@Nullable String applicationCode, @Nullable CompletionListener completionListener);

    @Nullable
    String getApplicationCode();

    void changeMerchantId(@Nullable String merchantId);

    @Nullable
    String getMerchantId();

    @NonNull
    List<FlipperFeature> getExperimentalFeatures();
}
