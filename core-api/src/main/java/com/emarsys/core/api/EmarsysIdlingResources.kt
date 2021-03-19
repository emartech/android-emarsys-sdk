package com.emarsys.core.api

import androidx.test.espresso.idling.CountingIdlingResource


object EmarsysIdlingResources {

    private const val RESOURCE = "EMARSYS-SDK"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    @JvmStatic
    fun increment() {
        countingIdlingResource.increment()
    }

    @JvmStatic
    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}