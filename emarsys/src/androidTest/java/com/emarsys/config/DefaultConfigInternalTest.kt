package com.emarsys.config

import android.os.Handler
import com.emarsys.EmarsysRequestModelFactory
import com.emarsys.config.model.RemoteConfig
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Registry
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.Storage
import com.emarsys.core.worker.Worker
import com.emarsys.fake.FakeRestClient
import com.emarsys.fake.FakeResultListener
import com.emarsys.feature.InnerFeature
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.push.PushTokenProvider
import com.emarsys.predict.PredictInternal
import com.emarsys.predict.request.PredictRequestContext
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.anyNotNull
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.*
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import java.util.concurrent.CountDownLatch

class DefaultConfigInternalTest {
    private companion object {
        const val APPLICATION_CODE = "applicationCode"
        const val MERCHANT_ID = "merchantId"
        const val OTHER_APPLICATION_CODE = "otherApplicationCode"
        const val CONTACT_FIELD_ID = 3
        const val CONTACT_FIELD_VALUE = "originalContactFieldValue"
        const val PUSH_TOKEN = "pushToken"
        const val EVENT_SERVICE_URL = "https://event.emarsys.com"
        const val CLIENT_SERVICE_URL = "https://client.emarsys.com"
        const val DEEPLINK_SERVICE_URL = "https://deeplink.emarsys.com"
        const val INBOX_SERVICE_URL = "https://inbox.emarsys.com"
        const val MOBILE_ENGAGE_V2_SERVICE_URL = "https://mev2.emarsys.com"
        const val PREDICT_SERVICE_URL = "https://predict.emarsys.com"
    }

    private lateinit var configInternal: ConfigInternal
    private lateinit var mockMobileEngageRequestContext: MobileEngageRequestContext
    private lateinit var mockPredictRequestContext: PredictRequestContext
    private lateinit var mockMobileEngageInternal: MobileEngageInternal
    private lateinit var mockPushInternal: PushInternal
    private lateinit var mockPushTokenProvider: PushTokenProvider
    private lateinit var mockPredictInternal: PredictInternal
    private lateinit var mockContactFieldValueStorage: Storage<String>
    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var latch: CountDownLatch
    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockEmarsysRequestModelFactory: EmarsysRequestModelFactory
    private lateinit var mockConfigResponseMapper: RemoteConfigResponseMapper
    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockEventServiceStorage: Storage<String>
    private lateinit var mockClientServiceStorage: Storage<String>
    private lateinit var mockDeeplinkServiceStorage: Storage<String>
    private lateinit var mockInboxServiceStorage: Storage<String>
    private lateinit var mockMobileEngageV2ServiceStorage: Storage<String>
    private lateinit var mockPredictServiceStorage: Storage<String>

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        FeatureTestUtils.resetFeatures()

        latch = CountDownLatch(1)

        mockPushTokenProvider = mock(PushTokenProvider::class.java).apply {
            whenever(providePushToken()).thenReturn(PUSH_TOKEN)
        }

        mockContactFieldValueStorage = (mock(Storage::class.java) as Storage<String>).apply {
            whenever(get()).thenReturn(CONTACT_FIELD_VALUE).thenReturn(null)
        }

        mockPredictRequestContext = mock(PredictRequestContext::class.java).apply {
            whenever(merchantId).thenReturn(MERCHANT_ID)
        }

        mockMobileEngageRequestContext = mock(MobileEngageRequestContext::class.java).apply {
            whenever(applicationCode).thenReturn(APPLICATION_CODE)
            whenever(contactFieldValueStorage).thenReturn(mockContactFieldValueStorage)
            whenever(contactFieldId).thenReturn(CONTACT_FIELD_ID)
        }
        mockMobileEngageInternal = mock(MobileEngageInternal::class.java).apply {
            whenever(clearContact(any())).thenAnswer { invocation ->
                mockContactFieldValueStorage.get()
                (invocation.getArgument(0) as CompletionListener?)?.onCompleted(null)
            }
            whenever(setContact(any(), any())).thenAnswer { invocation ->
                (invocation.getArgument(1) as CompletionListener?)?.onCompleted(null)
            }
        }
        mockPushInternal = mock(PushInternal::class.java).apply {
            whenever(setPushToken(any(), any())).thenAnswer { invocation ->
                (invocation.getArgument(1) as CompletionListener?)?.onCompleted(null)
            }
        }

        mockRequestModel = mock(RequestModel::class.java)

        mockEmarsysRequestModelFactory = mock(EmarsysRequestModelFactory::class.java).apply {
            whenever(createRemoteConfigRequest()).thenReturn(mockRequestModel)
        }

        mockRequestManager = mock(RequestManager::class.java)

        mockPredictInternal = mock(PredictInternal::class.java)

        mockDeviceInfo = mock(DeviceInfo::class.java)

        mockConfigResponseMapper = mock(RemoteConfigResponseMapper::class.java)

        mockResponseModel = mock(ResponseModel::class.java)
        mockClientServiceStorage = mock(Storage::class.java) as Storage<String>
        mockEventServiceStorage = mock(Storage::class.java) as Storage<String>
        mockDeeplinkServiceStorage = mock(Storage::class.java) as Storage<String>
        mockInboxServiceStorage = mock(Storage::class.java) as Storage<String>
        mockMobileEngageV2ServiceStorage = mock(Storage::class.java) as Storage<String>
        mockPredictServiceStorage = mock(Storage::class.java) as Storage<String>
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
                mockPredictServiceStorage))
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
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CompletionListener { })

        verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
    }

    @Test
    fun testChangeApplicationCode_shouldCallSetContactWithOriginalContactFieldIdAndContactFieldValue() {
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CompletionListener {
            verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
            verify(mockMobileEngageInternal).setContact(eq(CONTACT_FIELD_VALUE), any())
        })
    }

    @Test
    fun testChangeApplicationCode_shouldCallSetPushToken() {
        val latch = CountDownLatch(1)
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CompletionListener {
            latch.countDown()
        })
        latch.await()

        verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
        verify(mockMobileEngageInternal).setContact(eq(CONTACT_FIELD_VALUE), any())
        verify(mockPushInternal).setPushToken(eq(PUSH_TOKEN), any())
    }

    @Test
    fun testChangeApplicationCode_shouldChangeApplicationCodeAfterClearContact() {
        val latch = CountDownLatch(1)

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CompletionListener {
            latch.countDown()
        })
        latch.await()
        val inOrder = inOrder(mockMobileEngageInternal, mockPushInternal, mockMobileEngageRequestContext)
        inOrder.verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
        inOrder.verify(mockMobileEngageRequestContext).applicationCode = OTHER_APPLICATION_CODE
        inOrder.verify(mockPushInternal).setPushToken(eq(PUSH_TOKEN), any())
        inOrder.verify(mockMobileEngageInternal).setContact(eq(CONTACT_FIELD_VALUE), any())
    }

    @Test
    fun testChangeApplicationCode_shouldInterruptFlow_andDisableFeature_whenErrorHappenedDuringClearContact() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)
        val mockMobileEngageInternal = mock(MobileEngageInternal::class.java)
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
                mockPredictServiceStorage)
        val latch = CountDownLatch(1)
        val completionListener = CompletionListener {
            latch.countDown()
        }
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, completionListener)
        latch.await()
        verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
        verify(mockMobileEngageRequestContext).applicationCode
        verify(mockMobileEngageRequestContext).contactFieldValueStorage
        verifyZeroInteractions(mockPushInternal)
        verifyNoMoreInteractions(mockMobileEngageInternal)
        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
        verify(mockMobileEngageRequestContext).applicationCode = null
        verifyNoMoreInteractions(mockMobileEngageRequestContext)

    }

    @Test
    fun testChangeApplicationCode_shouldDoOnlyLogin_whenApplicationCode_isNull() {
        val latch = CountDownLatch(1)

        val mockMobileEngageRequestContext = mock(MobileEngageRequestContext::class.java).apply {
            whenever(applicationCode).thenReturn(null)
            whenever(contactFieldValueStorage).thenReturn(mockContactFieldValueStorage)
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
                mockPredictServiceStorage)

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CompletionListener {
            latch.countDown()
        })

        latch.await()

        verify(mockMobileEngageRequestContext).contactFieldValueStorage
        verify(mockMobileEngageRequestContext).applicationCode
        verify(mockMobileEngageRequestContext).applicationCode = OTHER_APPLICATION_CODE
        verify(mockPushInternal).setPushToken(eq(PUSH_TOKEN), any())
        verify(mockMobileEngageInternal).setContact(eq(CONTACT_FIELD_VALUE), any())

        verify(mockMobileEngageInternal, times(0)).clearContact(any(CompletionListener::class.java))
    }

    @Test
    fun testChangeApplicationCode_shouldInterruptFlow_andDisableFeature_whenErrorHappenedDuringSetPushToken() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        val mockPushInternal = mock(PushInternal::class.java).apply {
            whenever(setPushToken(any(), any())).thenAnswer { invocation ->
                (invocation.getArgument(1) as CompletionListener).onCompleted(Throwable())
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
                mockPredictServiceStorage)

        val completionListener = CompletionListener {
            latch.countDown()
        }

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, completionListener)
        latch.await()

        verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
        verify(mockPushInternal).setPushToken(eq(PUSH_TOKEN), any())
        verifyNoMoreInteractions(mockMobileEngageInternal)
        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
        verify(mockMobileEngageRequestContext).applicationCode = null
    }

    @Test
    fun testChangeApplicationCode_shouldInterruptFlow_andDisableFeature_whenErrorHappenedDuringSetContact() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        val mockMobileEngageInternal = mock(MobileEngageInternal::class.java).apply {
            whenever(setContact(any(), any())).thenAnswer { invocation ->
                (invocation.getArgument(1) as CompletionListener).onCompleted(Throwable())
            }
            whenever(clearContact(any())).thenAnswer { invocation ->
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
                mockPredictServiceStorage)

        val completionListener = CompletionListener {
            latch.countDown()
        }

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, completionListener)

        latch.await()

        verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
        verify(mockPushInternal).setPushToken(eq(PUSH_TOKEN), any())
        verify(mockMobileEngageInternal).setContact(eq(CONTACT_FIELD_VALUE), any(CompletionListener::class.java))

        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
        verify(mockMobileEngageRequestContext).applicationCode = null
    }

    @Test
    fun testChangeApplicationCode_shouldWorkWithoutCompletionListener() {
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, null)

        val inOrder = inOrder(mockMobileEngageInternal, mockPushInternal, mockMobileEngageRequestContext)
        inOrder.verify(mockMobileEngageInternal, timeout(50)).clearContact(any(CompletionListener::class.java))
        inOrder.verify(mockMobileEngageRequestContext, timeout(50)).applicationCode = OTHER_APPLICATION_CODE
        inOrder.verify(mockPushInternal, timeout(50)).setPushToken(eq(PUSH_TOKEN), any())
        inOrder.verify(mockMobileEngageInternal, timeout(50)).setContact(eq(CONTACT_FIELD_VALUE), any())
    }

    @Test
    fun testChangeApplicationCode_shouldEnableFeature() {
        configInternal.changeApplicationCode(APPLICATION_CODE, CompletionListener {
            latch.countDown()
        })

        latch.await()

        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe true
    }

    @Test
    fun testChangeApplicationCode_shouldDisableFeature() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        configInternal.changeApplicationCode(null, CompletionListener {
            latch.countDown()
        })

        latch.await()

        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
    }

    @Test
    fun testChangeApplicationCode_shouldOnlyLogout_whenApplicationCodeIsNull() {
        val latch = CountDownLatch(1)
        val completionListener = CompletionListener {
            latch.countDown()
        }
        configInternal.changeApplicationCode(null, completionListener)
        latch.await()
        verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
        verify(mockMobileEngageRequestContext).contactFieldValueStorage
        verify(mockMobileEngageRequestContext).applicationCode
        verifyNoMoreInteractions(mockMobileEngageRequestContext)
        verifyZeroInteractions(mockPushInternal)
        verifyNoMoreInteractions(mockMobileEngageInternal)
    }

    @Test
    fun testChangeApplicationCode_shouldNotSendPushToken_whenPushTokenIsNull() {
        whenever(mockPushTokenProvider.providePushToken()).thenReturn(null)
        val latch = CountDownLatch(1)

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CompletionListener {
            latch.countDown()
        })
        latch.await()
        val inOrder = inOrder(mockMobileEngageInternal, mockPushInternal, mockMobileEngageRequestContext)
        inOrder.verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
        inOrder.verify(mockMobileEngageRequestContext).applicationCode = OTHER_APPLICATION_CODE
        verifyZeroInteractions(mockPushInternal)
        inOrder.verify(mockMobileEngageInternal).setContact(eq(CONTACT_FIELD_VALUE), any())
    }

    @Test
    fun testChangeApplicationCode_shouldResetRemoteConfig() {
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CompletionListener { })

        verify(configInternal).resetRemoteConfig()
    }

    @Ignore
    @Test
    fun testChangeApplicationCode_shouldRefreshRemoteConfig_whenChangeWasSuccessfull() {
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CompletionListener { })

        val inOrder = inOrder(configInternal, mockMobileEngageInternal, mockPushInternal, mockMobileEngageRequestContext)
        inOrder.verify(configInternal).resetRemoteConfig()

        inOrder.verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
        inOrder.verify(mockMobileEngageRequestContext).applicationCode = OTHER_APPLICATION_CODE
        inOrder.verify(mockPushInternal).setPushToken(eq(PUSH_TOKEN), any())
        inOrder.verify(mockMobileEngageInternal).setContact(eq(CONTACT_FIELD_VALUE), any())

        inOrder.verify(configInternal).refreshRemoteConfig()
    }

    @Ignore
    @Test
    fun testChangeApplicationCode_shouldNotRefreshRemoteConfig_whenMobileEngageIsDisabled() {
        configInternal.changeApplicationCode(null, CompletionListener { latch.countDown() })
        latch.await()

        verify(configInternal, times(0)).refreshRemoteConfig()
        verify(configInternal, times(1)).resetRemoteConfig()
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

        FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT) shouldBe false
    }

    @Test
    fun testChangeMerchantId_shouldSaveMerchantId() {
        configInternal.changeMerchantId(MERCHANT_ID)

        verify(mockPredictRequestContext).merchantId = MERCHANT_ID
    }

    @Test
    fun testChangeMerchantId_shouldResetRemoteConfig() {
        configInternal.changeMerchantId(MERCHANT_ID)
        val inOrder = inOrder(configInternal, mockPredictRequestContext)
        inOrder.verify(configInternal).resetRemoteConfig()
        inOrder.verify(mockPredictRequestContext).merchantId = MERCHANT_ID
    }

    @Ignore
    @Test
    fun testChangeMerchantId_shouldRefreshRemoteConfigAfterChange() {
        configInternal.changeMerchantId(MERCHANT_ID)

        val inOrder = inOrder(configInternal, mockPredictRequestContext)
        inOrder.verify(configInternal).resetRemoteConfig()
        inOrder.verify(mockPredictRequestContext).merchantId = MERCHANT_ID
        inOrder.verify(configInternal).refreshRemoteConfig()
    }

    @Ignore
    @Test
    fun testChangeMerchantId_shouldNotRefreshRemoteConfig_whenPredictGotDisabled() {
        configInternal.changeMerchantId(null)

        val inOrder = inOrder(configInternal, mockPredictRequestContext)
        inOrder.verify(configInternal).resetRemoteConfig()
        inOrder.verify(mockPredictRequestContext).merchantId = null
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun testGetHardwareId_shouldReturnHWIDFromDeviceInfo() {
        whenever(mockDeviceInfo.hwid).thenReturn("testHardwareId")
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
        val notificationSettings = mock(NotificationManagerHelper::class.java)
        whenever(mockDeviceInfo.notificationSettings).thenReturn(notificationSettings)
        val result = configInternal.notificationSettings

        result shouldBe notificationSettings
    }

    @Test
    fun testFetchRemoteConfig_shouldCallRequestManager_withCorrectRequestModel() {
        val requestModel = mock(RequestModel::class.java)
        whenever(mockEmarsysRequestModelFactory.createRemoteConfigRequest()).thenReturn(requestModel)

        configInternal.refreshRemoteConfig()

        verify(mockRequestManager).submitNow(eq(requestModel), anyNotNull())
    }

    @Test
    fun testFetchRemoteConfig_shouldCallConfigResponseMapper_onSuccess() {
        val expectedResult = RemoteConfig(eventServiceUrl = "https://emarsys.com")
        val resultListener = FakeResultListener<RemoteConfig>(latch, FakeResultListener.Mode.MAIN_THREAD)

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
                mockPredictServiceStorage)

        whenever(mockConfigResponseMapper.map(mockResponseModel)).thenReturn(expectedResult)

        configInternal.fetchRemoteConfig(resultListener)

        latch.await()

        verify(mockConfigResponseMapper).map(any())
        resultListener.resultStatus shouldBe expectedResult
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
                mockPredictServiceStorage)

        val resultListener = FakeResultListener<RemoteConfig>(latch, FakeResultListener.Mode.MAIN_THREAD)
        (configInternal as DefaultConfigInternal).fetchRemoteConfig(resultListener)

        latch.await()

        resultListener.errorCount shouldBe 1
    }

    @Test
    fun testFetchRemoteConfig_shouldCallConfigResponseMapper_onException() {
        val mockException = mock(Exception::class.java)

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
                mockPredictServiceStorage)

        val resultListener = FakeResultListener<RemoteConfig>(latch, FakeResultListener.Mode.MAIN_THREAD)

        configInternal.fetchRemoteConfig(resultListener)

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
    }

    @Test
    fun testApplyRemoteConfig_applyAll() {
        val remoteConfig = RemoteConfig(
                eventServiceUrl = EVENT_SERVICE_URL,
                clientServiceUrl = CLIENT_SERVICE_URL,
                deepLinkServiceUrl = DEEPLINK_SERVICE_URL,
                inboxServiceUrl = INBOX_SERVICE_URL,
                mobileEngageV2ServiceUrl = MOBILE_ENGAGE_V2_SERVICE_URL,
                predictServiceUrl = PREDICT_SERVICE_URL
        )

        (configInternal as DefaultConfigInternal).applyRemoteConfig(remoteConfig)

        verify(mockClientServiceStorage).set(CLIENT_SERVICE_URL)
        verify(mockEventServiceStorage).set(EVENT_SERVICE_URL)
        verify(mockDeeplinkServiceStorage).set(DEEPLINK_SERVICE_URL)
        verify(mockInboxServiceStorage).set(INBOX_SERVICE_URL)
        verify(mockMobileEngageV2ServiceStorage).set(MOBILE_ENGAGE_V2_SERVICE_URL)
        verify(mockPredictServiceStorage).set(PREDICT_SERVICE_URL)
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
    }

    @Ignore
    @Test
    fun testRefreshRemoteConfig_verifyApplyRemoteConfigCalled_onSuccess() {
        val expectedRemoteConfig = RemoteConfig(eventServiceUrl = "https://test.emarsys.com")

        doAnswer {
            val result: Try<RemoteConfig> = Try.success(expectedRemoteConfig)
            (it.arguments[0] as ResultListener<Try<RemoteConfig>>).onResult(result)
        }.`when`(configInternal as DefaultConfigInternal).fetchRemoteConfig(anyNotNull())

        configInternal.refreshRemoteConfig()

        verify((configInternal as DefaultConfigInternal)).applyRemoteConfig(expectedRemoteConfig)
    }

    @Ignore
    @Test
    fun testRefreshRemoteConfig_verifyResetRemoteConfigCalled_onFailure() {
        val expectedException: Exception = mock(Exception::class.java)

        doAnswer {
            val result = Try.failure<Exception>(expectedException)
            (it.arguments[0] as ResultListener<Try<Exception>>).onResult(result)
        }.`when`(configInternal as DefaultConfigInternal).fetchRemoteConfig(anyNotNull())

        configInternal.refreshRemoteConfig()

        verify(configInternal as DefaultConfigInternal).resetRemoteConfig()
    }

    @Suppress("UNCHECKED_CAST")
    private fun requestManagerWithRestClient(restClient: RestClient): RequestManager {
        return RequestManager(
                mock(Handler::class.java),
                mock(Repository::class.java) as Repository<RequestModel, SqlSpecification>,
                mock(Repository::class.java) as Repository<ShardModel, SqlSpecification>,
                mock(Worker::class.java),
                restClient,
                mock(Registry::class.java) as Registry<RequestModel, CompletionListener>,
                mock(CoreCompletionHandler::class.java)
        )
    }
}