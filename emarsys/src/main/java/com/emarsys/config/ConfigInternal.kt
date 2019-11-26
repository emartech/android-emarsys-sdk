package com.emarsys.config

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.notification.NotificationSettings

interface ConfigInternal {

    val contactFieldId: Int

    val applicationCode: String?

    val merchantId: String?

    val hardwareId: String

    val language: String

    val notificationSettings: NotificationSettings

    fun changeApplicationCode(applicationCode: String?, completionListener: CompletionListener?)

    fun changeMerchantId(merchantId: String?)

    fun fetchRemoteConfig()

}
