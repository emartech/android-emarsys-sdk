package com.emarsys.mobileengage.geofence

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory

class DefaultGeofenceInternal(private val requestModelFactory: MobileEngageRequestModelFactory,
                              private val requestManager: RequestManager,
                              private val geofenceResponseMapper: GeofenceResponseMapper) : GeofenceInternal {

    override fun fetchGeofences() {
        val requestModel = requestModelFactory.createFetchGeofenceRequest()
        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String?, responseModel: ResponseModel?) {
                if (responseModel != null) {
                    geofenceResponseMapper.map(responseModel)
                }
            }

            override fun onError(id: String?, responseModel: ResponseModel?) {
            }

            override fun onError(id: String?, cause: Exception?) {
            }

        })
    }

    override fun enable(completionListener: CompletionListener?) {
    }
}