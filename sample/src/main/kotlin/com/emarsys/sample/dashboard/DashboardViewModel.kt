package com.emarsys.sample.dashboard

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.emarsys.Emarsys
import com.emarsys.sample.pref.Prefs

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

    fun setTfContactFieldId(value: String) {
        this.tfContactFieldId.value = value
    }

    fun getTfContactFieldValue(): String {
        return this.tfContactFieldValue.value
    }

    fun setTfContactField(value: String) {
        this.tfContactFieldValue.value = value
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

    fun resetContactInfo() {
        this.setTfContactField("")
        this.setTfContactFieldId("0")
        this.isLoggedIn.value = false
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
}