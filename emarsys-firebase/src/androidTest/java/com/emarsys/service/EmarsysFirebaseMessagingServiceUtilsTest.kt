package com.emarsys.service

import android.content.Context
import android.os.Bundle
import com.emarsys.fake.FakeFirebaseDependencyContainer
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.InstrumentationRegistry
import com.google.firebase.messaging.RemoteMessage
import io.kotest.matchers.shouldBe

class EmarsysFirebaseMessagingServiceUtilsTest : AnnotationSpec() {
    private lateinit var context: Context

    @Before
    fun init() {
        context = InstrumentationRegistry.getTargetContext()
        setupMobileEngageComponent(
            FakeFirebaseDependencyContainer()
        )
    }

    @Test
    fun handleMessage_shouldReturnFalse_ifMessage_isNotEmarsysMessage() {
        val testRemoteMessage = RemoteMessage(Bundle.EMPTY)

        EmarsysFirebaseMessagingServiceUtils.handleMessage(
            context,
            testRemoteMessage
        ) shouldBe false
    }

    @Test
    fun handleMessage_shouldReturnTrue_ifMessage_isEmarsysMessage_V1() {
        val testBundle = Bundle()
        testBundle.putString("ems_msg", "true")
        val testRemoteMessage = RemoteMessage(testBundle)

        EmarsysFirebaseMessagingServiceUtils.handleMessage(context, testRemoteMessage) shouldBe true
    }

    @Test
    fun handleMessage_shouldReturnTrue_ifMessage_isEmarsysMessage_V2() {
        val testBundle = Bundle()
        testBundle.putString("ems.version", "testValue")
        val testRemoteMessage = RemoteMessage(testBundle)

        EmarsysFirebaseMessagingServiceUtils.handleMessage(context, testRemoteMessage) shouldBe true
    }
}