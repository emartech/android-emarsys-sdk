package com.emarsys.config

import android.app.Application

class ConfigLoader  {

    fun loadConfigFromSharedPref(application: Application, sharedPreferenceSource: String): EmarsysConfig.Builder {
        val sharedPreferences = application.getSharedPreferences(sharedPreferenceSource, 0)
        val appCode =
                sharedPreferences.getString(ConfigStorageKeys.MOBILE_ENGAGE_APPLICATION_CODE.name, null)
        val merchantId =
                sharedPreferences.getString(ConfigStorageKeys.PREDICT_MERCHANT_ID.name, null)
        val disableAutomaticPushSending = sharedPreferences.getBoolean(
                ConfigStorageKeys.ANDROID_DISABLE_AUTOMATIC_PUSH_TOKEN_SENDING.name,
                false
        )
        val sharedPackages = sharedPreferences.getStringSet(
                ConfigStorageKeys.ANDROID_SHARED_PACKAGE_NAMES.name,
                mutableSetOf()
        )
        val secret = sharedPreferences.getString(ConfigStorageKeys.ANDROID_SHARED_SECRET.name, null)
        val enableVerboseLogging = sharedPreferences.getBoolean(
                ConfigStorageKeys.ANDROID_VERBOSE_CONSOLE_LOGGING_ENABLED.name,
                false
        )

        val builder = EmarsysConfig.Builder()
                .application(application)
                .applicationCode(appCode)
                .merchantId(merchantId)
        if (disableAutomaticPushSending) {
            builder.disableAutomaticPushTokenSending()
        }
        if (enableVerboseLogging) {
            builder.enableVerboseConsoleLogging()
        }
        if (!sharedPackages.isNullOrEmpty()) {
            builder.sharedPackageNames(sharedPackages.toList())
        }
        if (secret != null) {
            builder.sharedSecret(secret)
        }
        return builder
    }
}