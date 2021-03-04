package com.emarsys.service

import android.app.Application
import com.emarsys.Emarsys
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.push.PushApi
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.stubbing.Answer
import java.util.concurrent.CountDownLatch


class EmarsysMessagingServiceTest {

    private lateinit var mockHardwareIdProvider: HardwareIdProvider
    private lateinit var mockLanguageProvider: LanguageProvider
    private lateinit var mockVersionProvider: VersionProvider
    private lateinit var mockNotificationSettings: NotificationSettings

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private companion object {
        private const val APPLICATION_CODE = "EMS11-C3FD3"
        private const val CONTACT_FIELD_ID = 3
        private const val HARDWARE_ID = "hwid"
        private const val SDK_VERSION = "sdkVersion"
        private const val LANGUAGE = "en-US"
    }

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    private lateinit var mockPush: PushApi
    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var fakeDependencyContainer: FakeDependencyContainer
    private lateinit var mockCoreSdkHandler: CoreSdkHandler
    private lateinit var baseConfig: EmarsysConfig
    val latch = CountDownLatch(1)

    @Before
    fun setUp() {
        mockPush = mock()

        mockRequestContext = mock {
            on { applicationCode } doReturn APPLICATION_CODE
        }

        mockHardwareIdProvider = mock {
            on { provideHardwareId() } doReturn HARDWARE_ID
        }
        mockLanguageProvider = mock {
            on { provideLanguage(any()) } doReturn LANGUAGE
        }
        mockVersionProvider = mock {
            on { provideSdkVersion() } doReturn SDK_VERSION
        }
        mockNotificationSettings = mock()

        baseConfig = createConfig()

        mockCoreSdkHandler = mock {
            on { post(any()) } doAnswer Answer<Any?> { invocation ->
                invocation.getArgument<Runnable>(0).run()
                null
            }
        }

        FeatureTestUtils.resetFeatures()
    }

    @After
    fun tearDown() {
        application.unregisterActivityLifecycleCallbacks(getDependency<ActivityLifecycleWatchdog>())
        application.unregisterActivityLifecycleCallbacks(getDependency<CurrentActivityWatchdog>())
        DependencyInjection.tearDown()
        FeatureTestUtils.resetFeatures()
    }

    @Test
    fun testOnNewToken_whenIsAutomaticPushSendingEnabledIsTrue_callsSetPushToken() {
        setupEmarsys(true)

        EmarsysMessagingService().onNewToken("testToken")

        verify(mockPush, timeout(100)).pushToken = "testToken"
    }

    @Test
    fun testOnNewToken_whenIsAutomaticPushSendingEnabledIsTrue_callsSetPushToken_onCoreSdkThread() {
        setupEmarsys(true)

        EmarsysMessagingService().onNewToken("testToken")

        verify(mockCoreSdkHandler, timeout(1000).times(2)).post(any())
    }

    @Test
    fun testOnNewToken_whenIsAutomaticPushSendingEnabledIsFalse_doesNotCallSetPushToken() {
        setupEmarsys(false)

        EmarsysMessagingService().onNewToken("testToken")

        verify(mockPush, times(0)).pushToken = "testToken"
    }

    private fun createConfig(): EmarsysConfig {
        val builder = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APPLICATION_CODE)
                .contactFieldId(CONTACT_FIELD_ID)
        return builder.build()
    }

    private fun setupEmarsys(isAutomaticPushSending: Boolean) {
        val deviceInfo = DeviceInfo(
                application,
                mockHardwareIdProvider,
                mockVersionProvider,
                mockLanguageProvider,
                mockNotificationSettings,
                isAutomaticPushSending
        )

        fakeDependencyContainer = FakeDependencyContainer(
                coreSdkHandler = mockCoreSdkHandler,
                deviceInfo = deviceInfo,
                requestContext = mockRequestContext,
                push = mockPush
        )

        DependencyInjection.setup(fakeDependencyContainer)

        Emarsys.setup(baseConfig)
    }

}
