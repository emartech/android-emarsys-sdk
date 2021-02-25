package com.emarsys.mobileengage

import com.emarsys.core.Mockable
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.session.SessionIdHolder

@Mockable
data class MobileEngageRequestContext(
        var applicationCode: String?,
        var contactFieldId: Int,
        var openIdToken: String? = null,
        val deviceInfo: DeviceInfo,
        val timestampProvider: TimestampProvider,
        val uuidProvider: UUIDProvider,
        val clientStateStorage: StringStorage,
        val contactTokenStorage: StringStorage,
        val refreshTokenStorage: StringStorage,
        val contactFieldValueStorage: StringStorage,
        val pushTokenStorage: StringStorage,
        val sessionIdHolder: SessionIdHolder) {

    fun hasContactIdentification(): Boolean {
        return openIdToken != null || contactFieldValueStorage.get() != null
    }
}