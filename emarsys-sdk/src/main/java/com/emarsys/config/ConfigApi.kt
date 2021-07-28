package com.emarsys.config

import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.result.CompletionListener

interface ConfigApi {

    val contactFieldId: Int?

    val applicationCode: String?

    val merchantId: String?

    val hardwareId: String

    val languageCode: String

    val notificationSettings: NotificationSettings

    val isAutomaticPushSendingEnabled: Boolean

    val sdkVersion: String

    fun changeApplicationCode(applicationCode: String?)

    fun changeApplicationCode(applicationCode: String?, completionListener: CompletionListener?)

    fun changeMerchantId(merchantId: String?)
}
