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
import com.emarsys.mobileengage.api.inbox.InboxResult
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import java.util.*

class DefaultMessageInboxInternal(private val requestManager: RequestManager,
                                  private val mobileEngageRequestContext: MobileEngageRequestContext,
                                  private val mobileEngageRequestModelFactory: MobileEngageRequestModelFactory,
                                  private val handler: Handler,
                                  private val messageInboxResponseMapper: MessageInboxResponseMapper) : MessageInboxInternal {

    override fun fetchMessages(resultListener: ResultListener<Try<InboxResult>>) {
        if (mobileEngageRequestContext.contactFieldValueStorage.get() != null) {
            handleFetchRequest(resultListener)
        } else {
            handler.post {
                resultListener.onResult(Try.failure(NotificationInboxException("setContact must be called before calling fetchInboxMessages!")))
            }
        }
    }

    override fun addTag(tag: String, messageId: String, completionListener: CompletionListener?) {
        val eventAttributes = mapOf(
                "messageId" to messageId,
                "tag" to tag.toLowerCase(Locale.ENGLISH)
        )
        val requestModel = mobileEngageRequestModelFactory.createInternalCustomEventRequest("inbox:tag:add", eventAttributes)

        requestManager.submit(requestModel, completionListener)
    }

    override fun removeTag(tag: String, messageId: String, completionListener: CompletionListener?) {
        val eventAttributes = mapOf(
                "messageId" to messageId,
                "tag" to tag.toLowerCase(Locale.ENGLISH)
        )
        val requestModel = mobileEngageRequestModelFactory.createInternalCustomEventRequest("inbox:tag:remove", eventAttributes)

        requestManager.submit(requestModel, completionListener)
    }

    private fun handleFetchRequest(resultListener: ResultListener<Try<InboxResult>>) {
        val requestModel = mobileEngageRequestModelFactory.createFetchInboxMessagesRequest()

        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String, responseModel: ResponseModel) {
                resultListener.onResult(Try.success(messageInboxResponseMapper.map(responseModel)))
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
}