package com.emarsys.sample.dashboard

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.emarsys.Emarsys
import com.emarsys.sample.pref.Prefs
import com.emarsys.sample.pref.update

class DashboardViewModel : ViewModel() {

    val tfAppCode = mutableStateOf(Prefs.applicationCode)
    val tfMerchantId = mutableStateOf(Prefs.merchantId)
    val tfContactFieldId = mutableStateOf(Prefs.contactFieldId.toString())
    val tfContactFieldValue = mutableStateOf(Prefs.contactFieldValue)
    val isLoggedIn = mutableStateOf(Prefs.loggedIn)
    val envChangeChecked = mutableStateOf(false)
    val geofenceEnabled = mutableStateOf(Emarsys.geofence.isEnabled())
    private val errorVisible = mutableStateOf(false)
    private val errorMessage = mutableStateOf("")

    fun getTfAppCodeValue(): String {
        return this.tfAppCode.value
    }

    fun getTfMerchantIdValue(): String {
        return this.tfMerchantId.value
    }

    fun getTfContactFieldIdValue(): String {
        return this.tfContactFieldId.value
    }

    fun getTfContactFieldValue(): String {
        return this.tfContactFieldValue.value
    }

    fun hasLogin(): Boolean {
        return this.isLoggedIn.value
    }

    fun isMerchantIdPresent(): Boolean {
        return if (
            this.getTfMerchantIdValue().isNotEmpty()
        ) {
            true
        } else {
            this.errorMessage.value = "Merchant Id is required!"
            this.errorVisible.value = true
            false
        }
    }

    fun shouldChangeEnv(): Boolean {
        return this.envChangeChecked.value
    }

    fun isGeofenceEnabled(): Boolean {
        return this.geofenceEnabled.value
    }

    fun isContactDataPresent(): Boolean {
        return if (
            this.getTfContactFieldValue().isNotEmpty() &&
            this.getTfContactFieldIdValue().isNotEmpty() &&
            this.getTfContactFieldIdValue().toInt() != 0
        ) {
            true
        } else {
            this.errorMessage.value = "Contact info is required!"
            this.errorVisible.value = true
            false
        }
    }

    fun getErrorMessage(): String {
        return this.errorMessage.value
    }

    fun getErrorVisibleField(): MutableState<Boolean> {
        return this.errorVisible
    }

    fun isErrorVisible(): Boolean {
        return this.errorVisible.value
    }

    fun getInfoMap(): Map<String, String> {
        return mapOf(
            "loggedIn" to isLoggedIn.value.toString(),
            "applicationCode" to tfAppCode.value,
            "merchantId" to tfMerchantId.value,
            "clientId" to Prefs.clientId,
            "languageCode" to Prefs.languageCode,
            "sdkVersion" to Prefs.sdkVersion,
            "contactFieldValue" to tfContactFieldValue.value,
            "contactFieldId" to tfContactFieldId.value
        )
    }

    fun clearContact() {
        tfContactFieldId.value = "0"
        tfContactFieldValue.value = ""
        isLoggedIn.value = false
        Prefs.update(getInfoMap())
    }

    fun setContact(fieldId: String, fieldValue: String) {
        tfContactFieldId.value = fieldId
        tfContactFieldValue.value = fieldValue
        isLoggedIn.value = true
        Prefs.update(getInfoMap())
    }
}