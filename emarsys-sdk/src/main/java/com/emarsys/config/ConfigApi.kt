package com.emarsys.config

import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.result.CompletionListener

interface ConfigApi {

    val contactFieldId: Int

    val applicationCode: String?

    val merchantId: String?

    val hardwareId: String

    val language: String

    val notificationSettings: NotificationSettings

    val isAutomaticPushSendingEnabled: Boolean

    val sdkVersion: String

    fun changeApplicationCode(applicationCode: String?)

    fun changeApplicationCode(applicationCode: String?, completionListener: CompletionListener?)

    fun changeApplicationCode(applicationCode: String?, completionListener: (throwable: Throwable?) -> Unit)

    fun changeApplicationCode(applicationCode: String?, contactFieldId: Int)

    fun changeApplicationCode(applicationCode: String?, contactFieldId: Int, completionListener: CompletionListener?)

    fun changeApplicationCode(applicationCode: String?, contactFieldId: Int, completionListener: (throwable: Throwable?) -> Unit)

    fun changeMerchantId(merchantId: String?)
}
