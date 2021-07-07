package com.emarsys.config

import android.app.Application
import com.emarsys.core.api.experimental.FlipperFeature

data class EmarsysConfig internal constructor(val application: Application,
                                              val applicationCode: String?,
                                              val contactFieldId: Int,
                                              val merchantId: String?,
                                              val experimentalFeatures: List<FlipperFeature>,
                                              val automaticPushTokenSendingEnabled: Boolean,
                                              val sharedPackageNames: List<String>?,
                                              val sharedSecret: String?,
                                              val verboseConsoleLoggingEnabled: Boolean) {

    class Builder {
        private lateinit var application: Application
        private var applicationCode: String? = null
        private var contactFieldId: Int = 0
        private var merchantId: String? = null
        private var experimentalFeatures: List<FlipperFeature>? = null
        private var automaticPushTokenSending = true
        private var sharedSecret: String? = null
        private var sharedPackageNames: List<String>? = null
        private var verboseConsoleLoggingEnabled: Boolean = false
        fun from(baseConfig: EmarsysConfig): Builder {
            application = baseConfig.application
            applicationCode = baseConfig.applicationCode
            contactFieldId = baseConfig.contactFieldId
            merchantId = baseConfig.merchantId
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

        fun applicationCode(mobileEngageApplicationCode: String?): Builder {
            this.applicationCode = mobileEngageApplicationCode
            return this
        }

        fun contactFieldId(contactFieldId: Int): Builder {
            this.contactFieldId = contactFieldId
            return this
        }

        fun merchantId(predictMerchantId: String?): Builder {
            this.merchantId = predictMerchantId
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
                    applicationCode,
                    contactFieldId,
                    merchantId,
                    experimentalFeatures!!,
                    automaticPushTokenSending,
                    sharedPackageNames,
                    sharedSecret,
                    verboseConsoleLoggingEnabled)
        }
    }
}