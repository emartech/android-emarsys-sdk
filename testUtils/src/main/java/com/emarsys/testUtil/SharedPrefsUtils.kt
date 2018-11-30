package com.emarsys.testUtil

import android.content.Context

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