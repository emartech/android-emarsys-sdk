package com.emarsys.core.storage

import java.util.Locale

enum class CoreStorageKey : StorageKey {
    HARDWARE_ID,
    LOG_LEVEL;

    override val key: String
        get() = "core_" + name.lowercase(Locale.getDefault())
}
