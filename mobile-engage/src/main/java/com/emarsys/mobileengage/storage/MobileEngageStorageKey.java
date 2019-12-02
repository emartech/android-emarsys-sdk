package com.emarsys.mobileengage.storage;

import com.emarsys.core.storage.StorageKey;

public enum MobileEngageStorageKey implements StorageKey {
    REFRESH_TOKEN, CONTACT_TOKEN, CLIENT_STATE, CONTACT_FIELD_VALUE, PUSH_TOKEN, EVENT_SERVICE_URL, CLIENT_SERVICE_URL, INBOX_SERVICE_URL, DEEPLINK_SERVICE_URL, ME_V2_SERVICE_URL;

    @Override
    public String getKey() {
        return "mobile_engage_" + name().toLowerCase();
    }
}
