package com.emarsys.mobileengage.geofence

import android.app.PendingIntent
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class GeofencePendingIntentProviderTest : AnnotationSpec() {
    @Test
    fun testProvidePendingIntent() {
        val result =
            GeofencePendingIntentProvider(InstrumentationRegistry.getInstrumentation().context).providePendingIntent()

        result shouldNotBe null
        result::class.java shouldBe PendingIntent::class.java
    }
}