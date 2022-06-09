package com.emarsys.mobileengage.inbox

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.request.RequestManager
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
        updateTag(
            conditionCallback = {
                it.id.lowercase() == messageId.lowercase() && it.tags != null && !it.tags!!.contains(tag.lowercase())
            },
            requestUnnecessaryCallback = {
                completionListener?.onCompleted(null)
            },
            runnerCallback = {
                val eventAttributes = mapOf(
                    "messageId" to messageId,
                    "tag" to tag.lowercase(Locale.ENGLISH)
                )
                val requestModel = mobileEngageRequestModelFactory.createInternalCustomEventRequest(
                    "inbox:tag:add",
                    eventAttributes
                )

                requestManager.submit(requestModel, completionListener)
            }
        )
    }

    override fun removeTag(
        tag: String,
        messageId: String,
        completionListener: CompletionListener?
    ) {
        updateTag(
            conditionCallback = {
                it.id.lowercase() == messageId.lowercase() && it.tags != null && it.tags!!.contains(tag.lowercase())
            },
            requestUnnecessaryCallback = {
                completionListener?.onCompleted(null)
            },
            runnerCallback = {
                val eventAttributes = mapOf(
                    "messageId" to messageId,
                    "tag" to tag.lowercase(Locale.ENGLISH)
                )
                val requestModel = mobileEngageRequestModelFactory.createInternalCustomEventRequest(
                    "inbox:tag:remove",
                    eventAttributes
                )
                requestManager.submit(requestModel, completionListener)
            }
        )
    }

    private fun updateTag(
        conditionCallback: (Message) -> (Boolean),
        runnerCallback: () -> Unit,
        requestUnnecessaryCallback: () -> Unit
    ) {
        if (messages != null) {
            val triggeredCount = messages!!.count {
                val result = conditionCallback(it)
                if (result) {
                    runnerCallback()
                }
                result
            }
            if (triggeredCount == 0) {
                requestUnnecessaryCallback()
            }
        } else {
            runnerCallback()
        }
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