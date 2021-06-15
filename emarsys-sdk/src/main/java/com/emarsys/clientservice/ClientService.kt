package com.emarsys.clientservice

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.di.mobileEngage

class ClientService(private val loggingInstance: Boolean = false) : ClientServiceApi {
    override fun trackDeviceInfo(completionListener: CompletionListener?) {
        return (if (loggingInstance) mobileEngage().loggingClientServiceInternal else mobileEngage().clientServiceInternal)
            .trackDeviceInfo(completionListener)
    }
}