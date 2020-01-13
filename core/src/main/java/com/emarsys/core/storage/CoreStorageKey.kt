package com.emarsys.core.storage

import java.util.*

enum class CoreStorageKey : StorageKey {
    HARDWARE_ID;

    override fun getKey(): String {
        return "core_" + name.toLowerCase(Locale.getDefault())
    }
}
