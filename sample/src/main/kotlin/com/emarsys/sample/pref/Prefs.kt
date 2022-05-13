package com.emarsys.sample.pref

import com.chibatching.kotpref.KotprefModel
import java.util.*

object Prefs : KotprefModel(), Observer {

    var sdkVersion by stringPref("")
    var languageCode by stringPref("")
    var hardwareId by stringPref("")
    var applicationCode by stringPref("")
    var merchantId by stringPref("")
    var contactFieldValue by stringPref("")
    var contactFieldId by intPref(0)
    var loggedIn by booleanPref(false)

    override fun update(o: Observable?, arg: Any?) {
        val newSettings = arg as MutableMap<String, String>

        loggedIn = newSettings["LoggedIn"]!!.toBoolean()
        applicationCode = newSettings["ApplicationCode"]!!
        merchantId = newSettings["MerchantId"]!!
        hardwareId = newSettings["HardwareId"]!!
        languageCode = newSettings["LanguageCode"]!!
        sdkVersion = newSettings["SdkVersion"]!!
        contactFieldValue = newSettings["ContactFieldValue"]!!
        contactFieldId = newSettings["ContactFieldId"]!!.toInt()
    }
}