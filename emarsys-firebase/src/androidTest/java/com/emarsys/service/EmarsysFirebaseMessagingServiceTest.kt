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
import com.emarsys.testUtil.KotestRunnerAndroid
import com.emarsys.testUtil.ReflectionTestUtils
import io.kotest.core.spec.style.AnnotationSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.runner.RunWith

@RunWith(KotestRunnerAndroid::class)
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
        mockPushInternal = mockk(relaxed = true)

        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        spyCoreHandler = spyk(concurrentHandlerHolder.coreHandler)
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

        verify(timeout = 100L) { mockPushInternal.setPushToken("testToken", null) }
    }

    @Test
    fun testOnNewToken_whenIsAutomaticPushSendingEnabledIsTrue_callsSetPushToken_onCoreSdkThread() {
        setupEmarsys(isAutomaticPushSending = true, isGooglePlayAvailable = true)

        emarsysFirebaseMessagingService.onNewToken("testToken")

        verify(exactly = 1, timeout = 1000) { spyCoreHandler.post(any()) }
    }

    @Test
    fun testOnNewToken_whenIsAutomaticPushSendingEnabledIsTrue_andGooglePlayIsUnavailable_doesNotCallSetPushToken() {
        setupEmarsys(isAutomaticPushSending = true, isGooglePlayAvailable = false)

        emarsysFirebaseMessagingService.onNewToken("testToken")

        verify(exactly = 0) { mockPushInternal.setPushToken("testToken", null) }
    }

    @Test
    fun testOnNewToken_whenIsAutomaticPushSendingEnabledIsFalse_doesNotCallSetPushToken() {
        setupEmarsys(isAutomaticPushSending = false, isGooglePlayAvailable = true)

        emarsysFirebaseMessagingService.onNewToken("testToken")

        verify(exactly = 0) { mockPushInternal.setPushToken("testToken", null) }
    }

    private fun setupEmarsys(isAutomaticPushSending: Boolean, isGooglePlayAvailable: Boolean) {
        val deviceInfo = DeviceInfo(
            application,
            mockk(relaxed = true) {
                every { provideClientId() } returns  "clientId"
            },
            mockk(relaxed = true) {
                every { provideSdkVersion() } returns  "version"
            },
            mockk(relaxed = true) {
                every { provideLanguage(any()) } returns  "language"
            },
            mockk(relaxed = true),
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
