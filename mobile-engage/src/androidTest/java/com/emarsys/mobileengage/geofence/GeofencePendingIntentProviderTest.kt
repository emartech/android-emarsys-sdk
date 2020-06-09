package com.emarsys.mobileengage.geofence

import android.app.PendingIntent
import androidx.test.platform.app.InstrumentationRegistry
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test

class GeofencePendingIntentProviderTest {
    @Test
    fun testProvidePendingIntent() {
        val result = GeofencePendingIntentProvider(InstrumentationRegistry.getInstrumentation().context).providePendingIntent()

        result shouldNotBe null
        result::class.java shouldBe PendingIntent::class.java
    }
}