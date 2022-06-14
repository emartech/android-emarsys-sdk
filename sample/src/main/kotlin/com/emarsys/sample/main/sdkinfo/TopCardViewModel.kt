package com.emarsys.sample.main.sdkinfo

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.util.*

object TopCardViewModel : ViewModel(), Observer {
    val setupInfo = mutableStateOf(SetupInfo.provideInfoMap())
    val expanded = mutableStateOf(false)

    fun getMoreLessText(): String {
        return if (expanded.value) "Show less..." else "Show more..."
    }

    fun toggleCardExpansion() {
        expanded.value = !expanded.value
    }

    override fun update(o: Observable?, arg: Any?) {
        setupInfo.value = arg as MutableMap<String, String>
    }
}
