package com.emarsys.sample.main.sdkinfo

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

object TopCardViewModel : ViewModel() {
    val expanded = mutableStateOf(false)

    fun getMoreLessText(): String {
        return if (expanded.value) "Show less..." else "Show more..."
    }

    fun toggleCardExpansion() {
        expanded.value = !expanded.value
    }
}
