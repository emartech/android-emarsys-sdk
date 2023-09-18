package com.emarsys.mobileengage

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.RestClient
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler

class MobileEngageRefreshTokenInternal(
    private var tokenResponseHandler: MobileEngageTokenResponseHandler,
    private val restClient: RestClient,
    private val requestModelFactory: MobileEngageRequestModelFactory
) : RefreshTokenInternal {

    override fun refreshContactToken(completionListener: CompletionListener?) {
        try {
            val requestModel = requestModelFactory.createRefreshContactTokenRequest()
            restClient.execute(requestModel, object : CoreCompletionHandler {
                override fun onSuccess(id: String, responseModel: ResponseModel) {
                    tokenResponseHandler.processResponse(responseModel)
                    completionListener?.onCompleted(null)
                }

                override fun onError(id: String, responseModel: ResponseModel) {
                    completionListener?.onCompleted(
                        ResponseErrorException(
                            responseModel.statusCode,
                            responseModel.message,
                            responseModel.body
                        )
                    )
                }

                override fun onError(id: String, cause: Exception) {
                    completionListener?.onCompleted(cause)
                }
            })

        } catch (e: IllegalArgumentException) {
            completionListener?.onCompleted(e)
        }
    }
}