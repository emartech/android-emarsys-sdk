package com.emarsys.clientservice

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.di.getDependency
import com.emarsys.mobileengage.client.ClientServiceInternal

class ClientService(private val loggingInstance: Boolean = false) : ClientServiceApi {
    override fun trackDeviceInfo(completionListener: CompletionListener?) {
        return (if (loggingInstance) getDependency("loggingInstance") else getDependency<ClientServiceInternal>("defaultInstance"))
            .trackDeviceInfo(completionListener)
    }
}