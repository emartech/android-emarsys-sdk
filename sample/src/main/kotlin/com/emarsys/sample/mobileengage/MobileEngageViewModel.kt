package com.emarsys.sample.mobileengage

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class MobileEngageViewModel : ViewModel() {
    private val customEventField = mutableStateOf("")
    private val payloadField = mutableStateOf("")
    private val errorVisible = mutableStateOf(false)
    private val errorMessage = mutableStateOf("")

    fun getCustomEvent(): String {
        return this.customEventField.value
    }

    fun getCustomEventField(): MutableState<String> {
        return this.customEventField
    }

    fun getPayloadMap(): Map<String, String>? {
        var payload: Map<String, String>? = null
        if (payloadField.value.isNotEmpty()) {
            val mapType: Type = object : TypeToken<Map<String, String>?>() {}.type
            payload = Gson().fromJson(payloadField.value, mapType)
        }

        return payload
    }

    fun getPayloadField(): MutableState<String> {
        return this.payloadField
    }

    fun isEventPresent(): Boolean {
        return if (customEventField.value.isNotEmpty()) {
            true
        } else {
            this.errorMessage.value = "Custom event is required!"
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