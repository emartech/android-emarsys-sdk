package com.emarsys.mobileengage.storage;

import com.emarsys.core.storage.StorageKey;

public enum MobileEngageStorageKey implements StorageKey {
    REFRESH_TOKEN, CONTACT_TOKEN, CLIENT_STATE, CONTACT_FIELD_VALUE, APPLICATION_CODE;

    @Override
    public String getKey() {
        return "mobile_engage_" + name().toLowerCase();
    }
}
