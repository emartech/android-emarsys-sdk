package com.emarsys.mobileengage.storage;

public enum MobileEngageStorageKey implements StorageKey {
    REFRESH_TOKEN, CONTACT_TOKEN, CLIENT_STATE;

    @Override
    public String getKey() {
        return "mobile_engage_" + name().toLowerCase();
    }
}
