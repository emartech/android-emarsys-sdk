package com.emarsys.core.storage

import java.util.*

enum class CoreStorageKey : StorageKey {
    HARDWARE_ID,
    LOG_LEVEL;

    override fun getKey(): String {
        return "core_" + name.lowercase(Locale.getDefault())
    }
}
