package com.emarsys.config


import com.emarsys.EmarsysRequestModelFactory
import com.emarsys.common.feature.InnerFeature
import com.emarsys.config.model.RemoteConfig
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.crypto.Crypto
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.util.log.LogLevel
import com.emarsys.core.worker.DelegatorCompletionHandlerProvider
import com.emarsys.fake.FakeRestClient
import com.emarsys.fake.FakeResultListener
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.predict.PredictInternal
import com.emarsys.predict.request.PredictRequestContext
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.ExtensionTestUtils.tryCast
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.mockito.ThreadSpy
import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import java.util.concurrent.CountDownLatch

class DefaultConfigInternalTest : AnnotationSpec() {
    private companion object {
        const val APPLICATION_CODE = "applicationCode"
        const val MERCHANT_ID = "merchantId"
        const val OTHER_APPLICATION_CODE = "otherApplicationCode"
        const val CONTACT_FIELD_ID = 3
        const val PUSH_TOKEN = "pushToken"
        const val EVENT_SERVICE_URL = "https://event.emarsys.com"
        const val CLIENT_SERVICE_URL = "https://client.emarsys.com"
        const val DEEPLINK_SERVICE_URL = "https://deeplink.emarsys.com"
        const val INBOX_SERVICE_URL = "https://inbox.emarsys.com"
        const val MOBILE_ENGAGE_V2_SERVICE_URL = "https://mev2.emarsys.com"
        const val PREDICT_SERVICE_URL = "https://predict.emarsys.com"
        const val MESSAGE_INBOX_SERVICE_URL = "https://inbox.v3.emarsys.com"
        const val SDK_VERSION = "testSdkVersion"
    }

    private lateinit var configInternal: ConfigInternal
    private lateinit var mockMobileEngageRequestContext: MobileEngageRequestContext
    private lateinit var mockPredictRequestContext: PredictRequestContext
    private lateinit var mockMobileEngageInternal: MobileEngageInternal
    private lateinit var mockPushInternal: PushInternal
    private lateinit var mockPredictInternal: PredictInternal
    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var latch: CountDownLatch
    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockEmarsysRequestModelFactory: EmarsysRequestModelFactory
    private lateinit var mockConfigResponseMapper: RemoteConfigResponseMapper
    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockEventServiceStorage: StringStorage
    private lateinit var mockClientServiceStorage: StringStorage
    private lateinit var mockDeeplinkServiceStorage: StringStorage
    private lateinit var mockPredictServiceStorage: StringStorage
    private lateinit var mockMessageInboxServiceStorage: StringStorage
    private lateinit var mockLogLevelStorage: StringStorage
    private lateinit var mockCrypto: Crypto
    private lateinit var mockClientServiceInternal: ClientServiceInternal
    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder


    @Before
    fun setUp() {
        FeatureTestUtils.resetFeatures()
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()

        latch = CountDownLatch(1)

        mockCompletionListener = mockk(relaxed = true)

        mockPredictRequestContext = mockk(relaxed = true)
        every { mockPredictRequestContext.merchantId } returns MERCHANT_ID

        mockMobileEngageRequestContext = mockk(relaxed = true)
        every { mockMobileEngageRequestContext.applicationCode } returns APPLICATION_CODE
        every { mockMobileEngageRequestContext.contactFieldId } returns CONTACT_FIELD_ID

        mockMobileEngageInternal = mockk(relaxed = true)
        every { mockMobileEngageInternal.clearContact(any()) } answers {
            (args[0] as CompletionListener?)?.onCompleted(null)
        }
        every { mockMobileEngageInternal.setContact(any(), any(), any()) } answers {
            (args[1] as CompletionListener?)?.onCompleted(null)
        }

        mockPushInternal = mockk(relaxed = true)

        every { mockPushInternal.setPushToken(any(), any()) } answers {
            (args[1] as CompletionListener?)?.onCompleted(null)
        }
        every { mockPushInternal.clearPushToken(any()) } answers {
            (args[0] as CompletionListener?)?.onCompleted(null)
        }

        mockRequestModel = mockk(relaxed = true)
        every { mockRequestModel.id } returns "reqId"

        mockEmarsysRequestModelFactory = mockk(relaxed = true)
        every { mockEmarsysRequestModelFactory.createRemoteConfigRequest() } returns mockRequestModel
        every { mockEmarsysRequestModelFactory.createRemoteConfigSignatureRequest() } returns mockRequestModel

        mockRequestManager = mockk(relaxed = true)

        mockPredictInternal = mockk(relaxed = true)

        mockDeviceInfo = mockk(relaxed = true)

        mockConfigResponseMapper = mockk(relaxed = true)

        mockResponseModel = mockk(relaxed = true)
        mockClientServiceStorage = mockk(relaxed = true)
        mockEventServiceStorage = mockk(relaxed = true)
        mockDeeplinkServiceStorage = mockk(relaxed = true)
        mockPredictServiceStorage = mockk(relaxed = true)
        mockMessageInboxServiceStorage = mockk(relaxed = true)
        mockLogLevelStorage = mockk(relaxed = true)
        mockCrypto = mockk(relaxed = true)
        mockClientServiceInternal = mockk(relaxed = true)
        every { mockClientServiceInternal.trackDeviceInfo(any()) } answers {
            (args[0] as CompletionListener?)?.onCompleted(null)
        }

        configInternal = spyk(
            DefaultConfigInternal(
                mockMobileEngageRequestContext,
                mockMobileEngageInternal,
                mockPushInternal,
                mockPredictRequestContext,
                mockDeviceInfo,
                mockRequestManager,
                mockEmarsysRequestModelFactory,
                mockConfigResponseMapper,
                mockClientServiceStorage,
                mockEventServiceStorage,
                mockDeeplinkServiceStorage,
                mockPredictServiceStorage,
                mockMessageInboxServiceStorage,
                mockLogLevelStorage,
                mockCrypto,
                mockClientServiceInternal,
                concurrentHandlerHolder
            )
        )
    }

    @After
    fun tearDown() {
        FeatureTestUtils.resetFeatures()
    }

    @Test
    fun testGetContactFieldId_shouldReturnValueFromRequestContext() {
        val result = configInternal.contactFieldId

        result shouldBe CONTACT_FIELD_ID
    }

    @Test
    fun testGetApplicationCode_shouldReturnValueFromRequestContext() {
        val result = configInternal.applicationCode

        result shouldBe APPLICATION_CODE
    }

    @Test
    fun testGetMerchantId_shouldReturnValueFromRequestContext() {
        val result = configInternal.merchantId

        result shouldBe MERCHANT_ID
    }

    @Test
    fun testChangeApplicationCode_shouldCallClearContact() {
        val latch = CountDownLatch(1)
        every { mockMobileEngageRequestContext.hasContactIdentification() } returns true
        every { mockMobileEngageRequestContext.applicationCode } returns APPLICATION_CODE
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE) {
            latch.countDown()
        }
        latch.await()

        verify { mockMobileEngageInternal.clearContact(any()) }
    }

    @Test
    fun testChangeApplicationCode_shouldChangeApplicationCodeAfterClearContact() {
        every { mockPushInternal.pushToken } returns PUSH_TOKEN
        every { mockMobileEngageRequestContext.hasContactIdentification() } returns true

        val latch = CountDownLatch(1)

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE) {
            latch.countDown()
        }
        latch.await()

        verifyOrder {
            mockPushInternal.clearPushToken(any())
            mockMobileEngageInternal.clearContact(any())
            mockMobileEngageRequestContext.applicationCode = OTHER_APPLICATION_CODE
            mockPushInternal.setPushToken(PUSH_TOKEN, any())
        }
    }

    @Test
    fun testChangeApplicationCode_shouldInterruptFlow_andDisableFeature_whenErrorHappenedDuringClearContact() {
        every { mockPushInternal.pushToken } returns PUSH_TOKEN
        every { mockMobileEngageRequestContext.hasContactIdentification() } returns true

        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)
        FeatureRegistry.enableFeature(InnerFeature.EVENT_SERVICE_V4)

        val mockMobileEngageInternal: MobileEngageInternal = mockk(relaxed = true)
        every { mockMobileEngageInternal.clearContact(any()) } answers {
            (args[0] as CompletionListener).onCompleted(Throwable())
        }

        configInternal = DefaultConfigInternal(
            mockMobileEngageRequestContext,
            mockMobileEngageInternal,
            mockPushInternal,
            mockPredictRequestContext,
            mockDeviceInfo,
            mockRequestManager,
            mockEmarsysRequestModelFactory,
            mockConfigResponseMapper,
            mockClientServiceStorage,
            mockEventServiceStorage,
            mockDeeplinkServiceStorage,
            mockPredictServiceStorage,
            mockMessageInboxServiceStorage,
            mockLogLevelStorage,
            mockCrypto,
            mockClientServiceInternal,
            concurrentHandlerHolder
        )
        val latch = CountDownLatch(1)
        val completionListener = CompletionListener {
            latch.countDown()
        }
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, completionListener)
        latch.await()

        verify { mockPushInternal.pushToken }
        verify(exactly = 2) { mockMobileEngageRequestContext.applicationCode }
        verify { mockMobileEngageRequestContext.hasContactIdentification() }
        verify { mockPushInternal.clearPushToken(any()) }
        verify { mockMobileEngageInternal.clearContact(any()) }
        confirmVerified(mockMobileEngageInternal)
        confirmVerified(mockPushInternal)

        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
        FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4) shouldBe false
        verify { mockMobileEngageRequestContext.applicationCode = null }
        verify { mockMobileEngageRequestContext.reset() }
        confirmVerified(mockMobileEngageRequestContext)
    }

    @Test
    fun testChangeApplicationCode_shouldDoOnlyLogin_whenApplicationCode_wasNull() {
        val latch = CountDownLatch(1)

        val mockMobileEngageRequestContext: MobileEngageRequestContext = mockk(relaxed = true)
        every { mockMobileEngageRequestContext.applicationCode } returns null
        every { mockMobileEngageRequestContext.contactFieldId } returns CONTACT_FIELD_ID

        every { mockPushInternal.pushToken } returns PUSH_TOKEN

        configInternal = DefaultConfigInternal(
            mockMobileEngageRequestContext,
            mockMobileEngageInternal,
            mockPushInternal,
            mockPredictRequestContext,
            mockDeviceInfo,
            mockRequestManager,
            mockEmarsysRequestModelFactory,
            mockConfigResponseMapper,
            mockClientServiceStorage,
            mockEventServiceStorage,
            mockDeeplinkServiceStorage,
            mockPredictServiceStorage,
            mockMessageInboxServiceStorage,
            mockLogLevelStorage,
            mockCrypto,
            mockClientServiceInternal,
            concurrentHandlerHolder
        )

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE) {
            latch.countDown()
        }

        latch.await()

        verify(exactly = 2) { mockMobileEngageRequestContext.applicationCode }
        verify { mockMobileEngageRequestContext.applicationCode = OTHER_APPLICATION_CODE }
        verify { mockPushInternal.setPushToken(PUSH_TOKEN, any()) }
        verify(exactly = 1) { mockMobileEngageInternal.clearContact(any()) }
    }

    @Test
    fun testChangeApplicationCode_shouldInterruptFlow_andDisableFeature_whenErrorHappenedDuringSetPushToken() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)
        FeatureRegistry.enableFeature(InnerFeature.EVENT_SERVICE_V4)

        val mockPushInternal: PushInternal = mockk(relaxed = true)
        every { mockPushInternal.setPushToken(any(), any()) } answers {
            (args[1] as CompletionListener).onCompleted(Throwable("testErrorMessage"))
        }
        every { mockPushInternal.clearPushToken(any()) } answers {
            (args[0] as CompletionListener).onCompleted(null)
        }

        every { mockPushInternal.pushToken } returns PUSH_TOKEN

        configInternal = DefaultConfigInternal(
            mockMobileEngageRequestContext,
            mockMobileEngageInternal,
            mockPushInternal,
            mockPredictRequestContext,
            mockDeviceInfo,
            mockRequestManager,
            mockEmarsysRequestModelFactory,
            mockConfigResponseMapper,
            mockClientServiceStorage,
            mockEventServiceStorage,
            mockDeeplinkServiceStorage,
            mockPredictServiceStorage,
            mockMessageInboxServiceStorage,
            mockLogLevelStorage,
            mockCrypto,
            mockClientServiceInternal,
            concurrentHandlerHolder
        )

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE) {
            latch.countDown()
        }
        latch.await()

        verify { mockPushInternal.setPushToken(PUSH_TOKEN, any()) }
        confirmVerified(mockMobileEngageInternal)
        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
        FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4) shouldBe false
        verify { mockMobileEngageRequestContext.applicationCode = null }
    }

    @Test
    fun testChangeApplicationCode_shouldWorkWithoutCompletionListener() {
        every { mockPushInternal.pushToken } returns PUSH_TOKEN
        every { mockMobileEngageRequestContext.hasContactIdentification() } returns true

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, null)

        verifyOrder {
            mockPushInternal.clearPushToken(any())
            mockMobileEngageInternal.clearContact(any())
            mockMobileEngageRequestContext.applicationCode =
                OTHER_APPLICATION_CODE
            mockPushInternal.setPushToken(PUSH_TOKEN, any())
        }
    }

    @Test
    fun testChangeApplicationCode_shouldEnableFeature() {
        configInternal.changeApplicationCode(APPLICATION_CODE) {
            latch.countDown()
        }

        latch.await()

        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe true
        FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4) shouldBe true
    }

    @Test
    fun testChangeApplicationCode_shouldDisableFeature() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        configInternal.changeApplicationCode(null) {
            latch.countDown()
        }

        latch.await()

        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
    }

    @Test
    fun testChangeApplicationCode_whenClearPushToken_returnsWithError_callHandleError() {
        every { mockPushInternal.pushToken } returns PUSH_TOKEN
        every { mockPushInternal.clearPushToken(any()) } answers {
            (args[0] as CompletionListener).onCompleted(Throwable())
        }
        val latch = CountDownLatch(1)
        val completionListener = CompletionListener {
            latch.countDown()
        }
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, completionListener)
        latch.await()

        confirmVerified(mockMobileEngageInternal)
        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
        FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4) shouldBe false
        every { mockMobileEngageRequestContext.applicationCode = null }
    }

    @Test
    fun testChangeApplicationCode_whenApplicationCodeIsMissing_shouldNotCleanPushTokenButWorkProperly() {
        every { mockMobileEngageRequestContext.applicationCode } returns null
        every { mockPushInternal.pushToken } returns PUSH_TOKEN

        val latch = CountDownLatch(1)
        val completionListener = CompletionListener {
            latch.countDown()
        }
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, completionListener)
        latch.await()

        verify(exactly = 0) { mockPushInternal.clearPushToken(any()) }
        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe true
        FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4) shouldBe true
        verify { mockMobileEngageRequestContext.applicationCode = OTHER_APPLICATION_CODE }
    }

    @Test
    fun testChangeApplicationCode_whenClearPushToken_butPushTokenHasNotBeenSetPreviously_callOnSuccess() {
        every { mockPushInternal.pushToken } returns null
        every { mockPushInternal.clearPushToken(any()) } answers {
            (args[0] as CompletionListener).onCompleted(Throwable())
        }
        val latch = CountDownLatch(1)
        var result: Throwable? = null

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE) {
            result = it
            latch.countDown()
        }
        latch.await()

        result shouldBe null
    }

    @Test
    fun testChangeApplicationCode_shouldOnlyLogout_whenApplicationCodeIsNull() {
        every { mockPushInternal.pushToken } returns "testPushToken"
        every { mockMobileEngageRequestContext.hasContactIdentification() } returns true
        every { mockMobileEngageRequestContext.applicationCode } returns APPLICATION_CODE

        val latch = CountDownLatch(1)
        val completionListener = CompletionListener {
            latch.countDown()
        }

        configInternal.changeApplicationCode(null, completionListener)

        latch.await()

        verify { mockPushInternal.pushToken }
        verify(exactly = 2) { mockMobileEngageRequestContext.applicationCode }
        verify { mockMobileEngageRequestContext.hasContactIdentification() }
        verify { mockMobileEngageRequestContext.applicationCode = null }
        verify { mockPushInternal.clearPushToken(any()) }
        verify { mockMobileEngageInternal.clearContact(any()) }
        verify { mockMobileEngageRequestContext.reset() }
        confirmVerified(mockMobileEngageRequestContext)
        confirmVerified(mockMobileEngageInternal)
        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
    }

    @Test
    fun testChangeApplicationCode_shouldNotSendPushToken_whenPushTokenIsNull() {
        every { mockMobileEngageRequestContext.hasContactIdentification() } returns true
        every { mockPushInternal.pushToken } returns null
        val latch = CountDownLatch(1)

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE) {
            latch.countDown()
        }
        latch.await()

        verifyOrder {
            mockPushInternal.pushToken
            mockMobileEngageRequestContext.hasContactIdentification()
            mockMobileEngageInternal.clearContact(any())
            mockMobileEngageRequestContext.applicationCode = OTHER_APPLICATION_CODE
            mockClientServiceInternal.trackDeviceInfo(any())
        }
        confirmVerified(mockPushInternal)
    }

    @Test
    fun testChangeApplicationCode_shouldSetAnonymContact_whenThereWasNoContactIdentification() {
        val latch = CountDownLatch(1)
        every { mockMobileEngageRequestContext.hasContactIdentification() } returns false
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE) {
            latch.countDown()
        }
        latch.await()

        verifyOrder {
            mockMobileEngageRequestContext.applicationCode = OTHER_APPLICATION_CODE
            mockClientServiceInternal.trackDeviceInfo(any())
            mockMobileEngageInternal.clearContact(any())
        }
    }

    @Test
    fun testChangeApplicationCode_shouldNotClearContactAfter_ifContactWasSet() {
        val latch = CountDownLatch(1)
        every { mockMobileEngageRequestContext.hasContactIdentification() } returns true
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE) {
            latch.countDown()
        }
        latch.await()
        verifyOrder {
            mockMobileEngageInternal.clearContact(any())
            mockMobileEngageRequestContext.applicationCode = OTHER_APPLICATION_CODE
            mockClientServiceInternal.trackDeviceInfo(any())
        }
        confirmVerified(mockMobileEngageInternal)
    }

    @Test
    fun testChangeMerchantId_shouldEnableFeature() {
        configInternal.changeMerchantId(MERCHANT_ID)

        FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT) shouldBe true
    }

    @Test
    fun testChangeMerchantId_shouldDisableFeature() {
        FeatureRegistry.enableFeature(InnerFeature.PREDICT)

        configInternal.changeMerchantId(null)

        verify { mockPredictRequestContext.merchantId = null }
        FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT) shouldBe false
    }

    @Test
    fun testChangeMerchantId_shouldSaveMerchantId() {
        configInternal.changeMerchantId(MERCHANT_ID)

        verify { mockPredictRequestContext.merchantId = MERCHANT_ID }
    }

    @Test
    fun testGetClientId_shouldReturnHWIDFromDeviceInfo() {
        every { mockDeviceInfo.clientId } returns "testClientId"
        val result = configInternal.clientId
        result shouldBe "testClientId"
    }

    @Test
    fun testGetLanguage_shouldReturnLanguageCodeFromDeviceInfo() {
        every { mockDeviceInfo.language } returns "testLanguage"
        val result = configInternal.language

        result shouldBe "testLanguage"
    }

    @Test
    fun testGetNotificationSettings_shouldReturnNotificationSettingsFromDeviceInfo() {
        val notificationSettings: NotificationSettings = mockk(relaxed = true)
        every { mockDeviceInfo.notificationSettings } returns notificationSettings
        val result = configInternal.notificationSettings

        result shouldBe notificationSettings
    }

    @Test
    fun testIsAutomaticPushSendingEnabled_shouldReturnValueFromDeviceInfo() {
        every { mockDeviceInfo.isAutomaticPushSendingEnabled } returns true
        val result = configInternal.isAutomaticPushSendingEnabled

        result shouldBe true
        verify { mockDeviceInfo.isAutomaticPushSendingEnabled }
    }

    @Test
    fun testSdkVersion_shouldReturnSdkVersionFromDeviceInfo() {
        every { mockDeviceInfo.sdkVersion } returns SDK_VERSION
        val result = configInternal.sdkVersion

        result shouldBe SDK_VERSION
        verify { mockDeviceInfo.sdkVersion }
    }

    @Test
    fun testRefreshRemoteConfig_shouldNotFetch_when_applicationCode_isNull() {
        every { mockMobileEngageRequestContext.applicationCode } returns null

        (configInternal as DefaultConfigInternal).refreshRemoteConfig(null)

        confirmVerified(mockEmarsysRequestModelFactory)
        confirmVerified(mockRequestManager)
    }

    @Test
    fun testFetchRemoteConfig_shouldCallRequestManager_withCorrectRequestModel() {
        val requestModel: RequestModel = mockk(relaxed = true)
        every { mockEmarsysRequestModelFactory.createRemoteConfigRequest() } returns requestModel

        (configInternal as DefaultConfigInternal).fetchRemoteConfig { }

        verify { mockRequestManager.submitNow(eq(requestModel), any()) }
    }

    @Test
    fun testFetchRemoteConfigSignature_shouldCallRequestManager_withCorrectRequestModel() {
        val requestModel: RequestModel = mockk(relaxed = true)
        every { mockEmarsysRequestModelFactory.createRemoteConfigSignatureRequest() } returns requestModel

        configInternal.refreshRemoteConfig(null)

        verify { mockRequestManager.submitNow(requestModel, any()) }
    }

    @Test
    fun testFetchRemoteConfig_shouldCallConfigResponseMapper_onSuccess() {
        val resultListener =
            FakeResultListener<ResponseModel>(latch, FakeResultListener.Mode.MAIN_THREAD)

        val configInternal = DefaultConfigInternal(
            mockMobileEngageRequestContext,
            mockMobileEngageInternal,
            mockPushInternal,
            mockPredictRequestContext,
            mockDeviceInfo,
            requestManagerWithRestClient(
                FakeRestClient(
                    response = mockResponseModel,
                    mode = FakeRestClient.Mode.SUCCESS
                )
            ),
            mockEmarsysRequestModelFactory,
            mockConfigResponseMapper,
            mockClientServiceStorage,
            mockEventServiceStorage,
            mockDeeplinkServiceStorage,
            mockPredictServiceStorage,
            mockMessageInboxServiceStorage,
            mockLogLevelStorage,
            mockCrypto,
            mockClientServiceInternal,
            concurrentHandlerHolder
        )

        configInternal.fetchRemoteConfig(resultListener)

        latch.await()

        resultListener.resultStatus shouldBe mockResponseModel
        resultListener.successCount shouldBe 1
    }

    @Test
    fun testFetchRemoteConfig_shouldCallConfigResponseMapper_onFailure() {
        configInternal = DefaultConfigInternal(
            mockMobileEngageRequestContext,
            mockMobileEngageInternal,
            mockPushInternal,
            mockPredictRequestContext,
            mockDeviceInfo,
            requestManagerWithRestClient(
                FakeRestClient(
                    response = mockResponseModel,
                    mode = FakeRestClient.Mode.ERROR_RESPONSE_MODEL
                )
            ),
            mockEmarsysRequestModelFactory,
            mockConfigResponseMapper,
            mockClientServiceStorage,
            mockEventServiceStorage,
            mockDeeplinkServiceStorage,
            mockPredictServiceStorage,
            mockMessageInboxServiceStorage,
            mockLogLevelStorage,
            mockCrypto,
            mockClientServiceInternal,
            concurrentHandlerHolder
        )

        val resultListener =
            FakeResultListener<ResponseModel>(latch, FakeResultListener.Mode.MAIN_THREAD)
        (configInternal as DefaultConfigInternal).fetchRemoteConfig(resultListener)

        latch.await()

        resultListener.errorCount shouldBe 1
    }

    @Test
    fun testFetchRemoteConfig_shouldCallConfigResponseMapper_onException() {
        val mockException: Exception = mockk(relaxed = true)

        val configInternal = DefaultConfigInternal(
            mockMobileEngageRequestContext,
            mockMobileEngageInternal,
            mockPushInternal,
            mockPredictRequestContext,
            mockDeviceInfo,
            requestManagerWithRestClient(FakeRestClient(exception = mockException)),
            mockEmarsysRequestModelFactory,
            mockConfigResponseMapper,
            mockClientServiceStorage,
            mockEventServiceStorage,
            mockDeeplinkServiceStorage,
            mockPredictServiceStorage,
            mockMessageInboxServiceStorage,
            mockLogLevelStorage,
            mockCrypto,
            mockClientServiceInternal,
            concurrentHandlerHolder
        )

        val resultListener =
            FakeResultListener<ResponseModel>(latch, FakeResultListener.Mode.MAIN_THREAD)

        configInternal.fetchRemoteConfig(resultListener)

        latch.await()

        resultListener.errorCount shouldBe 1
    }

    @Test
    fun testFetchRemoteConfigSignature_shouldCallConfigSignatureResultParser_onSuccess() {
        val expectedResult = "signature"
        val resultListener = FakeResultListener<String>(latch, FakeResultListener.Mode.MAIN_THREAD)
        every { mockResponseModel.body } returns "signature"

        val configInternal = DefaultConfigInternal(
            mockMobileEngageRequestContext,
            mockMobileEngageInternal,
            mockPushInternal,
            mockPredictRequestContext,
            mockDeviceInfo,
            requestManagerWithRestClient(
                FakeRestClient(
                    response = mockResponseModel,
                    mode = FakeRestClient.Mode.SUCCESS
                )
            ),
            mockEmarsysRequestModelFactory,
            mockConfigResponseMapper,
            mockClientServiceStorage,
            mockEventServiceStorage,
            mockDeeplinkServiceStorage,
            mockPredictServiceStorage,
            mockMessageInboxServiceStorage,
            mockLogLevelStorage,
            mockCrypto,
            mockClientServiceInternal,
            concurrentHandlerHolder
        )

        configInternal.fetchRemoteConfigSignature(resultListener)

        latch.await()

        resultListener.resultStatus shouldBe expectedResult
        resultListener.successCount shouldBe 1
    }

    @Test
    fun testFetchRemoteConfigSignature_shouldCallConfigResponseMapper_onFailure() {
        configInternal = DefaultConfigInternal(
            mockMobileEngageRequestContext,
            mockMobileEngageInternal,
            mockPushInternal,
            mockPredictRequestContext,
            mockDeviceInfo,
            requestManagerWithRestClient(
                FakeRestClient(
                    response = mockResponseModel,
                    mode = FakeRestClient.Mode.ERROR_RESPONSE_MODEL
                )
            ),
            mockEmarsysRequestModelFactory,
            mockConfigResponseMapper,
            mockClientServiceStorage,
            mockEventServiceStorage,
            mockDeeplinkServiceStorage,
            mockPredictServiceStorage,
            mockMessageInboxServiceStorage,
            mockLogLevelStorage,
            mockCrypto,
            mockClientServiceInternal,
            concurrentHandlerHolder
        )

        val resultListener = FakeResultListener<String>(latch, FakeResultListener.Mode.MAIN_THREAD)
        (configInternal as DefaultConfigInternal).fetchRemoteConfigSignature(resultListener)

        latch.await()

        resultListener.errorCount shouldBe 1
    }

    @Test
    fun testFetchRemoteConfigSignature_shouldCallConfigResponseMapper_onException() {
        val mockException: Exception = mockk(relaxed = true)

        val configInternal = DefaultConfigInternal(
            mockMobileEngageRequestContext,
            mockMobileEngageInternal,
            mockPushInternal,
            mockPredictRequestContext,
            mockDeviceInfo,
            requestManagerWithRestClient(FakeRestClient(exception = mockException)),
            mockEmarsysRequestModelFactory,
            mockConfigResponseMapper,
            mockClientServiceStorage,
            mockEventServiceStorage,
            mockDeeplinkServiceStorage,
            mockPredictServiceStorage,
            mockMessageInboxServiceStorage,
            mockLogLevelStorage,
            mockCrypto,
            mockClientServiceInternal,
            concurrentHandlerHolder
        )

        val resultListener = FakeResultListener<String>(latch, FakeResultListener.Mode.MAIN_THREAD)

        configInternal.fetchRemoteConfigSignature(resultListener)

        latch.await()

        resultListener.errorCount shouldBe 1
    }

    @Test
    fun testApplyRemoteConfig_applyOne() {
        val remoteConfig = RemoteConfig(
            clientServiceUrl = CLIENT_SERVICE_URL
        )

        (configInternal as DefaultConfigInternal).applyRemoteConfig(remoteConfig)

        verify { mockClientServiceStorage.set(CLIENT_SERVICE_URL) }
        verify { mockEventServiceStorage.set(null) }
        verify { mockDeeplinkServiceStorage.set(null) }
        verify { mockPredictServiceStorage.set(null) }
        verify { mockMessageInboxServiceStorage.set(null) }
    }

    @Test
    fun testApplyRemoteConfig_applyAll() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)
        FeatureRegistry.enableFeature(InnerFeature.PREDICT)
        FeatureRegistry.enableFeature(InnerFeature.EVENT_SERVICE_V4)

        val remoteConfig = RemoteConfig(
            eventServiceUrl = EVENT_SERVICE_URL,
            clientServiceUrl = CLIENT_SERVICE_URL,
            deepLinkServiceUrl = DEEPLINK_SERVICE_URL,
            inboxServiceUrl = INBOX_SERVICE_URL,
            mobileEngageV2ServiceUrl = MOBILE_ENGAGE_V2_SERVICE_URL,
            predictServiceUrl = PREDICT_SERVICE_URL,
            messageInboxServiceUrl = MESSAGE_INBOX_SERVICE_URL,
            logLevel = LogLevel.DEBUG,
            features = mapOf(
                InnerFeature.MOBILE_ENGAGE to false,
                InnerFeature.PREDICT to true,
                InnerFeature.EVENT_SERVICE_V4 to false
            )
        )

        (configInternal as DefaultConfigInternal).applyRemoteConfig(remoteConfig)

        verify { mockClientServiceStorage.set(CLIENT_SERVICE_URL) }
        verify { mockEventServiceStorage.set(EVENT_SERVICE_URL) }
        verify { mockDeeplinkServiceStorage.set(DEEPLINK_SERVICE_URL) }
        verify { mockPredictServiceStorage.set(PREDICT_SERVICE_URL) }
        verify { mockMessageInboxServiceStorage.set(MESSAGE_INBOX_SERVICE_URL) }
        verify { mockLogLevelStorage.set("DEBUG") }
        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
        FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT) shouldBe true
        FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4) shouldBe false
    }

    @Test
    fun testApplyRemoteConfig_applyFlippers() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)
        FeatureRegistry.enableFeature(InnerFeature.PREDICT)

        val remoteConfig = RemoteConfig(
            features = mapOf(
                InnerFeature.MOBILE_ENGAGE to false
            )
        )

        (configInternal as DefaultConfigInternal).applyRemoteConfig(remoteConfig)

        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
        FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT) shouldBe true
    }

    @Test
    fun testResetRemoteConfig() {
        configInternal.resetRemoteConfig()

        verify { mockClientServiceStorage.set(null) }
        verify { mockEventServiceStorage.set(null) }
        verify { mockDeeplinkServiceStorage.set(null) }
        verify { mockPredictServiceStorage.set(null) }
        verify { mockMessageInboxServiceStorage.set(null) }
        verify { mockLogLevelStorage.set(null) }
    }

    @Test
    fun testRefreshRemoteConfig_shouldNotCall_fetchRemoteConfig_andCallResetRemoteConfig_ifSignatureFetchFailed() {
        val expectedException: Exception = mockk(relaxed = true)
        val result: Try<String> = Try.failure(expectedException)
        every { (configInternal as DefaultConfigInternal).fetchRemoteConfigSignature(any()) } answers {
            (args[0]?.tryCast<ResultListener<Try<String>>> { onResult(result) })
        }

        configInternal.refreshRemoteConfig(mockCompletionListener)

        verify { mockCompletionListener.onCompleted(any()) }
        verify { (configInternal as DefaultConfigInternal).resetRemoteConfig() }
        verify(exactly = 0) { (configInternal as DefaultConfigInternal).fetchRemoteConfig(any()) }
    }

    @Test
    fun testRefreshRemoteConfig_verifyApplyRemoteConfigCalled_onSuccess() {
        val expectedRemoteConfig = RemoteConfig(eventServiceUrl = "https://test.emarsys.com")
        val expectedResponseModel = ResponseModel.Builder().body(
            """
                   {
                        "serviceUrls":{
                                "eventService":"https://test.emarsys.com"
                        }
                   }
               """.trimIndent()
        )
            .statusCode(200)
            .requestModel(mockRequestModel)
            .message("responseMessage").build()
        every { mockConfigResponseMapper.map(expectedResponseModel) } returns expectedRemoteConfig

        every { (configInternal as DefaultConfigInternal).fetchRemoteConfigSignature(any()) } answers {
            val result: Try<String> = Try.success("signature")
            args[0]?.tryCast<ResultListener<Try<String>>> { onResult(result) }
        }

        every { (configInternal as DefaultConfigInternal).fetchRemoteConfig(any()) } answers {
            val result: Try<ResponseModel> = Try.success(expectedResponseModel)
            args[0]?.tryCast<ResultListener<Try<ResponseModel>>> { onResult(result) }
        }

        every {
            mockCrypto.verify(
                expectedResponseModel.body!!.toByteArray(),
                "signature"
            )
        } returns true

        configInternal.refreshRemoteConfig(mockCompletionListener)

        verify { mockCompletionListener.onCompleted(any()) }
        verify { mockCrypto.verify(expectedResponseModel.body!!.toByteArray(), "signature") }
        verify { mockConfigResponseMapper.map(any()) }
        verify { (configInternal as DefaultConfigInternal).applyRemoteConfig(expectedRemoteConfig) }
    }

    @Test
    fun testRefreshRemoteConfig_verifyAndResetRemoteConfigCalled_onVerificationFailed() {
        val expectedRemoteConfig = RemoteConfig(eventServiceUrl = "https://test.emarsys.com")
        val expectedResponseModel = ResponseModel.Builder().body(
            """
                   {
                        "serviceUrls":{
                                "eventService":"https://test.emarsys.com"
                        }
                   }
               """.trimIndent()
        )
            .statusCode(200)
            .requestModel(mockRequestModel)
            .message("responseMessage").build()
        every { mockConfigResponseMapper.map(expectedResponseModel) } returns expectedRemoteConfig

        every {
            mockCrypto.verify(
                expectedResponseModel.body!!.toByteArray(),
                "signature"
            )
        } returns false

        every { (configInternal as DefaultConfigInternal).fetchRemoteConfigSignature(any()) } answers {
            val result: Try<String> = Try.success("signature")
            args[0]?.tryCast<ResultListener<Try<String>>> { onResult(result) }
        }

        every { (configInternal as DefaultConfigInternal).fetchRemoteConfig(any()) } answers {
            val result: Try<ResponseModel> = Try.success(expectedResponseModel)
            args[0]?.tryCast<ResultListener<Try<ResponseModel>>> { onResult(result) }
        }

        configInternal.refreshRemoteConfig(mockCompletionListener)

        verify { mockCompletionListener.onCompleted(any()) }
        verify { mockCrypto.verify(expectedResponseModel.body!!.toByteArray(), "signature") }
        verify { (configInternal as DefaultConfigInternal).resetRemoteConfig() }
        confirmVerified(mockConfigResponseMapper)
        verify(exactly = 0) {
            (configInternal as DefaultConfigInternal).applyRemoteConfig(
                expectedRemoteConfig
            )
        }
    }

    @Test
    fun testRefreshRemoteConfig_verifyResetRemoteConfigCalled_onFailure() {
        val expectedException: Exception = mockk(relaxed = true)

        every { (configInternal as DefaultConfigInternal).fetchRemoteConfigSignature(any()) } answers {
            val result: Try<String> = Try.success("signature")
            args[0]?.tryCast<ResultListener<Try<String>>> { onResult(result) }
        }

        every { (configInternal as DefaultConfigInternal).fetchRemoteConfig(any()) } answers {
            val result = Try.failure<ResponseModel>(expectedException)
            args[0]?.tryCast<ResultListener<Try<ResponseModel>>> { onResult(result) }
        }

        configInternal.refreshRemoteConfig(mockCompletionListener)

        verify { mockCompletionListener.onCompleted(any()) }
        verify { (configInternal as DefaultConfigInternal).resetRemoteConfig() }
    }

    @Test
    fun testChangeApplicationCode_shouldCallCompletionListener_onMainThread() {
        val latch = CountDownLatch(1)
        val threadSpy = ThreadSpy<Any>()

        configInternal.changeApplicationCode(null) {
            threadSpy.call()
            latch.countDown()
        }

        latch.await()
        threadSpy.verifyCalledOnMainThread()
    }

    private fun requestManagerWithRestClient(restClient: RestClient): RequestManager {
        val mockDelegatorCompletionHandlerProvider: DelegatorCompletionHandlerProvider =
            mockk(relaxed = true)
        every { mockDelegatorCompletionHandlerProvider.provide(any(), any()) } answers {
            args[1] as CoreCompletionHandler
        }

        val mockProvider: CompletionHandlerProxyProvider = mockk(relaxed = true)
        every { mockProvider.provideProxy(any(), any()) } answers {
            args[1] as CoreCompletionHandler
        }

        return RequestManager(
            ConcurrentHandlerHolderFactory.create(),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            restClient,
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockProvider,
            mockDelegatorCompletionHandlerProvider
        )
    }
}