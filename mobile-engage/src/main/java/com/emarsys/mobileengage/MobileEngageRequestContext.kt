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
    var openIdToken: String? = null,
    val deviceInfo: DeviceInfo,
    val timestampProvider: TimestampProvider,
    val uuidProvider: UUIDProvider,
    val clientStateStorage: Storage<String?>,
    val contactTokenStorage: Storage<String?>,
    val refreshTokenStorage: Storage<String?>,
    val pushTokenStorage: Storage<String?>,
    val contactFieldValueStorage: Storage<String?>,
    val sessionIdHolder: SessionIdHolder
) {

    var contactFieldValue: String?
        get() {
            return contactFieldValueStorage.get()
        }
        set(value) {
            contactFieldValueStorage.set(value)
        }

    fun hasContactIdentification(): Boolean {
        return openIdToken != null || contactFieldValue != null
    }

    fun reset() {
        clientStateStorage.remove()
        contactTokenStorage.remove()
        refreshTokenStorage.remove()
        contactFieldValueStorage.remove()
        pushTokenStorage.remove()
        sessionIdHolder.sessionId = null
        openIdToken = null
        applicationCode = null
    }
}