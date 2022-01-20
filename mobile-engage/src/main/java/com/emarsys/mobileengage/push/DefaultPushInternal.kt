package com.emarsys.mobileengage.push

import android.content.Intent
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.request.RequestManager
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.api.push.NotificationInformationListener
import com.emarsys.mobileengage.event.CacheableEventHandler
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class DefaultPushInternal(
    private val requestManager: RequestManager,
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val requestModelFactory: MobileEngageRequestModelFactory,
    private val eventServiceInternal: EventServiceInternal,
    private val pushTokenStorage: Storage<String?>,
    private val notificationCacheableEventHandler: CacheableEventHandler,
    private val silentMessageCacheableEventHandler: CacheableEventHandler,
    private val notificationInformationListenerProvider: NotificationInformationListenerProvider,
    private val silentNotificationInformationListenerProvider: SilentNotificationInformationListenerProvider
) : PushInternal {

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
            concurrentHandlerHolder.uiScope.launch {
                completionListener?.onCompleted(null)
            }
        }
    }

    override fun getPushToken(): String? {
        return pushTokenStorage.get()
    }

    override fun setNotificationInformationListener(notificationInformationListener: NotificationInformationListener) {
        notificationInformationListenerProvider.notificationInformationListener =
            notificationInformationListener
    }

    override fun setSilentNotificationInformationListener(silentNotificationInformationListener: NotificationInformationListener) {
        silentNotificationInformationListenerProvider.silentNotificationInformationListener =
            silentNotificationInformationListener
    }

    override fun clearPushToken(completionListener: CompletionListener?) {
        val requestModel = requestModelFactory.createRemovePushTokenRequest()
        pushTokenStorage.remove()
        requestManager.submit(requestModel, completionListener)
    }

    override fun trackMessageOpen(intent: Intent, completionListener: CompletionListener?) {
        val messageId = getMessageId(intent)
        messageId?.let { handleMessageOpen(completionListener, it) }
            ?: concurrentHandlerHolder.uiScope.launch {
                completionListener?.onCompleted(
                    IllegalArgumentException("No messageId found!")
                )
            }
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
        eventServiceInternal.trackInternalCustomEventAsync(
            "push:click",
            attributes,
            completionListener
        )
    }

    override fun setNotificationEventHandler(notificationEventHandler: EventHandler) {
        notificationCacheableEventHandler.setEventHandler(notificationEventHandler)
    }

    override fun setSilentMessageEventHandler(silentMessageEventHandler: EventHandler) {
        silentMessageCacheableEventHandler.setEventHandler(silentMessageEventHandler)
    }
}