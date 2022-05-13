package com.emarsys.sample.inapp

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.util.*

class InAppViewModel : ViewModel() {
    private val isDoNotDisturb = mutableStateOf(false)
    private val customEventField = mutableStateOf("")
    val viewIdField = mutableStateOf("")
    val viewIdsWithUuid = mutableStateMapOf<UUID, String>()
    val closedInApps = mutableListOf<UUID>()
    private val errorVisible = mutableStateOf(false)
    private val errorMessage = mutableStateOf("")

    fun isDnd(): Boolean {
        return isDoNotDisturb.value
    }

    fun switchDnd() {
        isDoNotDisturb.value = !isDoNotDisturb.value
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


    fun getCustomEvent(): String {
        return this.customEventField.value
    }

    fun getCustomEventField(): MutableState<String> {
        return this.customEventField
    }

    fun addViewIdToMap() {
        val uuid = UuidProvider.provide()
        viewIdsWithUuid.putIfAbsent(uuid, this.viewIdField.value)
    }

    fun removeInAppFromMap() {
        closedInApps.forEach {
            viewIdsWithUuid.remove(it)
        }
    }

    fun isViewIdPresent():Boolean {
        return if (this.viewIdField.value.isNotEmpty()) {
            true
        } else {
            this.errorMessage.value = "ViewId is required!"
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