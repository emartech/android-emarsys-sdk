package com.emarsys.sample.pref

import com.chibatching.kotpref.KotprefModel

object Prefs : KotprefModel() {
    override val commitAllPropertiesByDefault: Boolean = true

    var sdkVersion by stringPref("")
    var languageCode by stringPref("")
    var clientId by stringPref("")
    var applicationCode by stringPref("")
    var merchantId by stringPref("")
    var contactFieldValue by stringPref("")
    var contactFieldId by intPref(0)
    var loggedIn by booleanPref(false)
}

fun Prefs.update(sdkInfo: Map<String, String>) {
    sdkVersion = sdkInfo.getOrDefault("sdkVersion", "")
    languageCode = sdkInfo.getOrDefault("languageCode", "")
    clientId = sdkInfo.getOrDefault("clientId", "")
    applicationCode = sdkInfo.getOrDefault("applicationCode", "")
    merchantId = sdkInfo.getOrDefault("merchantId", "")
    contactFieldValue = sdkInfo.getOrDefault("contactFieldValue", "")
    contactFieldId = sdkInfo.getOrDefault("contactFieldId", "0").toInt()
    loggedIn = sdkInfo.getOrDefault("loggedIn", "false").toBoolean()
}