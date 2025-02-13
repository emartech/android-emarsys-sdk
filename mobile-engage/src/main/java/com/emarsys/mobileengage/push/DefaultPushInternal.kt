package com.emarsys.mobileengage.push

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.request.RequestManager
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.api.push.NotificationInformationListener
import com.emarsys.mobileengage.event.CacheableEventHandler
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory

class DefaultPushInternal(
    private val requestManager: RequestManager,
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val requestModelFactory: MobileEngageRequestModelFactory,
    private val eventServiceInternal: EventServiceInternal,
    private val pushTokenStorage: Storage<String?>,
    private val localPushTokenStorage: Storage<String?>,
    private val notificationCacheableEventHandler: CacheableEventHandler,
    private val silentMessageCacheableEventHandler: CacheableEventHandler,
    private val notificationInformationListenerProvider: NotificationInformationListenerProvider,
    private val silentNotificationInformationListenerProvider: SilentNotificationInformationListenerProvider,
    private val isAutomaticPushSendingEnabled: Boolean
) : PushInternal {

    override fun setPushToken(pushToken: String, completionListener: CompletionListener?) {
        localPushTokenStorage.set(pushToken)

        if (pushTokenStorage.get() != pushToken) {
            val requestModel = requestModelFactory.createSetPushTokenRequest(pushToken)

            requestManager.submit(requestModel) {
                if (it == null) {
                    pushTokenStorage.set(pushToken)
                }
                completionListener?.onCompleted(it)
            }
        } else {
            concurrentHandlerHolder.postOnMain {
                completionListener?.onCompleted(null)
            }
        }
    }

    override val pushToken: String?
        get() = pushTokenStorage.get()
            ?: (if (isAutomaticPushSendingEnabled) localPushTokenStorage.get() else null)

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

    override fun trackMessageOpen(
        sid: String?,
        completionListener: CompletionListener?
    ) {
        if (sid != null) {
            handleMessageOpen(completionListener, sid)
        } else {
            concurrentHandlerHolder.postOnMain {
                completionListener?.onCompleted(
                    IllegalArgumentException("No messageId found!")
                )
            }
        }
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