package com.emarsys.testUtil

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation

class InstrumentationRegistry {
    companion object {
        @JvmStatic
        fun getTargetContext(): Context = getInstrumentation().targetContext

    }
}