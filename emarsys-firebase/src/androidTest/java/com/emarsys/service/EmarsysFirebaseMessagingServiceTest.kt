package com.emarsys.service

import android.app.Application
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.fake.FakeFirebaseDependencyContainer
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.*
import org.mockito.stubbing.Answer
import java.util.concurrent.CountDownLatch


class EmarsysFirebaseMessagingServiceTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    private lateinit var mockPushInternal: PushInternal
    private lateinit var fakeDependencyContainer: FakeFirebaseDependencyContainer
    private lateinit var mockCoreSdkHandler: CoreSdkHandler
    val latch = CountDownLatch(1)

    @Before
    fun setUp() {
        mockPushInternal = mock()

        mockCoreSdkHandler = mock {
            on { post(any()) } doAnswer Answer<Any?> { invocation ->
                invocation.getArgument<Runnable>(0).run()
                null
            }
        }
    }

    @After
    fun tearDown() {
        tearDownMobileEngageComponent()
        FeatureTestUtils.resetFeatures()
    }

    @Test
    fun testOnNewToken_whenIsAutomaticPushSendingEnabledIsTrue_callsSetPushToken() {
        setupEmarsys(true)

        EmarsysFirebaseMessagingService().onNewToken("testToken")

        verify(mockPushInternal, timeout(100)).setPushToken("testToken", null)
    }

    @Test
    fun testOnNewToken_whenIsAutomaticPushSendingEnabledIsTrue_callsSetPushToken_onCoreSdkThread() {
        setupEmarsys(true)

        EmarsysFirebaseMessagingService().onNewToken("testToken")

        verify(mockCoreSdkHandler, timeout(1000).times(1)).post(any())
    }

    @Test
    fun testOnNewToken_whenIsAutomaticPushSendingEnabledIsFalse_doesNotCallSetPushToken() {
        setupEmarsys(false)

        EmarsysFirebaseMessagingService().onNewToken("testToken")

        verify(mockPushInternal, times(0)).setPushToken("testToken", null)
    }

    private fun setupEmarsys(isAutomaticPushSending: Boolean) {
        val deviceInfo = DeviceInfo(
                application,
                mock {
                    on { provideHardwareId() } doReturn "hardwareId"
                },
                mock {
                    on { provideSdkVersion() } doReturn "version"
                },
                mock {
                    on { provideLanguage(any()) } doReturn "language"
                },
                mock(),
                isAutomaticPushSending,
                true
        )

        fakeDependencyContainer = FakeFirebaseDependencyContainer(
            coreSdkHandler = mockCoreSdkHandler,
            deviceInfo = deviceInfo,
            pushInternal = mockPushInternal
        )

        setupMobileEngageComponent(fakeDependencyContainer)
    }
}
