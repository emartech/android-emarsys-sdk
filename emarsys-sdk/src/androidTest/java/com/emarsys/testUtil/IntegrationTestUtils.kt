package com.emarsys.testUtil

import com.emarsys.BuildConfig
import com.emarsys.Emarsys
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.kotlintest.shouldBe
import java.util.concurrent.CountDownLatch

object IntegrationTestUtils {

    @JvmStatic
    fun initializeFirebase() {
        val apiKey = BuildConfig.GOOGLE_SERVICES_API_KEY
        val options: FirebaseOptions = FirebaseOptions.Builder()
                .setProjectId("ems-mobile-engage-android-app")
                .setApiKey(apiKey)
                .setApplicationId("1:1014228643013:android:dee9098abac0567e")
                .build()

        FirebaseApp.initializeApp(InstrumentationRegistry.getTargetContext(), options)
    }

    fun doLogin(contactFieldValue: String = "test@test.com", pushToken: String = "integration_test_push_token") {
        val latchForPushToken = CountDownLatch(2)
        var errorCause: Throwable? = null
        Emarsys.push.setPushToken(pushToken) { throwable ->
            errorCause = throwable
            latchForPushToken.countDown()
            Emarsys.setContact(contactFieldValue) {
                errorCause = it
                latchForPushToken.countDown()
            }
        }
        latchForPushToken.await()
        errorCause shouldBe null
    }
}