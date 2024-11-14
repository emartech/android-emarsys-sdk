package com.emarsys.config

import com.emarsys.core.Mockable
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.di.emarsys

@Mockable
class Config : ConfigApi {
    override val contactFieldId: Int?
        get() = emarsys().configInternal.contactFieldId

    override val applicationCode: String?
        get() = emarsys().configInternal.applicationCode

    override val merchantId: String?
        get() = emarsys().configInternal.merchantId

    @Deprecated("Use clientId instead")
    override val hardwareId: String
        get() = emarsys().configInternal.clientId

    override val clientId: String
        get() = emarsys().configInternal.clientId

    override val languageCode: String
        get() = emarsys().configInternal.language

    override val notificationSettings: NotificationSettings
        get() = emarsys().configInternal.notificationSettings

    override val isAutomaticPushSendingEnabled: Boolean
        get() = emarsys().configInternal.isAutomaticPushSendingEnabled

    override val sdkVersion: String
        get() = emarsys().configInternal.sdkVersion

    override fun changeApplicationCode(applicationCode: String?) {
        emarsys().configInternal.changeApplicationCode(applicationCode, null)
    }

    override fun changeApplicationCode(
        applicationCode: String?,
        completionListener: CompletionListener?
    ) {
        emarsys().configInternal.changeApplicationCode(applicationCode, completionListener)
    }

    override fun changeMerchantId(merchantId: String?) {
        emarsys().configInternal.changeMerchantId(merchantId)
    }
}
