package com.emarsys.mobileengage.storage

import com.emarsys.core.storage.StorageKey
import java.util.Locale

enum class MobileEngageStorageKey : StorageKey {
    REFRESH_TOKEN,
    CONTACT_TOKEN,
    CLIENT_STATE,
    CONTACT_FIELD_VALUE,
    PUSH_TOKEN,
    LOCAL_PUSH_TOKEN,
    EVENT_SERVICE_URL,
    CLIENT_SERVICE_URL,
    MESSAGE_INBOX_SERVICE_URL,
    DEEPLINK_SERVICE_URL,
    GEOFENCE_ENABLED,
    DEVICE_EVENT_STATE,
    DEVICE_INFO_HASH,
    GEOFENCE_INITIAL_ENTER_TRIGGER;

    override fun getKey(): String {
        return "mobile_engage_" + name.lowercase(Locale.getDefault())
    }
}