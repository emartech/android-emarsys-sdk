package com.emarsys.sample.testutils

import android.content.Context

import androidx.multidex.MultiDex

import com.emarsys.sample.SampleApplication

class InstrumentedApplication : SampleApplication() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
