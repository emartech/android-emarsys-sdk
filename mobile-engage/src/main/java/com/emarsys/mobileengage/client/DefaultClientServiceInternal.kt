package com.emarsys.mobileengage.client

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.RequestManager
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory

class DefaultClientServiceInternal(
    private val requestManager: RequestManager,
    private val requestModelFactory: MobileEngageRequestModelFactory
) : ClientServiceInternal {
    override fun trackDeviceInfo(completionListener: CompletionListener?) {
        try {
            val requestModel = requestModelFactory.createTrackDeviceInfoRequest()
            requestManager.submit(requestModel, completionListener)
        } catch (e: IllegalArgumentException) {
            completionListener?.onCompleted(e)
        }
    }
}