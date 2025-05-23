package com.emarsys.config

import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.result.CompletionListener

interface ConfigInternal {

    val contactFieldId: Int?

    val applicationCode: String?

    val merchantId: String?

    val clientId: String

    val language: String

    val notificationSettings: NotificationSettings

    val isAutomaticPushSendingEnabled: Boolean

    val sdkVersion: String

    fun changeApplicationCode(applicationCode: String?, completionListener: CompletionListener?)

    fun changeMerchantId(merchantId: String?)

    fun refreshRemoteConfig(completionListener: CompletionListener?)

    fun resetRemoteConfig()
}
