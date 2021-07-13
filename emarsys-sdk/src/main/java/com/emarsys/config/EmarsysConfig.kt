package com.emarsys.config

import android.app.Application
import com.emarsys.core.api.experimental.FlipperFeature

data class EmarsysConfig constructor(val application: Application,
                                     val applicationCode: String? = null,
                                     val contactFieldId: Int,
                                     val merchantId: String? = null,
                                     val experimentalFeatures: List<FlipperFeature> = listOf(),
                                     val automaticPushTokenSendingEnabled: Boolean = true,
                                     val sharedPackageNames: List<String>? = null,
                                     val sharedSecret: String? = null,
                                     val verboseConsoleLoggingEnabled: Boolean = false) {

    @Deprecated(message = "Deprecated in Kotlin, please use the EmarsysConfig property based constructor.")
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

        @Deprecated(message = "Deprecated in Kotlin, please use the EmarsysConfig property based constructor.")
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

        @Deprecated(message = "Deprecated in Kotlin, please use the EmarsysConfig property based constructor.")
        fun application(application: Application): Builder {
            this.application = application
            return this
        }

        @Deprecated(message = "Deprecated in Kotlin, please use the EmarsysConfig property based constructor.")
        fun applicationCode(mobileEngageApplicationCode: String?): Builder {
            this.applicationCode = mobileEngageApplicationCode
            return this
        }

        @Deprecated(message = "Deprecated in Kotlin, please use the EmarsysConfig property based constructor.")
        fun contactFieldId(contactFieldId: Int): Builder {
            this.contactFieldId = contactFieldId
            return this
        }

        @Deprecated(message = "Deprecated in Kotlin, please use the EmarsysConfig property based constructor.")
        fun merchantId(predictMerchantId: String?): Builder {
            this.merchantId = predictMerchantId
            return this
        }

        @Deprecated(message = "Deprecated in Kotlin, please use the EmarsysConfig property based constructor.")
        fun enableExperimentalFeatures(vararg experimentalFeatures: FlipperFeature): Builder {
            this.experimentalFeatures = listOf(*experimentalFeatures)
            return this
        }

        @Deprecated(message = "Deprecated in Kotlin, please use the EmarsysConfig property based constructor.")
        fun disableAutomaticPushTokenSending(): Builder {
            automaticPushTokenSending = false
            return this
        }

        @Deprecated(message = "Deprecated in Kotlin, please use the EmarsysConfig property based constructor.")
        fun sharedSecret(sharedSecret: String): Builder {
            this.sharedSecret = sharedSecret
            return this
        }

        @Deprecated(message = "Deprecated in Kotlin, please use the EmarsysConfig property based constructor.")
        fun sharedPackageNames(sharedPackageNames: List<String>): Builder {
            this.sharedPackageNames = sharedPackageNames
            return this
        }

        @Deprecated(message = "Deprecated in Kotlin, please use the EmarsysConfig property based constructor.")
        fun enableVerboseConsoleLogging(): Builder {
            this.verboseConsoleLoggingEnabled = true
            return this
        }

        @Deprecated(message = "Deprecated in Kotlin, please use the EmarsysConfig property based constructor.")
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