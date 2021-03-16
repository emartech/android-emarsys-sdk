package com.emarsys.clientservice

import com.emarsys.core.api.result.CompletionListener

interface ClientServiceApi {
    fun trackDeviceInfo(completionListener: CompletionListener?)
}