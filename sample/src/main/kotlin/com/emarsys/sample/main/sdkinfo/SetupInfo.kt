package com.emarsys.sample.main.sdkinfo

import androidx.compose.runtime.mutableStateMapOf
import com.emarsys.Emarsys
import com.emarsys.sample.pref.Prefs
import java.util.*

object SetupInfo : Observable() {
    var loggedIn: Boolean = Prefs.loggedIn
    var hardwareId: String = Emarsys.config.hardwareId
    var languageCode: String = Emarsys.config.languageCode
    var sdkVersion: String = Emarsys.config.sdkVersion
    var contactFieldValue: String = Prefs.contactFieldValue
    var contactFieldId: String = Prefs.contactFieldId.toString()
    var applicationCode: String = Prefs.applicationCode
    var merchantId: String = Prefs.merchantId

    private val observers = listOf(TopCardViewModel, Prefs)

    override fun addObserver(o: Observer?) {
        super.addObserver(o)
    }

    override fun notifyObservers() {
        observers.forEach { observer ->
            observer.update(this, provideInfoMap())
        }
    }

    fun provideInfoMap(): MutableMap<String, String> {
        return mutableStateMapOf(
            "LoggedIn" to loggedIn.toString(),
            "ApplicationCode" to applicationCode,
            "MerchantId" to merchantId,
            "HardwareId" to hardwareId,
            "LanguageCode" to languageCode,
            "SdkVersion" to sdkVersion,
            "ContactFieldValue" to contactFieldValue,
            "ContactFieldId" to contactFieldId
        )
    }
}