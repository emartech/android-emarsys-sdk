package com.emarsys.mobileengage.inbox

import android.os.Handler
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.api.inbox.InboxMessage
import com.emarsys.mobileengage.api.inbox.MessageInboxResult
import com.emarsys.mobileengage.inbox.model.NotificationCache
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory

class DefaultMessageInboxInternal(private val NotificationCache: NotificationCache,
                                  private val requestManager: RequestManager,
                                  private val mobileEngageRequestContext: MobileEngageRequestContext,
                                  private val mobileEngageRequestModelFactory: MobileEngageRequestModelFactory,
                                  private val handler: Handler) : MessageInboxInternal {

    override fun fetchInboxMessages(resultListener: ResultListener<Try<MessageInboxResult>>) {
        if (mobileEngageRequestContext.contactFieldValueStorage.get() != null) {
            handleFetchRequest(resultListener)
        } else {
            handler.post {
                resultListener.onResult(Try.failure(NotificationInboxException("setContact must be called before calling fetchInboxMessages!")))
            }
        }
    }

    private fun handleFetchRequest(resultListener: ResultListener<Try<MessageInboxResult>>) {
        val requestModel = mobileEngageRequestModelFactory.createFetchInboxMessagesRequest()

        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String, responseModel: ResponseModel) {
                resultListener.onResult(Try.success(MessageInboxResult(listOf())))
            }

            override fun onError(id: String, responseModel: ResponseModel) {
                resultListener.onResult(Try.failure(ResponseErrorException(
                        responseModel.statusCode,
                        responseModel.message,
                        responseModel.body
                )))
            }

            override fun onError(id: String, cause: Exception) {
                resultListener.onResult(Try.failure(cause))
            }
        })
    }

    override fun trackNotificationOpen(notification: InboxMessage, completionListener: CompletionListener?) {
    }
}