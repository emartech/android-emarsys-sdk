package com.emarsys.mobileengage.push

import android.content.Intent
import android.os.Handler
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.RequestManager
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.event.EventHandlerProvider
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class DefaultPushInternal(private val requestManager: RequestManager,
                          private val uiHandler: Handler,
                          private val requestModelFactory: MobileEngageRequestModelFactory,
                          private val eventServiceInternal: EventServiceInternal,
                          private val pushTokenStorage: StringStorage,
                          private val notificationEventHandlerProvider: EventHandlerProvider,
                          private val silentMessageEventHandlerProvider: EventHandlerProvider) : PushInternal {

    override fun setPushToken(pushToken: String, completionListener: CompletionListener?) {
        if (pushTokenStorage.get() != pushToken) {
            val requestModel = requestModelFactory.createSetPushTokenRequest(pushToken)

            requestManager.submit(requestModel) {
                if (it == null) {
                    pushTokenStorage.set(pushToken)
                }
                completionListener?.onCompleted(it)
            }
        } else {
            uiHandler.post {
                completionListener?.onCompleted(null)
            }
        }
    }

    override fun clearPushToken(completionListener: CompletionListener?) {
        val requestModel = requestModelFactory.createRemovePushTokenRequest()
        pushTokenStorage.remove()
        requestManager.submit(requestModel, completionListener)
    }

    override fun trackMessageOpen(intent: Intent, completionListener: CompletionListener?) {
        val messageId = getMessageId(intent)
        messageId?.let { handleMessageOpen(completionListener, it) }
                ?: uiHandler.post { completionListener?.onCompleted(IllegalArgumentException("No messageId found!")) }
    }

    fun getMessageId(intent: Intent): String? {
        var sid: String? = null
        val payload = intent.getBundleExtra("payload")
        if (payload != null) {
            val customData = payload.getString("u")
            if (customData != null) {
                try {
                    sid = JSONObject(customData).getString("sid")
                } catch (ignore: JSONException) {
                }
            }
        }
        return sid
    }

    private fun handleMessageOpen(completionListener: CompletionListener?, messageId: String) {
        val attributes = HashMap<String, String>()
        attributes["sid"] = messageId
        attributes["origin"] = "main"
        eventServiceInternal.trackInternalCustomEvent("push:click", attributes, completionListener)
    }

    override fun setNotificationEventHandler(notificationEventHandler: EventHandler) {
        notificationEventHandlerProvider.eventHandler = notificationEventHandler
    }

    override fun setSilentMessageEventHandler(silentMessageEventHandler: EventHandler) {
        silentMessageEventHandlerProvider.eventHandler = silentMessageEventHandler
    }
}