package com.emarsys

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner
import com.emarsys.common.feature.InnerFeature.*
import com.emarsys.config.ConfigApi
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.api.proxyApi
import com.emarsys.core.api.proxyWithLogExceptions
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerType
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.di.DefaultEmarsysDependencies
import com.emarsys.di.EmarsysDependencyInjection
import com.emarsys.di.emarsys
import com.emarsys.di.isEmarsysComponentSetup
import com.emarsys.geofence.GeofenceApi
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.MessageInboxApi
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.oneventaction.OnEventActionApi
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
        get() = EmarsysDependencyInjection.geofence()

    @JvmStatic
    val config: ConfigApi
        get() = emarsys().config

    @JvmStatic
    val push: PushApi
        get() = EmarsysDependencyInjection.push()

    @JvmStatic
    val inApp: InAppApi
        get() = EmarsysDependencyInjection.inApp()

    @JvmStatic
    val onEventAction: OnEventActionApi
        get() = EmarsysDependencyInjection.onEventAction()

    @JvmStatic
    val messageInbox: MessageInboxApi
        get() = EmarsysDependencyInjection.messageInbox()

    @JvmStatic
    val predict: PredictApi
        get() = EmarsysDependencyInjection.predict()

    @JvmStatic
    fun setup(emarsysConfig: EmarsysConfig) {
        for (feature in emarsysConfig.experimentalFeatures) {
            FeatureRegistry.enableFeature(feature)
        }

        if (emarsysConfig.mobileEngageApplicationCode != null) {
            FeatureRegistry.enableFeature(MOBILE_ENGAGE)
            FeatureRegistry.enableFeature(EVENT_SERVICE_V4)
        }

        if (emarsysConfig.predictMerchantId != null) {
            FeatureRegistry.enableFeature(PREDICT)
        }


        if (!isEmarsysComponentSetup()) {
            DefaultEmarsysDependencies(emarsysConfig)
        }

        emarsys().coreSdkHandler.post {
            initializeInAppInternal(emarsysConfig)

            registerWatchDogs(emarsysConfig)
            registerLifecycleObservers()
            registerDatabaseTriggers()

            if (FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE)) {
                initializeMobileEngageContact()
            }
        }
    }

    private fun registerLifecycleObservers() {
        val appLifecycleObserver = emarsys().appLifecycleObserver
        emarsys().uiHandler.post {
            ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun setAuthenticatedContact(openIdToken: String, completionListener: CompletionListener? = null) {
        if (FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) || !FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) && !FeatureRegistry.isFeatureEnabled(PREDICT)) {
            EmarsysDependencyInjection.mobileEngageApi()
                    .proxyApi(mobileEngage().coreSdkHandler)
                    .setAuthenticatedContact(openIdToken, completionListener)
        }

        FeatureRegistry.disableFeature(PREDICT)
    }

    @JvmStatic
    fun setContact(contactId: String) {
        if (FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) || !FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) && !FeatureRegistry.isFeatureEnabled(PREDICT)) {
            EmarsysDependencyInjection.mobileEngageApi()
                    .proxyApi(mobileEngage().coreSdkHandler)
                    .setContact(contactId, null)
        }
        if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
            mobileEngage().coreSdkHandler.post {
                EmarsysDependencyInjection.predictRestrictedApi()
                        .proxyApi(mobileEngage().coreSdkHandler)
                        .setContact(contactId)
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
        if (FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) || !FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) && !FeatureRegistry.isFeatureEnabled(PREDICT)) {
            EmarsysDependencyInjection.mobileEngageApi()
                    .proxyApi(mobileEngage().coreSdkHandler)
                    .setContact(contactId, completionListener)
        }
        if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
            EmarsysDependencyInjection.predictRestrictedApi()
                    .proxyApi(mobileEngage().coreSdkHandler)
                    .setContact(contactId)
        }
    }

    @JvmStatic
    fun clearContact() {
        if (FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) || !FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) && !FeatureRegistry.isFeatureEnabled(PREDICT)) {
            EmarsysDependencyInjection.mobileEngageApi()
                    .proxyApi(mobileEngage().coreSdkHandler)
                    .clearContact(null)
        }
        if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
            mobileEngage().coreSdkHandler.post {
                EmarsysDependencyInjection.predictRestrictedApi()
                        .proxyApi(mobileEngage().coreSdkHandler)
                        .clearContact()
            }
        }
    }

    @JvmStatic
    fun clearContact(completionListener: (throwable: Throwable?) -> Unit) {
        clearContact(CompletionListener { completionListener(it) })
    }

    @JvmStatic
    fun clearContact(completionListener: CompletionListener) {
        if (FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) || !FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) && !FeatureRegistry.isFeatureEnabled(PREDICT)) {
            EmarsysDependencyInjection.mobileEngageApi()
                    .proxyApi(mobileEngage().coreSdkHandler)
                    .clearContact(completionListener)
        }
        if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
            EmarsysDependencyInjection.predictRestrictedApi()
                    .proxyApi(mobileEngage().coreSdkHandler)
                    .clearContact()
        }
    }

    @JvmStatic
    fun trackDeepLink(activity: Activity,
                      intent: Intent) {
        EmarsysDependencyInjection.deepLinkApi()
                .proxyApi(mobileEngage().coreSdkHandler)
                .trackDeepLinkOpen(activity, intent, null)
    }

    @JvmStatic
    fun trackDeepLink(activity: Activity, intent: Intent, completionListener: (throwable: Throwable?) -> Unit) {
        trackDeepLink(activity, intent, CompletionListener { completionListener(it) })
    }

    @JvmStatic
    fun trackDeepLink(activity: Activity,
                      intent: Intent,
                      completionListener: CompletionListener) {
        EmarsysDependencyInjection.deepLinkApi()
                .proxyApi(mobileEngage().coreSdkHandler)
                .trackDeepLinkOpen(activity, intent, completionListener)
    }

    @JvmStatic
    fun trackCustomEvent(
            eventName: String,
            eventAttributes: Map<String, String>?) {
        EmarsysDependencyInjection.eventServiceApi()
                .proxyApi(mobileEngage().coreSdkHandler)
                .trackCustomEventAsync(eventName, eventAttributes, null)
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
        EmarsysDependencyInjection.eventServiceApi()
                .proxyApi(mobileEngage().coreSdkHandler)
                .trackCustomEventAsync(eventName, eventAttributes, completionListener)
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
            predict.recommendProducts(recommendationLogic, resultListener = resultListener)
        }

        @JvmStatic
        fun recommendProducts(recommendationLogic: Logic, limit: Int, resultListener: ResultListener<Try<List<Product>>>) {
            predict.recommendProducts(recommendationLogic, limit, resultListener)
        }

        @JvmStatic
        fun recommendProducts(recommendationLogic: Logic, limit: Int, resultListener: (result: Try<List<Product>>) -> Unit) {
            predict.recommendProducts(recommendationLogic, limit = limit, resultListener = resultListener)
        }

        @JvmStatic
        fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, limit: Int, resultListener: ResultListener<Try<List<Product>>>) {
            predict.recommendProducts(recommendationLogic, recommendationFilters, limit, resultListener)
        }

        @JvmStatic
        fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, limit: Int, resultListener: (result: Try<List<Product>>) -> Unit) {
            predict.recommendProducts(recommendationLogic, recommendationFilters, limit, resultListener = resultListener)
        }

        @JvmStatic
        fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, resultListener: ResultListener<Try<List<Product>>>) {
            predict.recommendProducts(recommendationLogic, recommendationFilters, resultListener)
        }

        @JvmStatic
        fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, resultListener: (result: Try<List<Product>>) -> Unit) {
            predict.recommendProducts(recommendationLogic, recommendationFilters, resultListener = resultListener)
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
        config.application.registerActivityLifecycleCallbacks(
                emarsys().activityLifecycleWatchdog)
        config.application.registerActivityLifecycleCallbacks(emarsys().currentActivityWatchdog)
    }

    private fun registerDatabaseTriggers() {
        if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
            emarsys().coreSQLiteDatabase
                    .registerTrigger(
                            DatabaseContract.SHARD_TABLE_NAME,
                            TriggerType.AFTER,
                            TriggerEvent.INSERT,
                            emarsys().predictShardTrigger)
        }

        emarsys().coreSQLiteDatabase
                .registerTrigger(
                        DatabaseContract.SHARD_TABLE_NAME,
                        TriggerType.AFTER,
                        TriggerEvent.INSERT,
                        emarsys().logShardTrigger)
    }

    private fun initializeMobileEngageContact() {
        val deviceInfoPayload = emarsys().deviceInfoPayloadStorage.get()
        val contactToken = emarsys().contactTokenStorage.get()
        val requestContext = emarsys().requestContext
        val clientState = emarsys().clientStateStorage.get()
        val deviceInfo = emarsys().deviceInfo

        if (contactToken == null && !requestContext.hasContactIdentification()) {
            if (clientState == null || deviceInfoPayload != null && deviceInfoPayload != deviceInfo.deviceInfoPayload) {
                EmarsysDependencyInjection.clientServiceApi()
                        .proxyWithLogExceptions()
                        .trackDeviceInfo(null)
            }
            EmarsysDependencyInjection.mobileEngageApi()
                    .proxyWithLogExceptions()
                    .setContact(null, null)
        }
    }
}