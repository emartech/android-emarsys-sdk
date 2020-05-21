package com.emarsys.mobileengage

import com.emarsys.core.Mockable
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.storage.StringStorage

@Mockable
data class MobileEngageRequestContext(
        var applicationCode: String?,
        var contactFieldId: Int,
        val deviceInfo: DeviceInfo,
        val timestampProvider: TimestampProvider,
        val uuidProvider: UUIDProvider,
        val clientStateStorage: StringStorage,
        val contactTokenStorage: StringStorage,
        val refreshTokenStorage: StringStorage,
        val contactFieldValueStorage: StringStorage,
        val pushTokenStorage: StringStorage)