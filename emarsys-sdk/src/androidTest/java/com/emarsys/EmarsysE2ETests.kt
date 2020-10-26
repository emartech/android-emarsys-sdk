package com.emarsys

import android.app.Application
import com.emarsys.config.EmarsysConfig
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.IntegrationTestUtils
import com.google.firebase.FirebaseApp
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.CountDownLatch

class EmarsysE2ETests {

    private companion object {
        private const val APP_ID = "14C19-A121F"
        private const val APPLICATION_CODE = "EMS11-C3FD3"
        private const val CONTACT_FIELD_ID = 3
        private const val CONTACT_ID = "test@test.com"

        @BeforeClass
        @JvmStatic
        fun beforeAll() {
            IntegrationTestUtils.initializeFirebase()
        }

        @AfterClass
        @JvmStatic
        fun afterAll() {
            FirebaseApp.clearInstancesForTest()
        }
    }

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys(application)
    }

    @Test
    fun testChangeApplicationCode() {
        Emarsys.setup(EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .build())

        val changeApplicationCodeLatch = CountDownLatch(1)

        Emarsys.config.changeApplicationCode(APPLICATION_CODE) {
            changeApplicationCodeLatch.countDown()
        }

        changeApplicationCodeLatch.await()

        val setContactLatch = CountDownLatch(1)
        Emarsys.setContact(CONTACT_ID) {
            setContactLatch.countDown()
        }

        setContactLatch.await()

        val title = "Android - changeApplicationCode"
        val timestamp = System.currentTimeMillis()
        val trackCustomEventLatch = CountDownLatch(1)
        Emarsys.trackCustomEvent("emarsys-sdk-e2e-test", mapOf(
                "eventName" to title,
                "timestamp" to timestamp.toString()
        )) {
            trackCustomEventLatch.countDown()
        }

        trackCustomEventLatch.await()

        val fetchMessageInboxLatch = CountDownLatch(1)
        Emarsys.messageInbox.fetchMessages { response ->
            response.result
                    ?.messages
                    ?.filter { message -> message.title == title && message.body == timestamp.toString() }
                    ?.size
                    ?: 0 shouldBe 1
            fetchMessageInboxLatch.countDown()
        }

        fetchMessageInboxLatch.await()
    }

    @Test
    fun testChangeApplicationCodeFromNull() {

        Emarsys.setup(EmarsysConfig.Builder()
                .application(application)
                .contactFieldId(CONTACT_FIELD_ID)
                .build())

        val changeApplicationCodeLatch = CountDownLatch(1)
        Emarsys.config.changeApplicationCode(APPLICATION_CODE) {
            changeApplicationCodeLatch.countDown()
        }

        changeApplicationCodeLatch.await()

        val setContactLatch = CountDownLatch(1)
        Emarsys.setContact(CONTACT_ID) {
            setContactLatch.countDown()
        }

        setContactLatch.await()

        val title = "Android - enable ME feature"
        val timestamp = System.currentTimeMillis()
        val trackCustomEventLatch = CountDownLatch(1)
        Emarsys.trackCustomEvent("emarsys-sdk-e2e-test", mapOf(
                "eventName" to title,
                "timestamp" to timestamp.toString()
        )) {
            trackCustomEventLatch.countDown()
        }

        trackCustomEventLatch.await()

        val fetchMessageInboxLatch = CountDownLatch(1)
        Emarsys.messageInbox.fetchMessages { response ->
            response.result
                    ?.messages
                    ?.filter { message -> message.title == title && message.body == timestamp.toString() }
                    ?.size
                    ?: 0 shouldBe 1
            fetchMessageInboxLatch.countDown()
        }

        fetchMessageInboxLatch.await()
    }

    @Test
    fun testChangeApplicationCodeToNull() {
        var disabled = true
        Emarsys.setup(EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APPLICATION_CODE)
                .contactFieldId(CONTACT_FIELD_ID)
                .build())

        val changeApplicationCodeLatch = CountDownLatch(1)
        Emarsys.config.changeApplicationCode(null) {
            changeApplicationCodeLatch.countDown()
        }

        changeApplicationCodeLatch.await()

        Emarsys.setContact(CONTACT_ID) { disabled = false }

        val title = "Android - disable ME feature"
        val timestamp = System.currentTimeMillis()
        Emarsys.trackCustomEvent("emarsys-sdk-e2e-test", mapOf(
                "eventName" to title,
                "timestamp" to timestamp.toString()
        )) { disabled = false }

        Emarsys.messageInbox.fetchMessages { disabled = false }

        disabled shouldBe true
    }
}