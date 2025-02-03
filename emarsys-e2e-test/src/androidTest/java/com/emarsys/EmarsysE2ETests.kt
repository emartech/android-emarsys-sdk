package com.emarsys


import android.app.Application
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.util.RetryUtil
import com.emarsys.mobileengage.api.inbox.Message
import com.emarsys.testUtil.E2ETestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.RetryUtils
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import com.emarsys.testUtil.rules.RetryRule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.After
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class EmarsysE2ETests  {

    @Rule
    @JvmField
    val retryRule: RetryRule = RetryUtils.retryRule

    @Rule
    @JvmField
    val duplicateThreadRule = DuplicatedThreadRule("CoreSDKHandlerThread")

    companion object {
        private const val OLD_APPLICATION_CODE = "14C19-A121F"
        private const val APPLICATION_CODE = "EMS11-C3FD3"
        private const val CONTACT_FIELD_ID = 2575
        private const val CONTACT_FIELD_VALUE = "test@test.com"
        private const val TRIGGER_INBOX_EVENT = "emarsys-sdk-e2e-inbox-test"
        private const val TEST_TAG = "test_tag"
    }

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    @After
    fun tearDown() {
        E2ETestUtils.tearDownEmarsys(application)
    }

    @Test

    fun testChangeApplicationCode() {
        setup(OLD_APPLICATION_CODE)

        changeApplicationCode(APPLICATION_CODE)

        setContact()

        val title = "Android - changeApplicationCode"
        val timestamp = System.currentTimeMillis()

        trackCustomEvent(title, timestamp)
        RetryUtil.retry(10, 2000) {
            val message = fetchMessage(title, timestamp)
            message shouldNotBe null
        }
    }

    @Test
    fun testChangeApplicationCodeFromNull() {
        setup(null)

        changeApplicationCode(APPLICATION_CODE)

        setContact()

        val title = "Android - enable ME feature"
        val timestamp = System.currentTimeMillis()
        trackCustomEvent(title, timestamp)

        RetryUtil.retry(10, 2000) {
            val message = fetchMessage(title, timestamp)
            message shouldNotBe null
        }
    }

    @Test
    fun testChangeApplicationCodeToNull() {
        var disabled = true
        setup(APPLICATION_CODE)

        changeApplicationCode(null)

        Emarsys.setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE) { disabled = false }

        val title = "Android - disable ME feature"
        val timestamp = System.currentTimeMillis()
        Emarsys.trackCustomEvent(
            TRIGGER_INBOX_EVENT, mapOf(
                "eventName" to title,
                "timestamp" to timestamp.toString()
            )
        ) { disabled = false }

        Emarsys.messageInbox.fetchMessages { disabled = false }

        disabled shouldBe true
    }

    @Test
    fun testInbox_addTag_removeTag() {
        setup(APPLICATION_CODE)

        setContact()

        val title = "Android - test Inbox tags"
        val timestamp = System.currentTimeMillis()
        trackCustomEvent(title, timestamp)
        var message: Message? = null
        RetryUtil.retry(10, 2000) {
            message = fetchMessage(title, timestamp)
            message shouldNotBe null
        }
        val messageToTest = message
        if (messageToTest != null) {
            val addMessageTagLatch = CountDownLatch(1)

            Emarsys.messageInbox.addTag(TEST_TAG, messageToTest.id) {
                addMessageTagLatch.countDown()
            }
            addMessageTagLatch.await()

            RetryUtil.retry(10, 2000) {
                val updatedMessage = fetchMessage(title, timestamp)
                updatedMessage shouldNotBe null
                if (updatedMessage != null) {
                    updatedMessage.tags?.contains(TEST_TAG) shouldBe true
                }
            }
            val removeMessageTagLatch = CountDownLatch(1)
            Emarsys.messageInbox.removeTag(TEST_TAG, messageToTest.id) {
                removeMessageTagLatch.countDown()
            }
            removeMessageTagLatch.await()
            RetryUtil.retry(10, 2000) {
                val updatedMessage = fetchMessage(title, timestamp)
                updatedMessage shouldNotBe null
                updatedMessage?.tags?.contains(TEST_TAG) shouldBe false
            }
        }
    }

    private fun changeApplicationCode(appCode: String?) {
        val changeApplicationCodeLatch = CountDownLatch(1)
        Emarsys.config.changeApplicationCode(appCode) {
            changeApplicationCodeLatch.countDown()
        }

        changeApplicationCodeLatch.await()
    }

    private fun trackCustomEvent(title: String, timestamp: Long) {
        val trackCustomEventLatch = CountDownLatch(1)
        Emarsys.trackCustomEvent(
            TRIGGER_INBOX_EVENT, mapOf(
                "eventName" to title,
                "timestamp" to timestamp.toString()
            )
        ) {
            trackCustomEventLatch.countDown()
        }

        trackCustomEventLatch.await()
    }

    private fun setContact() {
        val setContactLatch = CountDownLatch(1)
        Emarsys.setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE) {
            setContactLatch.countDown()
        }

        setContactLatch.await()
    }

    private fun setup(appCode: String?) {
        Emarsys.setup(
            EmarsysConfig.Builder()
                .application(application)
                .applicationCode(appCode)
                .build()
        )
    }

    private fun fetchMessage(title: String, timestamp: Long, timeout: Long = 1000): Message? {
        var message: Message? = null
        Thread.sleep(1000)
        val fetchMessageInboxLatch = CountDownLatch(1)

        Emarsys.messageInbox.fetchMessages { response ->
            message = response.result
                ?.messages
                ?.firstOrNull { message -> message.title == title && message.body == timestamp.toString() }
            fetchMessageInboxLatch.countDown()
        }

        fetchMessageInboxLatch.await(timeout, TimeUnit.MILLISECONDS)
        return message
    }
}