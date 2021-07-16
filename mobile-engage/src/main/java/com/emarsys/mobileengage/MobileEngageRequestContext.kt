package com.emarsys.mobileengage

import com.emarsys.core.Mockable
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.session.SessionIdHolder

@Mockable
data class MobileEngageRequestContext(
    var applicationCode: String?,
    var contactFieldId: Int?,
    var contactFieldValue: String?,
    var openIdToken: String? = null,
    val deviceInfo: DeviceInfo,
    val timestampProvider: TimestampProvider,
    val uuidProvider: UUIDProvider,
    val clientStateStorage: Storage<String?>,
    val contactTokenStorage: Storage<String?>,
    val refreshTokenStorage: Storage<String?>,
    val pushTokenStorage: Storage<String?>,
    val sessionIdHolder: SessionIdHolder
) {

    fun hasContactIdentification(): Boolean {
        return openIdToken != null || contactFieldValue != null
    }
}