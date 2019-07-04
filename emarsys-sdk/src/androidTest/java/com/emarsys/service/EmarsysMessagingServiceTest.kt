package com.emarsys.service

import android.app.Application
import com.emarsys.Emarsys
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.notification.NotificationSettings
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.push.PushApi
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.*


class EmarsysMessagingServiceTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private companion object {
        private const val APPLICATION_CODE = "56789876"
        private const val CONTACT_FIELD_ID = 3
    }

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    private lateinit var mockPush: PushApi
    private lateinit var fakeDependencyContainer: FakeDependencyContainer

    private lateinit var baseConfig: EmarsysConfig

    @Before
    fun setUp() {
        mockPush = mock(PushApi::class.java)
        baseConfig = createConfig()
        FeatureTestUtils.resetFeatures()
        DependencyInjection.tearDown()
    }

    @After
    fun tearDown() {
        DependencyInjection.tearDown()
        FeatureTestUtils.resetFeatures()
    }

    @Test
    fun testOnNewToken_whenIsAutomaticPushSendingEnabledIsTrue_callsSetPushToken() {
        setupEmarsys(true)

        EmarsysMessagingService().onNewToken("testToken")

        verify(mockPush).setPushToken("testToken")
    }

    @Test
    fun testOnNewToken_whenIsAutomaticPushSendingEnabledIsFalse_doesNotCallSetPushToken() {
        setupEmarsys(false)

        EmarsysMessagingService().onNewToken("testToken")

        verify(mockPush, never()).setPushToken("testToken")
    }

    private fun createConfig(): EmarsysConfig {
        val builder = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APPLICATION_CODE)
                .contactFieldId(CONTACT_FIELD_ID)
        return builder.build()
    }

    private fun setupEmarsys(isAutomaticPushSending: Boolean) {
        val deviceInfo = DeviceInfo(application,
                mock(HardwareIdProvider::class.java),
                mock(VersionProvider::class.java),
                mock(LanguageProvider::class.java),
                mock(NotificationSettings::class.java),
                isAutomaticPushSending)

        fakeDependencyContainer = FakeDependencyContainer(
                deviceInfo = deviceInfo,
                push = mockPush)

        DependencyInjection.setup(fakeDependencyContainer)

        Emarsys.setup(baseConfig)
    }

}
