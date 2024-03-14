package com.emarsys.mobileengage.responsehandler

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.endpoint.Endpoint


class ClientInfoResponseHandler(private val deviceInfo: DeviceInfo,
                                private val deviceInfoPayloadStorage: Storage<String?>) : AbstractResponseHandler() {

    override fun shouldHandleResponse(responseModel: ResponseModel): Boolean {
        val url = responseModel.requestModel.url.toString()
        return url.startsWith(Endpoint.ME_CLIENT_HOST) && url.endsWith("/client")
    }

    override fun handleResponse(responseModel: ResponseModel) {
        deviceInfoPayloadStorage.set(deviceInfo.deviceInfoPayload)
    }
}
