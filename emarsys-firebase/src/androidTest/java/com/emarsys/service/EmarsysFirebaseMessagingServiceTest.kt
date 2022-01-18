package com.emarsys.service

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.handler.SdkHandler
import com.emarsys.fake.FakeFirebaseDependencyContainer
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.TimeoutUtils
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.*


class EmarsysFirebaseMessagingServiceTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    private lateinit var mockPushInternal: PushInternal
    private lateinit var fakeDependencyContainer: FakeFirebaseDependencyContainer
    private lateinit var concurrentHandlerHolderFactory: ConcurrentHandlerHolderFactory
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var spyCoreHandler: SdkHandler

    @Before
    fun setUp() {
        mockPushInternal = mock()
        concurrentHandlerHolderFactory =
            ConcurrentHandlerHolderFactory(Handler(Looper.getMainLooper()))
        concurrentHandlerHolder = concurrentHandlerHolderFactory.create()
        spyCoreHandler = spy(concurrentHandlerHolder.coreHandler)
        ReflectionTestUtils.setInstanceField(
            concurrentHandlerHolder,
            "coreHandler",
            spyCoreHandler
        )
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

        verify(spyCoreHandler, timeout(1000).times(1)).post(any())
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
            concurrentHandlerHolder = concurrentHandlerHolder,
            deviceInfo = deviceInfo,
            pushInternal = mockPushInternal
        )

        setupMobileEngageComponent(fakeDependencyContainer)
    }
}
