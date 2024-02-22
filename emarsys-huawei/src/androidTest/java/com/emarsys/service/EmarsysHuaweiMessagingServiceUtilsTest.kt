package com.emarsys.service

import android.content.Context
import android.os.Bundle
import com.emarsys.fake.FakeHuaweiDependencyContainer
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.testUtil.InstrumentationRegistry
import com.huawei.hms.push.RemoteMessage
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EmarsysHuaweiMessagingServiceUtilsTest {
    private lateinit var context: Context

    @BeforeEach
    fun init() {
        context = InstrumentationRegistry.getTargetContext()
        setupMobileEngageComponent(
            FakeHuaweiDependencyContainer()
        )
    }

    @Test
    fun handleMessage_shouldReturnFalse_ifMessage_isNotEmarsysMessage() {
        val testRemoteMessage = RemoteMessage(Bundle.EMPTY)

        EmarsysHuaweiMessagingServiceUtils.handleMessage(context, testRemoteMessage)
    }

    @Test
    fun handleMessage_shouldReturnTrue_ifMessage_isEmarsysMessage_V1() {
        val testMessage = RemoteMessage.Builder("a").addData("ems_msg", "true").build()

        EmarsysHuaweiMessagingServiceUtils.handleMessage(context, testMessage) shouldBe true
    }

    @Test
    fun handleMessage_shouldReturnTrue_ifMessage_isEmarsysMessage_V2() {
        val testMessage = RemoteMessage.Builder("a").addData("ems.version", "testValue").build()

        EmarsysHuaweiMessagingServiceUtils.handleMessage(context, testMessage) shouldBe true
    }
}