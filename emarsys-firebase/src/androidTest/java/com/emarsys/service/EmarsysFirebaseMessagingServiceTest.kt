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
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.ReflectionTestUtils
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.timeout
import org.mockito.kotlin.times
import org.mockito.kotlin.verify


class EmarsysFirebaseMessagingServiceTest : AnnotationSpec() {


    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    private lateinit var mockPushInternal: PushInternal
    private lateinit var fakeDependencyContainer: FakeFirebaseDependencyContainer
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var spyCoreHandler: SdkHandler
    private lateinit var emarsysFirebaseMessagingService: EmarsysFirebaseMessagingService

    @Before
    fun setUp() {
        emarsysFirebaseMessagingService = EmarsysFirebaseMessagingService()
        mockPushInternal = mock()

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
