package com.emarsys.predict.storage

import com.emarsys.core.storage.StorageKey

enum class PredictStorageKey : StorageKey {
    PREDICT_SERVICE_URL;

    override fun getKey(): String {
        return "predict_" + name.toLowerCase()
    }
}
