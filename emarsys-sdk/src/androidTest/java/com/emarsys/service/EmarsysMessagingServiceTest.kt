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
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.push.PushApi
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.*
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.util.*


class EmarsysMessagingServiceTest {

    private lateinit var mockHardwareIdProvider: HardwareIdProvider
    private lateinit var mockLanguageProvider: LanguageProvider
    private lateinit var mockVersionProvider: VersionProvider
    private lateinit var mockNotificationSettings: NotificationSettings

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private companion object {
        private const val APPLICATION_CODE = "56789876"
        private const val CONTACT_FIELD_ID = 3
        private const val HARDWARE_ID = "hwid"
        private const val SDK_VERSION = "sdkVersion"
        private const val LANGUAGE = "en-US"

        @BeforeClass
        @JvmStatic
        fun beforeAll() {
            val options: FirebaseOptions = FirebaseOptions.Builder()
                    .setApplicationId("com.emarsys.sdk")
                    .build()

            FirebaseApp.initializeApp(InstrumentationRegistry.getTargetContext(), options)
        }

        @AfterClass
        @JvmStatic
        fun afterAll() {
            FirebaseApp.clearInstancesForTest()
        }
    }

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    private lateinit var mockPush: PushApi
    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var fakeDependencyContainer: FakeDependencyContainer

    private lateinit var baseConfig: EmarsysConfig

    @Before
    fun setUp() {
        mockPush = mock(PushApi::class.java)

        mockRequestContext = mock(MobileEngageRequestContext::class.java).apply {
            whenever(applicationCode).thenReturn(APPLICATION_CODE)
        }

        mockHardwareIdProvider = mock(HardwareIdProvider::class.java)
        mockLanguageProvider = mock(LanguageProvider::class.java)
        mockVersionProvider = mock(VersionProvider::class.java)
        mockNotificationSettings = mock(NotificationSettings::class.java)
        whenever(mockHardwareIdProvider.provideHardwareId()).thenReturn(HARDWARE_ID)
        whenever(mockLanguageProvider.provideLanguage(ArgumentMatchers.any(Locale::class.java))).thenReturn(LANGUAGE)
        whenever(mockVersionProvider.provideSdkVersion()).thenReturn(SDK_VERSION)

        baseConfig = createConfig()
        FeatureTestUtils.resetFeatures()
        DependencyInjection.tearDown()
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
                mockHardwareIdProvider,
                mockVersionProvider,
                mockLanguageProvider,
                mockNotificationSettings,
                isAutomaticPushSending)

        fakeDependencyContainer = FakeDependencyContainer(
                deviceInfo = deviceInfo,
                requestContext = mockRequestContext,
                push = mockPush)

        DependencyInjection.setup(fakeDependencyContainer)

        Emarsys.setup(baseConfig)
    }

}
