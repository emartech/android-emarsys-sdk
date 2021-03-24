package com.emarsys.config

import com.emarsys.EmarsysRequestModelFactory
import com.emarsys.common.feature.InnerFeature
import com.emarsys.config.model.RemoteConfig
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Registry
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.crypto.Crypto
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.util.log.LogLevel
import com.emarsys.fake.FakeRestClient
import com.emarsys.fake.FakeResultListener
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.push.PushTokenProvider
import com.emarsys.predict.PredictInternal
import com.emarsys.predict.request.PredictRequestContext
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.concurrent.CountDownLatch

class DefaultConfigInternalTest {
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
    private lateinit var mockPushTokenProvider: PushTokenProvider
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
    private lateinit var mockInboxServiceStorage: StringStorage
    private lateinit var mockMobileEngageV2ServiceStorage: StringStorage
    private lateinit var mockPredictServiceStorage: StringStorage
    private lateinit var mockMessageInboxServiceStorage: StringStorage
    private lateinit var mockLogLevelStorage: StringStorage
    private lateinit var mockCrypto: Crypto
    private lateinit var mockClientServiceInternal: ClientServiceInternal

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        FeatureTestUtils.resetFeatures()

        latch = CountDownLatch(1)

        mockPushTokenProvider = mock {
            on { providePushToken() } doReturn PUSH_TOKEN
        }

        mockPredictRequestContext = mock() {
            on { merchantId } doReturn MERCHANT_ID
        }

        mockMobileEngageRequestContext = mock() {
            on { applicationCode } doReturn APPLICATION_CODE
            on { contactFieldId } doReturn CONTACT_FIELD_ID
        }
        mockMobileEngageInternal = mock() {
            on { clearContact(any()) } doAnswer { invocation ->
                (invocation.getArgument(0) as CompletionListener?)?.onCompleted(null)
            }
            on { setContact(any(), any()) } doAnswer { invocation ->
                (invocation.getArgument(1) as CompletionListener?)?.onCompleted(null)
            }
        }
        mockPushInternal = mock {
            on { setPushToken(any(), any()) } doAnswer { invocation ->
                (invocation.getArgument(1) as CompletionListener?)?.onCompleted(null)
            }
            on { clearPushToken(any()) } doAnswer { invocation ->
                (invocation.getArgument(0) as CompletionListener).onCompleted(null)
            }
        }

        mockRequestModel = mock()

        mockEmarsysRequestModelFactory = mock {
            on { createRemoteConfigRequest() } doReturn mockRequestModel
            on { createRemoteConfigSignatureRequest() } doReturn mockRequestModel
        }

        mockRequestManager = mock()

        mockPredictInternal = mock()

        mockDeviceInfo = mock()

        mockConfigResponseMapper = mock()

        mockResponseModel = mock()
        mockClientServiceStorage = mock()
        mockEventServiceStorage = mock()
        mockDeeplinkServiceStorage = mock()
        mockInboxServiceStorage = mock()
        mockMobileEngageV2ServiceStorage = mock()
        mockPredictServiceStorage = mock()
        mockMessageInboxServiceStorage = mock()
        mockLogLevelStorage = mock()
        mockCrypto = mock()
        mockClientServiceInternal = mock {
            on { trackDeviceInfo(any()) } doAnswer { invocation ->
                (invocation.getArgument(0) as CompletionListener?)?.onCompleted(null)
            }
        }

        configInternal = spy(DefaultConfigInternal(mockMobileEngageRequestContext,
                mockMobileEngageInternal,
                mockPushInternal,
                mockPushTokenProvider,
                mockPredictRequestContext,
                mockDeviceInfo,
                mockRequestManager,
                mockEmarsysRequestModelFactory,
                mockConfigResponseMapper,
                mockClientServiceStorage,
                mockEventServiceStorage,
                mockDeeplinkServiceStorage,
                mockInboxServiceStorage,
                mockMobileEngageV2ServiceStorage,
                mockPredictServiceStorage,
                mockMessageInboxServiceStorage,
                mockLogLevelStorage,
                mockCrypto,
                mockClientServiceInternal))
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
    fun testChangeApplicationCode_shouldSavePushTokenToInternal() {

        val latch = CountDownLatch(1)
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CONTACT_FIELD_ID, CompletionListener {
            latch.countDown()
        })

        latch.await()

        val result = ReflectionTestUtils.getInstanceField<String>(configInternal, "originalPushToken")

        result shouldBe PUSH_TOKEN
    }

    @Test
    fun testChangeApplicationCode_shouldCallClearContact() {
        whenever(mockMobileEngageRequestContext.hasContactIdentification()).thenReturn(true)
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CONTACT_FIELD_ID, CompletionListener { })

        verify(mockMobileEngageInternal).clearContact(any())
    }

    @Test
    fun testChangeApplicationCode_shouldChangeApplicationCodeAfterClearContact() {
        val latch = CountDownLatch(1)

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CONTACT_FIELD_ID, CompletionListener {
            latch.countDown()
        })
        latch.await()
        val inOrder = inOrder(mockMobileEngageInternal, mockPushInternal, mockMobileEngageRequestContext)
        inOrder.verify(mockPushInternal).clearPushToken(any())
        inOrder.verify(mockMobileEngageInternal).clearContact(any())
        inOrder.verify(mockMobileEngageRequestContext).applicationCode = OTHER_APPLICATION_CODE
        inOrder.verify(mockPushInternal).setPushToken(eq(PUSH_TOKEN), any())
    }

    @Test
    fun testChangeApplicationCode_shouldInterruptFlow_andDisableFeature_whenErrorHappenedDuringClearContact() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)
        val mockMobileEngageInternal: MobileEngageInternal = mock()
        whenever(mockMobileEngageInternal.clearContact(any())).thenAnswer { invocation ->
            (invocation.getArgument(0) as CompletionListener).onCompleted(Throwable())
        }

        configInternal = DefaultConfigInternal(mockMobileEngageRequestContext,
                mockMobileEngageInternal,
                mockPushInternal,
                mockPushTokenProvider,
                mockPredictRequestContext,
                mockDeviceInfo,
                mockRequestManager,
                mockEmarsysRequestModelFactory,
                mockConfigResponseMapper,
                mockClientServiceStorage,
                mockEventServiceStorage,
                mockDeeplinkServiceStorage,
                mockInboxServiceStorage,
                mockMobileEngageV2ServiceStorage,
                mockPredictServiceStorage,
                mockMessageInboxServiceStorage,
                mockLogLevelStorage,
                mockCrypto,
                mockClientServiceInternal)
        val latch = CountDownLatch(1)
        val completionListener = CompletionListener {
            latch.countDown()
        }
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CONTACT_FIELD_ID, completionListener)
        latch.await()
        verify(mockMobileEngageRequestContext).applicationCode
        verify(mockMobileEngageRequestContext).contactFieldId
        verify(mockMobileEngageRequestContext).hasContactIdentification()
        verify(mockPushInternal).clearPushToken(any())
        verify(mockMobileEngageInternal).clearContact(any())
        verifyNoMoreInteractions(mockMobileEngageInternal)
        verifyNoMoreInteractions(mockPushInternal)
        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
        verify(mockMobileEngageRequestContext).applicationCode = null
        verify(mockMobileEngageRequestContext).contactFieldId = CONTACT_FIELD_ID
        verifyNoMoreInteractions(mockMobileEngageRequestContext)
    }

    @Test
    fun testChangeApplicationCode_shouldDoOnlyLogin_whenApplicationCode_wasNull() {
        val latch = CountDownLatch(1)

        val mockMobileEngageRequestContext: MobileEngageRequestContext = mock {
            on {
                applicationCode
            } doReturn null
            on { contactFieldId } doReturn CONTACT_FIELD_ID
        }

        configInternal = DefaultConfigInternal(mockMobileEngageRequestContext,
                mockMobileEngageInternal,
                mockPushInternal,
                mockPushTokenProvider,
                mockPredictRequestContext,
                mockDeviceInfo,
                mockRequestManager,
                mockEmarsysRequestModelFactory,
                mockConfigResponseMapper,
                mockClientServiceStorage,
                mockEventServiceStorage,
                mockDeeplinkServiceStorage,
                mockInboxServiceStorage,
                mockMobileEngageV2ServiceStorage,
                mockPredictServiceStorage,
                mockMessageInboxServiceStorage,
                mockLogLevelStorage,
                mockCrypto,
                mockClientServiceInternal)

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CONTACT_FIELD_ID) {
            latch.countDown()
        }

        latch.await()

        verify(mockMobileEngageRequestContext).applicationCode
        verify(mockMobileEngageRequestContext).applicationCode = OTHER_APPLICATION_CODE
        verify(mockPushInternal).setPushToken(eq(PUSH_TOKEN), any())

        verify(mockMobileEngageInternal, times(1)).clearContact(any())
    }

    @Test
    fun testChangeApplicationCode_shouldInterruptFlow_andDisableFeature_whenErrorHappenedDuringSetPushToken() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        val mockPushInternal: PushInternal = mock {
            on {
                setPushToken(any(), any())
            } doAnswer { invocation ->
                (invocation.getArgument(1) as CompletionListener).onCompleted(Throwable())
            }
            on {
                clearPushToken(any())
            } doAnswer { invocation ->
                (invocation.getArgument(0) as CompletionListener).onCompleted(null)
            }
        }
        configInternal = DefaultConfigInternal(mockMobileEngageRequestContext,
                mockMobileEngageInternal,
                mockPushInternal,
                mockPushTokenProvider,
                mockPredictRequestContext,
                mockDeviceInfo,
                mockRequestManager,
                mockEmarsysRequestModelFactory,
                mockConfigResponseMapper,
                mockClientServiceStorage,
                mockEventServiceStorage,
                mockDeeplinkServiceStorage,
                mockInboxServiceStorage,
                mockMobileEngageV2ServiceStorage,
                mockPredictServiceStorage,
                mockMessageInboxServiceStorage,
                mockLogLevelStorage,
                mockCrypto,
                mockClientServiceInternal)
        whenever(mockMobileEngageRequestContext.contactFieldId).thenReturn(1)

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CONTACT_FIELD_ID) {
            latch.countDown()
        }
        latch.await()

        verify(mockMobileEngageInternal).clearContact(any())
        verify(mockPushInternal).setPushToken(eq(PUSH_TOKEN), any())
        verifyNoMoreInteractions(mockMobileEngageInternal)
        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
        verify(mockMobileEngageRequestContext).applicationCode = null
        verify(mockMobileEngageRequestContext).contactFieldId = 1
    }

    @Test
    fun testChangeApplicationCode_shouldWorkWithoutCompletionListener() {
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CONTACT_FIELD_ID, null)

        val inOrder = inOrder(mockMobileEngageInternal, mockPushInternal, mockMobileEngageRequestContext)
        inOrder.verify(mockPushInternal, timeout(50)).clearPushToken(any())
        inOrder.verify(mockMobileEngageInternal, timeout(50)).clearContact(any())
        inOrder.verify(mockMobileEngageRequestContext, timeout(50)).applicationCode = OTHER_APPLICATION_CODE
        inOrder.verify(mockPushInternal, timeout(50)).setPushToken(eq(PUSH_TOKEN), any())
    }

    @Test
    fun testChangeApplicationCode_shouldEnableFeature() {
        configInternal.changeApplicationCode(APPLICATION_CODE, CONTACT_FIELD_ID, CompletionListener {
            latch.countDown()
        })

        latch.await()

        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe true
        FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4) shouldBe true
    }

    @Test
    fun testChangeApplicationCode_shouldDisableFeature() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        configInternal.changeApplicationCode(null, CONTACT_FIELD_ID, CompletionListener {
            latch.countDown()
        })

        latch.await()

        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
    }

    @Test
    fun testChangeApplicationCode_whenClearPushToken_returnsWithError_callHandleError() {
        whenever(mockPushInternal.clearPushToken(any())).thenAnswer { invocation ->
            (invocation.getArgument(0) as CompletionListener).onCompleted(Throwable())
        }
        val latch = CountDownLatch(1)
        val completionListener = CompletionListener {
            latch.countDown()
        }
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CONTACT_FIELD_ID, completionListener)
        latch.await()

        verify(mockMobileEngageRequestContext).contactFieldId
        verifyZeroInteractions(mockMobileEngageInternal)
        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
        verify(mockMobileEngageRequestContext).applicationCode = null
        verify(mockMobileEngageRequestContext).contactFieldId = CONTACT_FIELD_ID
    }

    @Test
    fun testChangeApplicationCode_whenClearPushToken_butPushTokenHasNotBeenSetPreviously_callOnSuccess() {
        whenever(mockPushTokenProvider.providePushToken()).thenReturn(null)
        whenever(mockPushInternal.clearPushToken(any())).thenAnswer { invocation ->
            (invocation.getArgument(0) as CompletionListener).onCompleted(Throwable())
        }
        val latch = CountDownLatch(1)
        var result: Throwable? = null

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CONTACT_FIELD_ID) {
            result = it
            latch.countDown()
        }
        latch.await()

        result shouldBe null
    }

    @Test
    fun testChangeApplicationCode_shouldOnlyLogout_whenApplicationCodeIsNull() {
        val latch = CountDownLatch(1)
        val completionListener = CompletionListener {
            latch.countDown()
        }
        configInternal.changeApplicationCode(null, CONTACT_FIELD_ID, completionListener)
        latch.await()
        verify(mockMobileEngageRequestContext).contactFieldId
        verify(mockMobileEngageRequestContext).applicationCode
        verify(mockMobileEngageRequestContext).hasContactIdentification()
        verify(mockPushInternal).clearPushToken(any())
        verify(mockMobileEngageInternal).clearContact(any())
        verifyNoMoreInteractions(mockMobileEngageRequestContext)
        verifyNoMoreInteractions(mockPushInternal)
        verifyNoMoreInteractions(mockMobileEngageInternal)
        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
    }

    @Test
    fun testChangeApplicationCode_shouldNotSendPushToken_whenPushTokenIsNull() {
        whenever(mockPushTokenProvider.providePushToken()).thenReturn(null)
        val latch = CountDownLatch(1)

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CONTACT_FIELD_ID, CompletionListener {
            latch.countDown()
        })
        latch.await()
        val inOrder = inOrder(mockMobileEngageInternal, mockPushInternal, mockMobileEngageRequestContext, mockClientServiceInternal)
        inOrder.verify(mockMobileEngageInternal).clearContact(any())
        inOrder.verify(mockMobileEngageRequestContext).applicationCode = OTHER_APPLICATION_CODE
        inOrder.verify(mockClientServiceInternal).trackDeviceInfo(any())
        verifyZeroInteractions(mockPushInternal)
    }

    @Test
    fun testChangeApplicationCode_shouldClearContactAfter_ifContactWasNotSet() {
        val latch = CountDownLatch(1)
        whenever(mockMobileEngageRequestContext.hasContactIdentification()).doReturn(false)
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CONTACT_FIELD_ID, CompletionListener {
            latch.countDown()
        })
        latch.await()
        val inOrder = inOrder(mockMobileEngageInternal, mockMobileEngageRequestContext, mockClientServiceInternal)
        inOrder.verify(mockMobileEngageInternal).clearContact(any())
        inOrder.verify(mockMobileEngageRequestContext).applicationCode = OTHER_APPLICATION_CODE
        inOrder.verify(mockClientServiceInternal).trackDeviceInfo(any())
        inOrder.verify(mockMobileEngageInternal).clearContact(any())
    }

    @Test
    fun testChangeApplicationCode_shouldNotClearContactAfter_ifContactWasSet() {
        val latch = CountDownLatch(1)
        whenever(mockMobileEngageRequestContext.hasContactIdentification()).doReturn(true)
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CONTACT_FIELD_ID, CompletionListener {
            latch.countDown()
        })
        latch.await()
        val inOrder = inOrder(mockMobileEngageInternal, mockMobileEngageRequestContext, mockClientServiceInternal)
        inOrder.verify(mockMobileEngageInternal).clearContact(any())
        inOrder.verify(mockMobileEngageRequestContext).applicationCode = OTHER_APPLICATION_CODE
        inOrder.verify(mockClientServiceInternal).trackDeviceInfo(any())
        verifyNoMoreInteractions(mockMobileEngageInternal)
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

        verify(mockPredictRequestContext).merchantId = null
        FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT) shouldBe false
    }

    @Test
    fun testChangeMerchantId_shouldSaveMerchantId() {
        configInternal.changeMerchantId(MERCHANT_ID)

        verify(mockPredictRequestContext).merchantId = MERCHANT_ID
    }

    @Test
    fun testGetHardwareId_shouldReturnHWIDFromDeviceInfo() {
        whenever(mockDeviceInfo.hardwareId).thenReturn("testHardwareId")
        val result = configInternal.hardwareId
        result shouldBe "testHardwareId"
    }

    @Test
    fun testGetHardwareId_shouldReturnLanguageCodeFromDeviceInfo() {
        whenever(mockDeviceInfo.language).thenReturn("testLanguage")
        val result = configInternal.language

        result shouldBe "testLanguage"
    }

    @Test
    fun testGetHardwareId_shouldReturnNotificationSettingsFromDeviceInfo() {
        val notificationSettings: NotificationSettings = mock()
        whenever(mockDeviceInfo.notificationSettings).thenReturn(notificationSettings)
        val result = configInternal.notificationSettings

        result shouldBe notificationSettings
    }

    @Test
    fun testIsAutomaticPushSendingEnabled_shouldReturnValueFromDeviceInfo() {
        whenever(mockDeviceInfo.isAutomaticPushSendingEnabled).thenReturn(true)
        val result = configInternal.isAutomaticPushSendingEnabled

        result shouldBe true
        verify(mockDeviceInfo).isAutomaticPushSendingEnabled
    }

    @Test
    fun testSdkVersion_shouldReturnSdkVersionFromDeviceInfo() {
        whenever(mockDeviceInfo.sdkVersion).thenReturn(SDK_VERSION)
        val result = configInternal.sdkVersion

        result shouldBe SDK_VERSION
        verify(mockDeviceInfo).sdkVersion
    }

    @Test
    fun testRefreshRemoteConfig_shouldNotFetch_when_applicationCode_isNull() {
        whenever(mockMobileEngageRequestContext.applicationCode).thenReturn(null)

        (configInternal as DefaultConfigInternal).refreshRemoteConfig(null)

        verifyZeroInteractions(mockEmarsysRequestModelFactory)
        verifyZeroInteractions(mockRequestManager)
    }

    @Test
    fun testFetchRemoteConfig_shouldCallRequestManager_withCorrectRequestModel() {
        val requestModel: RequestModel = mock()
        whenever(mockEmarsysRequestModelFactory.createRemoteConfigRequest()).thenReturn(requestModel)

        (configInternal as DefaultConfigInternal).fetchRemoteConfig(ResultListener { })

        verify(mockRequestManager).submitNow(eq(requestModel), any())
    }

    @Test
    fun testFetchRemoteConfigSignature_shouldCallRequestManager_withCorrectRequestModel() {
        val requestModel: RequestModel = mock()
        whenever(mockEmarsysRequestModelFactory.createRemoteConfigSignatureRequest()).thenReturn(requestModel)

        configInternal.refreshRemoteConfig(null)

        verify(mockRequestManager).submitNow(eq(requestModel), any())
    }

    @Test
    fun testFetchRemoteConfig_shouldCallConfigResponseMapper_onSuccess() {
        val resultListener = FakeResultListener<ResponseModel>(latch, FakeResultListener.Mode.MAIN_THREAD)

        val configInternal = DefaultConfigInternal(mockMobileEngageRequestContext,
                mockMobileEngageInternal,
                mockPushInternal,
                mockPushTokenProvider,
                mockPredictRequestContext,
                mockDeviceInfo,
                requestManagerWithRestClient(FakeRestClient(mockResponseModel, FakeRestClient.Mode.SUCCESS)),
                mockEmarsysRequestModelFactory,
                mockConfigResponseMapper,
                mockClientServiceStorage,
                mockEventServiceStorage,
                mockDeeplinkServiceStorage,
                mockInboxServiceStorage,
                mockMobileEngageV2ServiceStorage,
                mockPredictServiceStorage,
                mockMessageInboxServiceStorage,
                mockLogLevelStorage,
                mockCrypto,
                mockClientServiceInternal)

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
                mockPushTokenProvider,
                mockPredictRequestContext,
                mockDeviceInfo,
                requestManagerWithRestClient(FakeRestClient(mockResponseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL)),
                mockEmarsysRequestModelFactory,
                mockConfigResponseMapper,
                mockClientServiceStorage,
                mockEventServiceStorage,
                mockDeeplinkServiceStorage,
                mockInboxServiceStorage,
                mockMobileEngageV2ServiceStorage,
                mockPredictServiceStorage,
                mockMessageInboxServiceStorage,
                mockLogLevelStorage,
                mockCrypto,
                mockClientServiceInternal)

        val resultListener = FakeResultListener<ResponseModel>(latch, FakeResultListener.Mode.MAIN_THREAD)
        (configInternal as DefaultConfigInternal).fetchRemoteConfig(resultListener)

        latch.await()

        resultListener.errorCount shouldBe 1
    }

    @Test
    fun testFetchRemoteConfig_shouldCallConfigResponseMapper_onException() {
        val mockException: Exception = mock()

        val configInternal = DefaultConfigInternal(
                mockMobileEngageRequestContext,
                mockMobileEngageInternal,
                mockPushInternal,
                mockPushTokenProvider,
                mockPredictRequestContext,
                mockDeviceInfo,
                requestManagerWithRestClient(FakeRestClient(mockException)),
                mockEmarsysRequestModelFactory,
                mockConfigResponseMapper,
                mockClientServiceStorage,
                mockEventServiceStorage,
                mockDeeplinkServiceStorage,
                mockInboxServiceStorage,
                mockMobileEngageV2ServiceStorage,
                mockPredictServiceStorage,
                mockMessageInboxServiceStorage,
                mockLogLevelStorage,
                mockCrypto,
                mockClientServiceInternal)

        val resultListener = FakeResultListener<ResponseModel>(latch, FakeResultListener.Mode.MAIN_THREAD)

        configInternal.fetchRemoteConfig(resultListener)

        latch.await()

        resultListener.errorCount shouldBe 1
    }

    @Test
    fun testFetchRemoteConfigSignature_shouldCallConfigSignatureResultParser_onSuccess() {
        val expectedResult = "signature"
        val resultListener = FakeResultListener<String>(latch, FakeResultListener.Mode.MAIN_THREAD)
        whenever(mockResponseModel.body).thenReturn("signature")

        val configInternal = DefaultConfigInternal(mockMobileEngageRequestContext,
                mockMobileEngageInternal,
                mockPushInternal,
                mockPushTokenProvider,
                mockPredictRequestContext,
                mockDeviceInfo,
                requestManagerWithRestClient(FakeRestClient(mockResponseModel, FakeRestClient.Mode.SUCCESS)),
                mockEmarsysRequestModelFactory,
                mockConfigResponseMapper,
                mockClientServiceStorage,
                mockEventServiceStorage,
                mockDeeplinkServiceStorage,
                mockInboxServiceStorage,
                mockMobileEngageV2ServiceStorage,
                mockPredictServiceStorage,
                mockMessageInboxServiceStorage,
                mockLogLevelStorage,
                mockCrypto,
                mockClientServiceInternal)

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
                mockPushTokenProvider,
                mockPredictRequestContext,
                mockDeviceInfo,
                requestManagerWithRestClient(FakeRestClient(mockResponseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL)),
                mockEmarsysRequestModelFactory,
                mockConfigResponseMapper,
                mockClientServiceStorage,
                mockEventServiceStorage,
                mockDeeplinkServiceStorage,
                mockInboxServiceStorage,
                mockMobileEngageV2ServiceStorage,
                mockPredictServiceStorage,
                mockMessageInboxServiceStorage,
                mockLogLevelStorage,
                mockCrypto,
                mockClientServiceInternal)

        val resultListener = FakeResultListener<String>(latch, FakeResultListener.Mode.MAIN_THREAD)
        (configInternal as DefaultConfigInternal).fetchRemoteConfigSignature(resultListener)

        latch.await()

        resultListener.errorCount shouldBe 1
    }

    @Test
    fun testFetchRemoteConfigSignature_shouldCallConfigResponseMapper_onException() {
        val mockException: Exception = mock()

        val configInternal = DefaultConfigInternal(
                mockMobileEngageRequestContext,
                mockMobileEngageInternal,
                mockPushInternal,
                mockPushTokenProvider,
                mockPredictRequestContext,
                mockDeviceInfo,
                requestManagerWithRestClient(FakeRestClient(mockException)),
                mockEmarsysRequestModelFactory,
                mockConfigResponseMapper,
                mockClientServiceStorage,
                mockEventServiceStorage,
                mockDeeplinkServiceStorage,
                mockInboxServiceStorage,
                mockMobileEngageV2ServiceStorage,
                mockPredictServiceStorage,
                mockMessageInboxServiceStorage,
                mockLogLevelStorage,
                mockCrypto,
                mockClientServiceInternal)

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

        verify(mockClientServiceStorage).set(CLIENT_SERVICE_URL)
        verify(mockEventServiceStorage).set(null)
        verify(mockDeeplinkServiceStorage).set(null)
        verify(mockInboxServiceStorage).set(null)
        verify(mockMobileEngageV2ServiceStorage).set(null)
        verify(mockPredictServiceStorage).set(null)
        verify(mockMessageInboxServiceStorage).set(null)
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

        verify(mockClientServiceStorage).set(CLIENT_SERVICE_URL)
        verify(mockEventServiceStorage).set(EVENT_SERVICE_URL)
        verify(mockDeeplinkServiceStorage).set(DEEPLINK_SERVICE_URL)
        verify(mockInboxServiceStorage).set(INBOX_SERVICE_URL)
        verify(mockMobileEngageV2ServiceStorage).set(MOBILE_ENGAGE_V2_SERVICE_URL)
        verify(mockPredictServiceStorage).set(PREDICT_SERVICE_URL)
        verify(mockMessageInboxServiceStorage).set(MESSAGE_INBOX_SERVICE_URL)
        verify(mockLogLevelStorage).set("DEBUG")
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

        verify(mockClientServiceStorage).set(null)
        verify(mockEventServiceStorage).set(null)
        verify(mockDeeplinkServiceStorage).set(null)
        verify(mockInboxServiceStorage).set(null)
        verify(mockMobileEngageV2ServiceStorage).set(null)
        verify(mockPredictServiceStorage).set(null)
        verify(mockMessageInboxServiceStorage).set(null)
        verify(mockLogLevelStorage).set(null)
    }

    @Test
    fun testRefreshRemoteConfig_shouldNotCall_fetchRemoteConfig_andCallResetRemoteConfig_ifSignatureFetchFailed() {
        val expectedException: Exception = mock()

        doAnswer {
            val result: Try<String> = Try.failure(expectedException)
            (it.arguments[0] as ResultListener<Try<String>>).onResult(result)
        }.whenever(configInternal as DefaultConfigInternal).fetchRemoteConfigSignature(any())

        configInternal.refreshRemoteConfig(null)
        verify(configInternal as DefaultConfigInternal).resetRemoteConfig()
        verify(configInternal as DefaultConfigInternal, times(0)).fetchRemoteConfig(any())
    }

    @Test
    fun testRefreshRemoteConfig_verifyApplyRemoteConfigCalled_onSuccess() {
        val expectedRemoteConfig = RemoteConfig(eventServiceUrl = "https://test.emarsys.com")
        val expectedResponseModel = ResponseModel.Builder().body("""
                   {
                        "serviceUrls":{
                                "eventService":"https://test.emarsys.com"
                        }
                   }
               """.trimIndent())
                .statusCode(200)
                .requestModel(mockRequestModel)
                .message("responseMessage").build()
        whenever(mockConfigResponseMapper.map(expectedResponseModel)).thenReturn(expectedRemoteConfig)

        doAnswer {
            val result: Try<String> = Try.success("signature")
            (it.arguments[0] as ResultListener<Try<String>>).onResult(result)
        }.whenever(configInternal as DefaultConfigInternal).fetchRemoteConfigSignature(any())

        doAnswer {
            val result: Try<ResponseModel> = Try.success(expectedResponseModel)
            (it.arguments[0] as ResultListener<Try<ResponseModel>>).onResult(result)
        }.whenever(configInternal as DefaultConfigInternal).fetchRemoteConfig(any())
        whenever(mockCrypto.verify(expectedResponseModel.body.toByteArray(), "signature")).thenReturn(true)

        configInternal.refreshRemoteConfig(null)

        verify(mockCrypto).verify(expectedResponseModel.body.toByteArray(), "signature")
        verify(mockConfigResponseMapper).map(any())
        verify((configInternal as DefaultConfigInternal)).applyRemoteConfig(expectedRemoteConfig)
    }

    @Test
    fun testRefreshRemoteConfig_verifyAndResetRemoteConfigCalled_onVerificationFailed() {
        val expectedRemoteConfig = RemoteConfig(eventServiceUrl = "https://test.emarsys.com")
        val expectedResponseModel = ResponseModel.Builder().body("""
                   {
                        "serviceUrls":{
                                "eventService":"https://test.emarsys.com"
                        }
                   }
               """.trimIndent())
                .statusCode(200)
                .requestModel(mockRequestModel)
                .message("responseMessage").build()
        whenever(mockConfigResponseMapper.map(expectedResponseModel)).thenReturn(expectedRemoteConfig)
        whenever(mockCrypto.verify(expectedResponseModel.body.toByteArray(), "signature")).thenReturn(false)

        doAnswer {
            val result: Try<String> = Try.success("signature")
            (it.arguments[0] as ResultListener<Try<String>>).onResult(result)
        }.whenever(configInternal as DefaultConfigInternal).fetchRemoteConfigSignature(any())

        doAnswer {
            val result: Try<ResponseModel> = Try.success(expectedResponseModel)
            (it.arguments[0] as ResultListener<Try<ResponseModel>>).onResult(result)
        }.whenever(configInternal as DefaultConfigInternal).fetchRemoteConfig(any())

        configInternal.refreshRemoteConfig(null)

        verify(mockCrypto).verify(expectedResponseModel.body.toByteArray(), "signature")
        verify((configInternal as DefaultConfigInternal)).resetRemoteConfig()
        verifyZeroInteractions(mockConfigResponseMapper)
        verify((configInternal as DefaultConfigInternal), times(0)).applyRemoteConfig(expectedRemoteConfig)
    }

    @Test
    fun testRefreshRemoteConfig_verifyResetRemoteConfigCalled_onFailure() {
        val expectedException: Exception = mock()

        doAnswer {
            val result: Try<String> = Try.success("signature")
            (it.arguments[0] as ResultListener<Try<String>>).onResult(result)
        }.whenever(configInternal as DefaultConfigInternal).fetchRemoteConfigSignature(any())

        doAnswer {
            val result = Try.failure<ResponseModel>(expectedException)
            (it.arguments[0] as ResultListener<Try<ResponseModel>>).onResult(result)
        }.whenever(configInternal as DefaultConfigInternal).fetchRemoteConfig(any())

        configInternal.refreshRemoteConfig(null)

        verify(configInternal as DefaultConfigInternal).resetRemoteConfig()
    }

    @Suppress("UNCHECKED_CAST")
    fun requestManagerWithRestClient(restClient: RestClient): RequestManager {
        val mockProvider: CompletionHandlerProxyProvider = mock {
            on { provideProxy(isNull(), any()) } doAnswer {
                it.arguments[1] as CoreCompletionHandler
            }
            on { provideProxy(any(), any()) } doAnswer {
                it.arguments[1] as CoreCompletionHandler
            }
        }

        return RequestManager(
                mock(),
                mock() as Repository<RequestModel, SqlSpecification>,
                mock() as Repository<ShardModel, SqlSpecification>,
                mock(),
                restClient,
                mock() as Registry<RequestModel, CompletionListener>,
                mock(),
                mockProvider
        )
    }
}