package com.emarsys.testUtil

import android.content.Context
import android.support.test.InstrumentationRegistry

object SharedPrefsUtils {

    @JvmStatic
    @JvmOverloads
    fun clearSharedPrefs(namespace: String, mode: Int = Context.MODE_PRIVATE) =
            InstrumentationRegistry
                    .getTargetContext()
                    .getSharedPreferences(namespace, mode)
                    .edit()
                    .clear()
                    .commit()

}