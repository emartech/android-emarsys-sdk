package com.emarsys.predict.request

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.storage.KeyValueStore

data class PredictRequestContext(var merchantId: String?,
                                 val deviceInfo: DeviceInfo,
                                 val timestampProvider: TimestampProvider,
                                 val uuidProvider: UUIDProvider,
                                 val keyValueStore: KeyValueStore)
