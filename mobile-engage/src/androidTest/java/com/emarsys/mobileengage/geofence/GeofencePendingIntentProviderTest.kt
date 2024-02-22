package com.emarsys.mobileengage.geofence

import android.app.PendingIntent
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class GeofencePendingIntentProviderTest {
    @Test
    fun testProvidePendingIntent() {
        val result = GeofencePendingIntentProvider(InstrumentationRegistry.getInstrumentation().context).providePendingIntent()

        result shouldNotBe null
        result::class.java shouldBe PendingIntent::class.java
    }
}