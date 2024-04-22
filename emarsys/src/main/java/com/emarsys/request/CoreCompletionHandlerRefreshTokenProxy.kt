package com.emarsys.request

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler
import com.emarsys.mobileengage.util.RequestModelHelper

class CoreCompletionHandlerRefreshTokenProxy(
    private val coreCompletionHandler: CoreCompletionHandler,
    private val restClient: RestClient,
    private val contactTokenStorage: Storage<String?>,
    private val pushTokenStorage: Storage<String?>,
    private var tokenResponseHandler: MobileEngageTokenResponseHandler,
    private val requestModelHelper: RequestModelHelper,
    private val requestModelFactory: MobileEngageRequestModelFactory
) : CoreCompletionHandler {
    private var originalResponseModel: ResponseModel? = null
    private var retryCount = 0

    override fun onSuccess(id: String, responseModel: ResponseModel) {
        if (retryCount >= 3) {
            val response = originalResponseModel
            reset()
            coreCompletionHandler.onError(id, response!!.copy(statusCode = 418))
        } else if (isRefreshContactTokenRequest(responseModel.requestModel)) {
            tokenResponseHandler.processResponse(responseModel)
            Thread.sleep(500)
            retryCount += 1
            restClient.execute(originalResponseModel!!.requestModel, this)
        } else {
            reset()
            coreCompletionHandler.onSuccess(id, responseModel)
        }
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun onError(id: String, responseModel: ResponseModel) {
        if (retryCount >= 3 || isRefreshContactTokenRequest(responseModel.requestModel)) {
            val response = originalResponseModel
            reset()
            coreCompletionHandler.onError(id, response!!.copy(statusCode = 418))
        } else if (responseModel.statusCode == 401
            && requestModelHelper.isMobileEngageRequest(responseModel.requestModel)
        ) {
            pushTokenStorage.remove()
            originalResponseModel = responseModel
            val refreshTokenRequestModel = requestModelFactory.createRefreshContactTokenRequest()
            restClient.execute(refreshTokenRequestModel, this)
        } else {
            reset()
            coreCompletionHandler.onError(id, responseModel)
        }
    }

    override fun onError(id: String, cause: Exception) {
        reset()
        coreCompletionHandler.onError(id, cause)
    }

    private fun isRefreshContactTokenRequest(requestModel: RequestModel): Boolean {
        return requestModel.url.path.endsWith("contact-token")
    }

    private fun reset() {
        retryCount = 0
        originalResponseModel = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoreCompletionHandlerRefreshTokenProxy

        if (coreCompletionHandler != other.coreCompletionHandler) return false
        if (restClient != other.restClient) return false
        if (contactTokenStorage != other.contactTokenStorage) return false
        return pushTokenStorage == other.pushTokenStorage
    }

    override fun hashCode(): Int {
        var result = coreCompletionHandler.hashCode()
        result = 31 * result + restClient.hashCode()
        result = 31 * result + contactTokenStorage.hashCode()
        result = 31 * result + pushTokenStorage.hashCode()
        return result
    }

}