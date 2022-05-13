package com.emarsys.sample.mobileengage

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class MobileEngageViewModel() : ViewModel() {
    private val objectMapper = ObjectMapper().registerKotlinModule()
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
            payload = try {
                objectMapper.readValue<Map<String, String>>(this.payloadField.value)
            } catch (error: JsonParseException) {
                errorMessage.value = "JSON parsing error!"
                errorVisible.value = true
                null
            }
        }
        return payload
    }

    fun getPayloadField(): MutableState<String> {
        return this.payloadField
    }

    fun isEventPresent():Boolean {
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