package com.emarsys.mobileengage.inbox

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.api.inbox.InboxResult
import com.emarsys.mobileengage.api.inbox.Message
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import java.util.*

class DefaultMessageInboxInternal(
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val requestManager: RequestManager,
    private val mobileEngageRequestModelFactory: MobileEngageRequestModelFactory,
    private val messageInboxResponseMapper: MessageInboxResponseMapper
) : MessageInboxInternal {

    var messages: List<Message>? = null

    override fun fetchMessages(resultListener: ResultListener<Try<InboxResult>>) {
        handleFetchRequest(resultListener)
    }

    override fun addTag(tag: String, messageId: String, completionListener: CompletionListener?) {
        updateTag(completionListener, { shouldAdd(it, messageId, tag) }) {
            val eventAttributes = mapOf(
                "messageId" to messageId,
                "tag" to tag.lowercase(Locale.ENGLISH)
            )
            mobileEngageRequestModelFactory.createInternalCustomEventRequest(
                "inbox:tag:add",
                eventAttributes
            )
        }
    }

    override fun removeTag(
        tag: String,
        messageId: String,
        completionListener: CompletionListener?
    ) {
        updateTag(completionListener, { shouldRemove(it, messageId, tag) }) {
            val eventAttributes = mapOf(
                "messageId" to messageId,
                "tag" to tag.lowercase(Locale.ENGLISH)
            )
            mobileEngageRequestModelFactory.createInternalCustomEventRequest(
                "inbox:tag:remove",
                eventAttributes
            )
        }
    }

    private fun updateTag(
        completionListener: CompletionListener?,
        predicate: (Message) -> Boolean,
        updateRequest: () -> RequestModel
    ) {
        (messages?.any {
            predicate(it)
        } ?: true).let { shouldUpdateTag ->
            if (shouldUpdateTag) {
                requestManager.submit(updateRequest(), completionListener)
            } else {
                completionListener?.onCompleted(null)
            }
        }
    }

    private fun shouldAdd(message: Message, messageId: String, tag: String): Boolean {
        return (message.id.lowercase() == messageId.lowercase()
                && message.tags != null
                && !message.tags!!.contains(tag.lowercase()))
    }

    private fun shouldRemove(message: Message, messageId: String, tag: String): Boolean {
        return (message.id.lowercase() == messageId.lowercase()
                && message.tags != null
                && message.tags!!.contains(tag.lowercase()))
    }

    private fun handleFetchRequest(resultListener: ResultListener<Try<InboxResult>>) {
        val requestModel = mobileEngageRequestModelFactory.createFetchInboxMessagesRequest()

        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String, responseModel: ResponseModel) {
                val result = messageInboxResponseMapper.map(responseModel)
                messages = result.messages
                resultListener.onResult(Try.success(result))
            }

            override fun onError(id: String, responseModel: ResponseModel) {
                messages = null
                resultListener.onResult(
                    Try.failure(
                        ResponseErrorException(
                            responseModel.statusCode,
                            responseModel.message,
                            responseModel.body
                        )
                    )
                )
            }

            override fun onError(id: String, cause: Exception) {
                messages = null
                resultListener.onResult(Try.failure(cause))
            }
        }, concurrentHandlerHolder.uiHandler)
    }
}