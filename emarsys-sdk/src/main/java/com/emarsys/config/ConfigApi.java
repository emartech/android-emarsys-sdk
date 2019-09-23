package com.emarsys.config;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.emarsys.core.api.result.CompletionListener;

public interface ConfigApi {

    @NonNull
    int getContactFieldId();

    void changeApplicationCode(@Nullable String applicationCode, @Nullable CompletionListener completionListener);

    @Nullable
    String getApplicationCode();

    void changeMerchantId(@Nullable String merchantId);
}
