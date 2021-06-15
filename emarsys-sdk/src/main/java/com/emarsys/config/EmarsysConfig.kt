package com.emarsys.config

import android.app.Application
import android.content.Context
import com.emarsys.core.api.experimental.FlipperFeature

import com.emarsys.mobileengage.api.EventHandler
import com.emarsys.mobileengage.api.NotificationEventHandler
import com.emarsys.mobileengage.di.mobileEngage
import org.json.JSONObject

data class EmarsysConfig internal constructor(val application: Application,
                                              val mobileEngageApplicationCode: String?,
                                              val contactFieldId: Int,
                                              val predictMerchantId: String?,
                                              @Deprecated("will be removed in 3.0.0")
                                              private val inputInAppEventHandler: com.emarsys.mobileengage.api.event.EventHandler?,
                                              @Deprecated("will be removed in 3.0.0")
                                              private val inputNotificationEventHandler: com.emarsys.mobileengage.api.event.EventHandler?,
                                              val experimentalFeatures: List<FlipperFeature>,
                                              val automaticPushTokenSendingEnabled: Boolean,
                                              val sharedPackageNames: List<String>?,
                                              val sharedSecret: String?,
                                              val verboseConsoleLoggingEnabled: Boolean) {

    @Deprecated("will be removed in 3.0.0")
    val inAppEventHandler: com.emarsys.mobileengage.api.EventHandler?
        get() {
            return if (inputInAppEventHandler == null) {
                null
            } else EventHandler { eventName, payload ->
                val currentActivityProvider = mobileEngage().currentActivityProvider
                val currentActivity = currentActivityProvider.get()
                if (currentActivity != null) {
                    inputInAppEventHandler.handleEvent(currentActivity, eventName, payload)
                }
            }
        }

    @Deprecated("will be removed in 3.0.0")
    val notificationEventHandler: NotificationEventHandler?
        get() {
            return if (inputNotificationEventHandler == null) {
                null
            } else NotificationEventHandler { context, eventName, payload -> inputNotificationEventHandler.handleEvent(context, eventName, payload) }
        }

    class Builder {
        private lateinit var application: Application
        private var mobileEngageApplicationCode: String? = null
        private var contactFieldId: Int = 0
        private var predictMerchantId: String? = null
        private var defaultInAppEventHandler: com.emarsys.mobileengage.api.event.EventHandler? = null
        private var notificationEventHandler: com.emarsys.mobileengage.api.event.EventHandler? = null
        private var experimentalFeatures: List<FlipperFeature>? = null
        private var automaticPushTokenSending = true
        private var sharedSecret: String? = null
        private var sharedPackageNames: List<String>? = null
        private var verboseConsoleLoggingEnabled: Boolean = false
        fun from(baseConfig: EmarsysConfig): Builder {
            application = baseConfig.application
            mobileEngageApplicationCode = baseConfig.mobileEngageApplicationCode
            contactFieldId = baseConfig.contactFieldId
            predictMerchantId = baseConfig.predictMerchantId
            defaultInAppEventHandler = object : com.emarsys.mobileengage.api.event.EventHandler {
                override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
                    baseConfig.inAppEventHandler!!.handleEvent(eventName, payload)
                }
            }
            notificationEventHandler = object : com.emarsys.mobileengage.api.event.EventHandler {
                override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
                    baseConfig.notificationEventHandler!!.handleEvent(context, eventName, payload)
                }
            }
            experimentalFeatures = baseConfig.experimentalFeatures
            automaticPushTokenSending = baseConfig.automaticPushTokenSendingEnabled
            sharedSecret = baseConfig.sharedSecret
            sharedPackageNames = baseConfig.sharedPackageNames
            verboseConsoleLoggingEnabled = baseConfig.verboseConsoleLoggingEnabled
            return this
        }

        fun application(application: Application): Builder {
            this.application = application
            return this
        }

        fun mobileEngageApplicationCode(mobileEngageApplicationCode: String?): Builder {
            this.mobileEngageApplicationCode = mobileEngageApplicationCode
            return this
        }

        fun contactFieldId(contactFieldId: Int): Builder {
            this.contactFieldId = contactFieldId
            return this
        }

        fun predictMerchantId(predictMerchantId: String?): Builder {
            this.predictMerchantId = predictMerchantId
            return this
        }

        fun enableExperimentalFeatures(vararg experimentalFeatures: FlipperFeature): Builder {
            this.experimentalFeatures = listOf(*experimentalFeatures)
            return this
        }

        fun disableAutomaticPushTokenSending(): Builder {
            automaticPushTokenSending = false
            return this
        }

        @Deprecated("will be removed in 3.0.0, use Emarsys.inapp.setEventHandler(EventHandler) instead.")
        fun inAppEventHandler(inAppEventHandler: EventHandler): Builder {
            defaultInAppEventHandler = object : com.emarsys.mobileengage.api.event.EventHandler {
                override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
                    inAppEventHandler.handleEvent(eventName, payload)
                }
            }
            return this
        }

        @Deprecated("will be removed in 3.0.0, use Emarsys.push.setNotificationEventHandler(EventHandler) instead.")
        fun notificationEventHandler(notificationEventHandler: NotificationEventHandler): Builder {
            this.notificationEventHandler = object : com.emarsys.mobileengage.api.event.EventHandler {
                override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
                    notificationEventHandler.handleEvent(context, eventName, payload)
                }
            }
            return this
        }

        fun sharedSecret(sharedSecret: String): Builder {
            this.sharedSecret = sharedSecret
            return this
        }

        fun sharedPackageNames(sharedPackageNames: List<String>): Builder {
            this.sharedPackageNames = sharedPackageNames
            return this
        }

        fun enableVerboseConsoleLogging(): Builder {
            this.verboseConsoleLoggingEnabled = true
            return this
        }

        fun build(): EmarsysConfig {
            experimentalFeatures = if (experimentalFeatures == null) emptyList() else experimentalFeatures
            return EmarsysConfig(
                    application,
                    mobileEngageApplicationCode,
                    contactFieldId,
                    predictMerchantId,
                    defaultInAppEventHandler,
                    notificationEventHandler,
                    experimentalFeatures!!,
                    automaticPushTokenSending,
                    sharedPackageNames,
                    sharedSecret,
                    verboseConsoleLoggingEnabled)
        }
    }
}