package com.emarsys.config

import android.app.Application
import com.emarsys.core.api.experimental.FlipperFeature

data class EmarsysConfig(val application: Application,
                                     val applicationCode: String? = null,
                                     val merchantId: String? = null,
                                     val experimentalFeatures: List<FlipperFeature> = listOf(),
                                     val automaticPushTokenSendingEnabled: Boolean = true,
                                     val sharedPackageNames: List<String>? = null,
                                     val sharedSecret: String? = null,
                                     val verboseConsoleLoggingEnabled: Boolean = false) {

    class Builder {
        private lateinit var application: Application
        private var applicationCode: String? = null
        private var merchantId: String? = null
        private var experimentalFeatures: List<FlipperFeature>? = null
        private var automaticPushTokenSending = true
        private var sharedSecret: String? = null
        private var sharedPackageNames: List<String>? = null
        private var verboseConsoleLoggingEnabled: Boolean = false

        fun from(baseConfig: EmarsysConfig): Builder {
            application = baseConfig.application
            applicationCode = baseConfig.applicationCode
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
                    merchantId,
                    experimentalFeatures!!,
                    automaticPushTokenSending,
                    sharedPackageNames,
                    sharedSecret,
                    verboseConsoleLoggingEnabled)
        }
    }
}