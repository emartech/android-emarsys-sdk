package com.emarsys.mobileengage.client

import com.emarsys.core.api.result.CompletionListener

interface ClientServiceInternal {
    fun trackDeviceInfo(completionListener: CompletionListener?)
}