package com.emarsys.predict.storage

import com.emarsys.core.storage.StorageKey
import java.util.Locale

enum class PredictStorageKey : StorageKey {
    PREDICT_SERVICE_URL;

    override val key: String
        get() = "predict_" + name.lowercase(Locale.getDefault())
}
