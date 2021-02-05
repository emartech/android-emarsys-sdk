package com.emarsys.mobileengage.request

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.RequestModelUtils
import com.emarsys.mobileengage.RefreshTokenInternal
import com.emarsys.mobileengage.util.RequestModelUtils.isMobileEngageRequest

class CoreCompletionHandlerRefreshTokenProxy(private val coreCompletionHandler: CoreCompletionHandler,
                                             private val refreshTokenInternal: RefreshTokenInternal,
                                             private val restClient: RestClient,
                                             private val contactTokenStorage: Storage<String>,
                                             private val pushTokenStorage: Storage<String>) : CoreCompletionHandler {

    override fun onSuccess(id: String, responseModel: ResponseModel) {
        coreCompletionHandler.onSuccess(id, responseModel)
    }

    override fun onError(originalId: String, originalResponseModel: ResponseModel) {
        if (originalResponseModel.statusCode == 401
                && originalResponseModel.isMobileEngageRequest()) {
            pushTokenStorage.remove()
            refreshTokenInternal.refreshContactToken { errorCause ->
                if (errorCause == null) {
                    restClient.execute(originalResponseModel.requestModel, this)
                } else {
                    for (id in RequestModelUtils.extractIdsFromCompositeRequestModel(originalResponseModel.requestModel)) {
                        coreCompletionHandler.onError(id, Exception(errorCause))
                    }
                }
            }
        } else {
            coreCompletionHandler.onError(originalId, originalResponseModel)
        }
    }

    override fun onError(id: String, cause: Exception) {
        coreCompletionHandler.onError(id, cause)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoreCompletionHandlerRefreshTokenProxy

        if (coreCompletionHandler != other.coreCompletionHandler) return false
        if (refreshTokenInternal != other.refreshTokenInternal) return false
        if (restClient != other.restClient) return false
        if (contactTokenStorage != other.contactTokenStorage) return false
        if (pushTokenStorage != other.pushTokenStorage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = coreCompletionHandler.hashCode()
        result = 31 * result + refreshTokenInternal.hashCode()
        result = 31 * result + restClient.hashCode()
        result = 31 * result + contactTokenStorage.hashCode()
        result = 31 * result + pushTokenStorage.hashCode()
        return result
    }

}