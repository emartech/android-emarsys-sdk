package com.emarsys.service

import android.app.Application
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
import com.google.firebase.messaging.RemoteMessage
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
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
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var spyCoreHandler: SdkHandler
    private lateinit var mockRemoteMessage: RemoteMessage
    private lateinit var emarsysFirebaseMessagingService: EmarsysFirebaseMessagingService

    @Before
    fun setUp() {
        emarsysFirebaseMessagingService = EmarsysFirebaseMessagingService()
        mockPushInternal = mock()
        mockRemoteMessage = mockk(relaxed = true)

        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
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
        unmockkAll()
    }

    @Test
    fun testOnNewToken_whenIsAutomaticPushSendingEnabledIsTrue_callsSetPushToken() {
        setupEmarsys(isAutomaticPushSending = true, isGooglePlayAvailable = true)

        emarsysFirebaseMessagingService.onNewToken("testToken")

        verify(mockPushInternal, timeout(100)).setPushToken("testToken", null)
    }

    @Test
    fun testOnNewToken_whenIsAutomaticPushSendingEnabledIsTrue_callsSetPushToken_onCoreSdkThread() {
        setupEmarsys(isAutomaticPushSending = true, isGooglePlayAvailable = true)

        emarsysFirebaseMessagingService.onNewToken("testToken")

        verify(spyCoreHandler, timeout(1000).times(1)).post(any())
    }

    @Test
    fun testOnNewToken_whenIsAutomaticPushSendingEnabledIsTrue_andGooglePlayIsUnavailable_doesNotCallSetPushToken() {
        setupEmarsys(isAutomaticPushSending = true, isGooglePlayAvailable = false)

        emarsysFirebaseMessagingService.onNewToken("testToken")

        verify(mockPushInternal, times(0)).setPushToken("testToken", null)
    }

    @Test
    fun testOnNewToken_whenIsAutomaticPushSendingEnabledIsFalse_doesNotCallSetPushToken() {
        setupEmarsys(isAutomaticPushSending = false, isGooglePlayAvailable = true)

        emarsysFirebaseMessagingService.onNewToken("testToken")

        verify(mockPushInternal, times(0)).setPushToken("testToken", null)
    }

    @Test
    fun testOnMessageReceived_whenGooglePlayIsAvailable_callsHandleMessage() {
        mockkStatic("com.emarsys.service.EmarsysFirebaseMessagingServiceUtils")
        setupEmarsys(isAutomaticPushSending = true, isGooglePlayAvailable = true)

        emarsysFirebaseMessagingService.onMessageReceived(mockRemoteMessage)

        verify(times(1)) {
            EmarsysFirebaseMessagingServiceUtils.handleMessage(
                emarsysFirebaseMessagingService,
                mockRemoteMessage
            )
        }
    }

    @Test
    fun testOnMessageReceived_whenGooglePlayIsUnavailable_doesNotCallHandleMessage() {
        mockkStatic("com.emarsys.service.EmarsysFirebaseMessagingServiceUtils")
        setupEmarsys(isAutomaticPushSending = true, isGooglePlayAvailable = false)

        emarsysFirebaseMessagingService.onMessageReceived(mockRemoteMessage)

        verify(times(0)) {
            EmarsysFirebaseMessagingServiceUtils.handleMessage(
                emarsysFirebaseMessagingService,
                mockRemoteMessage
            )
        }
    }

    private fun setupEmarsys(isAutomaticPushSending: Boolean, isGooglePlayAvailable: Boolean) {
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
            isGooglePlayAvailable
        )

        fakeDependencyContainer = FakeFirebaseDependencyContainer(
            concurrentHandlerHolder = concurrentHandlerHolder,
            deviceInfo = deviceInfo,
            pushInternal = mockPushInternal
        )

        setupMobileEngageComponent(fakeDependencyContainer)
    }
}
