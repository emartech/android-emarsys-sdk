package com.emarsys.request

import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.request.RestClient
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler
import com.emarsys.mobileengage.util.RequestModelHelper
import com.emarsys.predict.request.PredictMultiIdRequestModelFactory

class CoreCompletionHandlerRefreshTokenProxy(
    private val coreCompletionHandler: CoreCompletionHandler,
    private val restClient: RestClient,
    private val contactTokenStorage: Storage<String?>,
    private val refreshTokenStorage: Storage<String?>,
    private val pushTokenStorage: Storage<String?>,
    private var tokenResponseHandler: MobileEngageTokenResponseHandler,
    private val requestModelHelper: RequestModelHelper,
    private val requestModelFactory: MobileEngageRequestModelFactory,
    private val predictMultiIdRequestModelFactory: PredictMultiIdRequestModelFactory
) : CoreCompletionHandler {
    private var originalResponseModel: ResponseModel? = null
    private var retryCount = 0

    override fun onSuccess(id: String, responseModel: ResponseModel) {
        if (retryCount >= 3) {
            val response = originalResponseModel
            reset()
            coreCompletionHandler.onError(id, response!!.copy(statusCode = 418))
        } else if (requestModelHelper.isMobileEngageRefreshContactTokenRequest(responseModel.requestModel)
            || requestModelHelper.isPredictMultiIdRefreshContactTokenRequest(responseModel.requestModel)
        ) {
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
        if (retryCount >= 3
            || requestModelHelper.isMobileEngageRefreshContactTokenRequest(responseModel.requestModel)
            || requestModelHelper.isPredictMultiIdRefreshContactTokenRequest(responseModel.requestModel)
        ) {
            val response = originalResponseModel
            reset()
            coreCompletionHandler.onError(id, response!!.copy(statusCode = 418))
        } else if (isPredictOrMobileEngageRequestUnauthorized(responseModel)) {
            refreshMobileEngageContactToken(responseModel)
        } else if (isPredictOnlyPredictRequestUnauthorized(responseModel)) {
            refreshPredictContactToken(id, responseModel)
        } else {
            callCompletionHandlerOnError(id, responseModel)
        }
    }

    private fun isPredictOnlyPredictRequestUnauthorized(responseModel: ResponseModel) =
        FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT)
                && !FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE)
                && isPredictRequestUnauthorized(responseModel)

    private fun isPredictOrMobileEngageRequestUnauthorized(responseModel: ResponseModel) =
        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE)
                && (isPredictRequestUnauthorized(responseModel) || isMobileEngageRequestUnauthorized(responseModel))

    private fun isPredictRequestUnauthorized(responseModel: ResponseModel) =
        (responseModel.statusCode == 401
                && requestModelHelper.isPredictRequest(responseModel.requestModel))

    private fun isMobileEngageRequestUnauthorized(responseModel: ResponseModel) =
        (responseModel.statusCode == 401
                && requestModelHelper.isMobileEngageRequest(responseModel.requestModel))

    private fun refreshMobileEngageContactToken(responseModel: ResponseModel) {
        pushTokenStorage.remove()
        originalResponseModel = responseModel
        val refreshTokenRequestModel = requestModelFactory.createRefreshContactTokenRequest()
        restClient.execute(refreshTokenRequestModel, this)
    }

    private fun refreshPredictContactToken(id: String, responseModel: ResponseModel) {
        refreshTokenStorage.get()?.let { refreshToken ->
            originalResponseModel = responseModel
            val refreshTokenRequestModel =
                predictMultiIdRequestModelFactory.createRefreshContactTokenRequestModel(
                    refreshToken
                )
            restClient.execute(refreshTokenRequestModel, this)
        } ?: run {
            callCompletionHandlerOnError(id, responseModel)
        }
    }

    private fun callCompletionHandlerOnError(id: String, responseModel: ResponseModel) {
        reset()
        coreCompletionHandler.onError(id, responseModel)
    }

    override fun onError(id: String, cause: Exception) {
        reset()
        coreCompletionHandler.onError(id, cause)
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