package com.emarsys

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.emarsys.config.ConfigApi
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.RunnerProxy
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerType
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.util.Assert
import com.emarsys.di.DefaultEmarsysDependencyContainer
import com.emarsys.di.EmarsysDependencyContainer
import com.emarsys.di.EmarsysDependencyInjection
import com.emarsys.feature.InnerFeature.MOBILE_ENGAGE
import com.emarsys.feature.InnerFeature.PREDICT
import com.emarsys.geofence.GeofenceApi
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.InboxApi
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.api.inbox.Notification
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus
import com.emarsys.predict.PredictApi
import com.emarsys.predict.api.model.CartItem
import com.emarsys.predict.api.model.Logic
import com.emarsys.predict.api.model.Product
import com.emarsys.predict.api.model.RecommendationFilter
import com.emarsys.push.PushApi
import org.json.JSONObject

object Emarsys {

    @JvmStatic
    val geofence: GeofenceApi
        get() = container.geofence

    @JvmStatic
    val config: ConfigApi
        get() = container.config

    @JvmStatic
    val push: PushApi
        get() = EmarsysDependencyInjection.push()

    @JvmStatic
    val inApp: InAppApi
        get() = EmarsysDependencyInjection.inApp()

    @JvmStatic
    val inbox: InboxApi
        get() = EmarsysDependencyInjection.inbox()

    @JvmStatic
    val predict: PredictApi
        get() = EmarsysDependencyInjection.predict()

    private val container: EmarsysDependencyContainer
        get() = DependencyInjection.getContainer()

    private val runnerProxy: RunnerProxy
        get() = container.runnerProxy

    @JvmStatic
    fun setup(emarsysConfig: EmarsysConfig) {
        Assert.notNull(emarsysConfig, "Config must not be null!")

        for (feature in emarsysConfig.experimentalFeatures) {
            FeatureRegistry.enableFeature(feature)
        }

        DependencyInjection.setup(DefaultEmarsysDependencyContainer(emarsysConfig))

        initializeInAppInternal(emarsysConfig)

        registerWatchDogs(emarsysConfig)

        registerDatabaseTriggers()

        initializeContact()
    }

    @JvmStatic
    fun setContact(contactId: String) {
        runnerProxy.logException {
            Assert.notNull(contactId, "ContactId must not be null!")

            if (FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) || !FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) && !FeatureRegistry.isFeatureEnabled(PREDICT)) {
                EmarsysDependencyInjection.mobileEngageInternal().setContact(contactId, null)
            }
            if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
                EmarsysDependencyInjection.predictInternal().setContact(contactId)
            }
        }
    }

    @JvmStatic
    fun setContact(contactId: String,
                   completionListener: (throwable: Throwable?) -> Unit) {
        setContact(contactId, CompletionListener { completionListener(it) })
    }

    @JvmStatic
    fun setContact(
            contactId: String,
            completionListener: CompletionListener) {
        runnerProxy.logException {
            Assert.notNull(contactId, "ContactId must not be null!")
            Assert.notNull(completionListener, "CompletionListener must not be null!")

            if (FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) || !FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) && !FeatureRegistry.isFeatureEnabled(PREDICT)) {
                EmarsysDependencyInjection.mobileEngageInternal().setContact(contactId, completionListener)
            }
            if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
                EmarsysDependencyInjection.predictInternal().setContact(contactId)
            }
        }
    }

    @JvmStatic
    fun clearContact() {
        runnerProxy.logException {
            if (FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) || !FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) && !FeatureRegistry.isFeatureEnabled(PREDICT)) {
                EmarsysDependencyInjection.mobileEngageInternal().clearContact(null)
            }
            if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
                EmarsysDependencyInjection.predictInternal().clearContact()
            }
        }
    }

    @JvmStatic
    fun clearContact(completionListener: (throwable: Throwable?) -> Unit) {
        clearContact(CompletionListener { completionListener(it) })
    }

    @JvmStatic
    fun clearContact(completionListener: CompletionListener) {
        runnerProxy.logException {
            Assert.notNull(completionListener, "CompletionListener must not be null!")

            if (FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) || !FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) && !FeatureRegistry.isFeatureEnabled(PREDICT)) {
                EmarsysDependencyInjection.mobileEngageInternal().clearContact(completionListener)
            }
            if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
                EmarsysDependencyInjection.predictInternal().clearContact()
            }
        }
    }

    @JvmStatic
    fun trackDeepLink(activity: Activity,
                      intent: Intent) {
        runnerProxy.logException {
            Assert.notNull(activity, "Activity must not be null!")
            Assert.notNull(intent, "Intent must not be null!")

            EmarsysDependencyInjection.deepLinkInternal().trackDeepLinkOpen(activity, intent, null)
        }
    }

    @JvmStatic
    fun trackDeepLink(activity: Activity, intent: Intent, completionListener: (throwable: Throwable?) -> Unit) {
        trackDeepLink(activity, intent, CompletionListener { completionListener(it) })
    }

    @JvmStatic
    fun trackDeepLink(activity: Activity,
                      intent: Intent,
                      completionListener: CompletionListener) {
        runnerProxy.logException {
            Assert.notNull(activity, "Activity must not be null!")
            Assert.notNull(intent, "Intent must not be null!")
            Assert.notNull(completionListener, "CompletionListener must not be null!")

            EmarsysDependencyInjection.deepLinkInternal().trackDeepLinkOpen(activity, intent, completionListener)
        }
    }

    @JvmStatic
    fun trackCustomEvent(
            eventName: String,
            eventAttributes: Map<String, String>?) {
        runnerProxy.logException {
            Assert.notNull(eventName, "EventName must not be null!")

            EmarsysDependencyInjection.eventServiceInternal().trackCustomEvent(eventName, eventAttributes, null)
        }
    }

    @JvmStatic
    fun trackCustomEvent(eventName: String,
                         eventAttributes: Map<String, String>?,
                         completionListener: (throwable: Throwable?) -> Unit) {
        trackCustomEvent(eventName, eventAttributes, CompletionListener { completionListener(it) })
    }

    @JvmStatic
    fun trackCustomEvent(
            eventName: String,
            eventAttributes: Map<String, String>?,
            completionListener: CompletionListener) {
        runnerProxy.logException {
            Assert.notNull(eventName, "EventName must not be null!")
            Assert.notNull(completionListener, "CompletionListener must not be null!")

            EmarsysDependencyInjection.eventServiceInternal().trackCustomEvent(eventName, eventAttributes, completionListener)
        }
    }

    @Deprecated(message = "Use config property instead, will be removed in 3.0.0", replaceWith = ReplaceWith("Emarsys.config"))
    object Config {

        @JvmStatic
        val contactFieldId: Int
            get() = config.contactFieldId

        @JvmStatic
        val applicationCode: String?
            get() = config.applicationCode

        @JvmStatic
        val merchantId: String?
            get() = config.merchantId

        @JvmStatic
        fun changeApplicationCode(applicationCode: String?) {
            changeApplicationCode(applicationCode, config.contactFieldId)
        }

        @JvmStatic
        fun changeApplicationCode(applicationCode: String?, completionListener: (throwable: Throwable?) -> Unit) {
            changeApplicationCode(applicationCode, config.contactFieldId, CompletionListener { completionListener(it) })
        }

        @JvmStatic
        fun changeApplicationCode(applicationCode: String?, completionListener: CompletionListener) {
            changeApplicationCode(applicationCode, config.contactFieldId, completionListener)
        }

        @JvmStatic
        fun changeApplicationCode(applicationCode: String?, contactFieldId: Int) {
            config.changeApplicationCode(applicationCode, contactFieldId, null)
        }

        @JvmStatic
        fun changeApplicationCode(applicationCode: String?, contactFieldId: Int, completionListener: (throwable: Throwable?) -> Unit) {
            changeApplicationCode(applicationCode, contactFieldId, CompletionListener { completionListener(it) })
        }

        @JvmStatic
        fun changeApplicationCode(applicationCode: String?, contactFieldId: Int, completionListener: CompletionListener) {
            config.changeApplicationCode(applicationCode, contactFieldId, completionListener)
        }

        @JvmStatic
        fun changeMerchantId(merchantId: String?) {
            config.changeMerchantId(merchantId)
        }
    }

    @Deprecated(message = "Use push property instead, will be removed in 3.0.0", replaceWith = ReplaceWith("Emarsys.push"))
    object Push {

        @JvmStatic
        @Deprecated(message = "This method is not necessary anymore. EmarsysMessagingService takes care of it.", replaceWith = ReplaceWith("EmarsysMessagingService.handleMessage()"))
        fun trackMessageOpen(intent: Intent) {
            push.trackMessageOpen(intent)
        }

        @Deprecated(message = "This method is not necessary anymore. EmarsysMessagingService takes care of it.", replaceWith = ReplaceWith("EmarsysMessagingService.handleMessage()"))
        @JvmStatic
        fun trackMessageOpen(
                intent: Intent,
                completionListener: (throwable: Throwable?) -> Unit) {
            trackMessageOpen(intent, CompletionListener { completionListener(it) })
        }

        @Deprecated(message = "This method is not necessary anymore. EmarsysMessagingService takes care of it.", replaceWith = ReplaceWith("EmarsysMessagingService.handleMessage()"))
        @JvmStatic
        fun trackMessageOpen(
                intent: Intent,
                completionListener: CompletionListener) {
            push.trackMessageOpen(intent, completionListener)
        }

        @JvmStatic
        fun setPushToken(pushToken: String) {
            push.setPushToken(pushToken)
        }

        @JvmStatic
        fun setPushToken(
                pushToken: String,
                completionListener: (throwable: Throwable?) -> Unit) {
            setPushToken(pushToken, CompletionListener { completionListener(it) })
        }

        @JvmStatic
        fun setPushToken(
                pushToken: String,
                completionListener: CompletionListener) {
            push.setPushToken(pushToken, completionListener)
        }

        @JvmStatic
        fun clearPushToken() {
            push.clearPushToken()
        }

        @JvmStatic
        fun clearPushToken(completionListener: (throwable: Throwable?) -> Unit) {
            clearPushToken(CompletionListener { completionListener(it) })
        }

        @JvmStatic
        fun clearPushToken(completionListener: CompletionListener) {
            push.clearPushToken(completionListener)
        }

        @JvmStatic
        fun setNotificationEventHandler(notificationEventHandler: EventHandler) {
            push.setNotificationEventHandler(notificationEventHandler)
        }

        @JvmStatic
        fun setSilentMesssageEventHandler(silentNotificationEventHandler: EventHandler) {
            push.setSilentMessageEventHandler(silentNotificationEventHandler)
        }
    }

    @Deprecated(message = "Use predict property instead, will be removed in 3.0.0", replaceWith = ReplaceWith("Emarsys.predict"))
    object Predict {

        @JvmStatic
        fun trackCart(items: List<CartItem>) {
            predict.trackCart(items)
        }

        @JvmStatic
        fun trackPurchase(orderId: String,
                          items: List<CartItem>) {
            predict.trackPurchase(orderId, items)
        }

        @JvmStatic
        fun trackItemView(itemId: String) {
            predict.trackItemView(itemId)
        }

        @JvmStatic
        fun trackCategoryView(categoryPath: String) {
            predict.trackCategoryView(categoryPath)
        }

        @JvmStatic
        fun trackSearchTerm(searchTerm: String) {
            predict.trackSearchTerm(searchTerm)
        }

        @JvmStatic
        fun trackTag(tag: String, attributes: Map<String, String>?) {
            predict.trackTag(tag, attributes)
        }

        @JvmStatic
        fun recommendProducts(recommendationLogic: Logic, resultListener: ResultListener<Try<List<Product>>>) {
            predict.recommendProducts(recommendationLogic, resultListener)
        }

        @JvmStatic
        fun recommendProducts(recommendationLogic: Logic, resultListener: (result: Try<List<Product>>) -> Unit) {
            predict.recommendProducts(recommendationLogic) { resultListener(it) }
        }

        @JvmStatic
        fun recommendProducts(recommendationLogic: Logic, limit: Int, resultListener: ResultListener<Try<List<Product>>>) {
            predict.recommendProducts(recommendationLogic, limit, resultListener)
        }

        @JvmStatic
        fun recommendProducts(recommendationLogic: Logic, limit: Int, resultListener: (result: Try<List<Product>>) -> Unit) {
            predict.recommendProducts(recommendationLogic, limit) { resultListener(it) }
        }

        @JvmStatic
        fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, limit: Int, resultListener: ResultListener<Try<List<Product>>>) {
            predict.recommendProducts(recommendationLogic, recommendationFilters, limit, resultListener)
        }

        @JvmStatic
        fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, limit: Int, resultListener: (result: Try<List<Product>>) -> Unit) {
            predict.recommendProducts(recommendationLogic, recommendationFilters, limit) { resultListener(it) }
        }

        @JvmStatic
        fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, resultListener: ResultListener<Try<List<Product>>>) {
            predict.recommendProducts(recommendationLogic, recommendationFilters, resultListener)
        }

        @JvmStatic
        fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, resultListener: (result: Try<List<Product>>) -> Unit) {
            predict.recommendProducts(recommendationLogic, recommendationFilters) { resultListener(it) }
        }

        @JvmStatic
        fun trackRecommendationClick(product: Product) {
            predict.trackRecommendationClick(product)
        }
    }

    @Deprecated(message = "Use inApp property instead, will be removed in 3.0.0", replaceWith = ReplaceWith("Emarsys.inApp"))
    object InApp {

        @JvmStatic
        val isPaused: Boolean
            get() = inApp.isPaused

        @JvmStatic
        fun pause() {
            inApp.pause()
        }

        @JvmStatic
        fun resume() {
            inApp.resume()
        }

        @JvmStatic
        fun setEventHandler(eventHandler: EventHandler) {
            inApp.setEventHandler(eventHandler)
        }
    }

    @Deprecated(message = "Use inbox property instead, will be removed in 3.0.0", replaceWith = ReplaceWith("Emarsys.inbox"))
    object Inbox {

        @JvmStatic
        fun fetchNotifications(resultListener: ResultListener<Try<NotificationInboxStatus>>) {
            inbox.fetchNotifications(resultListener)
        }

        @JvmStatic
        fun fetchNotifications(resultListener: (result: Try<NotificationInboxStatus>) -> Unit) {
            inbox.fetchNotifications { resultListener(it) }
        }

        @JvmStatic
        fun trackNotificationOpen(notification: Notification) {
            inbox.trackNotificationOpen(notification)
        }

        @JvmStatic
        fun trackNotificationOpen(notification: Notification, completionListener: (throwable: Throwable?) -> Unit) {
            trackNotificationOpen(notification, CompletionListener { completionListener(it) })
        }

        @JvmStatic
        fun trackNotificationOpen(notification: Notification, completionListener: CompletionListener) {
            inbox.trackNotificationOpen(notification, completionListener)
        }

        @JvmStatic
        fun resetBadgeCount() {
            inbox.resetBadgeCount()
        }

        @JvmStatic
        fun resetBadgeCount(completionListener: (throwable: Throwable?) -> Unit) {
            resetBadgeCount(CompletionListener { completionListener(it) })
        }

        @JvmStatic
        fun resetBadgeCount(completionListener: CompletionListener) {
            inbox.resetBadgeCount(completionListener)
        }
    }

    @Deprecated(message = "Will be removed in 3.0.0")
    private fun initializeInAppInternal(config: EmarsysConfig) {
        val inAppEventHandler = config.inAppEventHandler

        if (inAppEventHandler != null) {
            inApp.setEventHandler(object : EventHandler {
                override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
                    inAppEventHandler.handleEvent(eventName, payload)
                }
            })
        }
    }

    private fun registerWatchDogs(config: EmarsysConfig) {
        config.application.registerActivityLifecycleCallbacks(container.activityLifecycleWatchdog)
        config.application.registerActivityLifecycleCallbacks(container.currentActivityWatchdog)
    }

    private fun registerDatabaseTriggers() {
        if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
            container.coreSQLiteDatabase.registerTrigger(
                    DatabaseContract.SHARD_TABLE_NAME,
                    TriggerType.AFTER,
                    TriggerEvent.INSERT,
                    container.predictShardTrigger)
        }

        container.coreSQLiteDatabase.registerTrigger(
                DatabaseContract.SHARD_TABLE_NAME,
                TriggerType.AFTER,
                TriggerEvent.INSERT,
                container.logShardTrigger)
    }

    private fun initializeContact() {
        val deviceInfoHash = container.deviceInfoHashStorage.get()
        val contactToken = container.contactTokenStorage.get()
        val contactFieldValue = container.contactFieldValueStorage.get()
        val clientState = container.clientStateStorage.get()
        val deviceInfo = container.deviceInfo

        if (contactToken == null && contactFieldValue == null) {
            if (clientState == null || deviceInfoHash != null && deviceInfoHash != deviceInfo.hash) {
                EmarsysDependencyInjection.clientServiceInternal().trackDeviceInfo()
            }
            EmarsysDependencyInjection.mobileEngageInternal().setContact(null, null)
        }
    }
}