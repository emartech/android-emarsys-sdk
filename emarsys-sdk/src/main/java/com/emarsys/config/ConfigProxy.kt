package com.emarsys.config

import com.emarsys.core.Callable
import com.emarsys.core.RunnerProxy
import com.emarsys.core.api.result.CompletionListener

class ConfigProxy(private val runnerProxy: RunnerProxy, private val configInternal: ConfigInternal) : ConfigApi {

    override val contactFieldId: Int
        get() = runnerProxy.logException(Callable { configInternal.contactFieldId })

    override val applicationCode: String?
        get() = runnerProxy.logException(Callable<String> { configInternal.applicationCode })

    override val merchantId: String?
        get() = runnerProxy.logException(Callable<String> { configInternal.merchantId })

    override fun changeApplicationCode(applicationCode: String?, completionListener: CompletionListener?) {
        runnerProxy.logException { configInternal.changeApplicationCode(applicationCode, completionListener) }
    }

    override fun changeMerchantId(merchantId: String?) {
        runnerProxy.logException { configInternal.changeMerchantId(merchantId) }
    }
}
