package com.emarsys.config

import com.emarsys.core.Callable
import com.emarsys.core.RunnerProxy
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.result.CompletionListener

class ConfigProxy(private val runnerProxy: RunnerProxy, private val configInternal: ConfigInternal) : ConfigApi {
    override val contactFieldId: Int
        get() = runnerProxy.logException(Callable { configInternal.contactFieldId })

    override val applicationCode: String?
        get() = runnerProxy.logException(Callable<String> { configInternal.applicationCode })

    override val merchantId: String?
        get() = runnerProxy.logException(Callable<String> { configInternal.merchantId })
    override val hardwareId: String
        get() = runnerProxy.logException(Callable<String> { configInternal.hardwareId })
    override val language: String
        get() = runnerProxy.logException(Callable<String> { configInternal.language })
    override val notificationSettings: NotificationSettings
        get() = runnerProxy.logException(Callable<NotificationSettings> { configInternal.notificationSettings })

    override fun changeApplicationCode(applicationCode: String?, completionListener: CompletionListener?) {
        runnerProxy.logException { configInternal.changeApplicationCode(applicationCode, completionListener) }
    }

    override fun changeApplicationCode(applicationCode: String?, completionListener: (throwable: Throwable?) -> Unit) {
        changeApplicationCode(applicationCode, CompletionListener { completionListener(it) })
    }

    override fun changeMerchantId(merchantId: String?) {
        runnerProxy.logException { configInternal.changeMerchantId(merchantId) }
    }
}
