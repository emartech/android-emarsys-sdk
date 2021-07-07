package com.emarsys

import android.app.Activity
import android.app.Application
import android.content.Intent
import com.emarsys.Emarsys.clearContact
import com.emarsys.Emarsys.setAuthenticatedContact
import com.emarsys.Emarsys.setContact
import com.emarsys.Emarsys.setup
import com.emarsys.Emarsys.trackCustomEvent
import com.emarsys.Emarsys.trackDeepLink
import com.emarsys.clientservice.ClientServiceApi
import com.emarsys.common.feature.InnerFeature
import com.emarsys.config.ConfigApi
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.api.experimental.FlipperFeature
import com.emarsys.core.api.notification.ChannelSettings
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerType
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseHandlersProcessor
import com.emarsys.core.storage.StringStorage
import com.emarsys.deeplink.DeepLinkApi
import com.emarsys.di.*
import com.emarsys.eventservice.EventServiceApi
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.MessageInboxApi
import com.emarsys.mobileengage.MobileEngageApi
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.deeplink.DeepLinkAction
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.iam.InAppStartAction
import com.emarsys.mobileengage.iam.model.requestRepositoryProxy.RequestRepositoryProxy
import com.emarsys.mobileengage.responsehandler.*
import com.emarsys.predict.PredictApi
import com.emarsys.predict.PredictRestrictedApi
import com.emarsys.predict.api.model.Logic
import com.emarsys.predict.api.model.Product
import com.emarsys.predict.api.model.RecommendationFilter
import com.emarsys.predict.response.VisitorIdResponseHandler
import com.emarsys.predict.response.XPResponseHandler
import com.emarsys.push.PushApi
import com.emarsys.testUtil.CollectionTestUtils.getElementByType
import com.emarsys.testUtil.CollectionTestUtils.numberOfElementsIn
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.ReflectionTestUtils.getInstanceField
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.*
import org.junit.rules.TestRule
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.*
import java.util.*
import java.util.concurrent.CountDownLatch

class EmarsysTest {
    companion object {
        private const val APPLICATION_CODE = "EMS11-C3FD3"
        private const val CONTACT_FIELD_ID = 3
        private const val MERCHANT_ID = "merchantId"
        private const val SDK_VERSION = "sdkVersion"
        private const val CONTACT_ID = "CONTACT_ID"
        private const val ID_TOKEN = "testIdToken"
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    val duplicateThreadRule = DuplicatedThreadRule("CoreSDKHandlerThread")

    private lateinit var activityLifecycleWatchdog: ActivityLifecycleWatchdog
    private lateinit var currentActivityWatchdog: CurrentActivityWatchdog
    private lateinit var mockCoreSQLiteDatabase: CoreSQLiteDatabase
    private lateinit var mockLogShardTrigger: Runnable
    private lateinit var mockMobileEngageInternal: MobileEngageInternal
    private lateinit var mockDeepLinkInternal: DeepLinkInternal
    private lateinit var mockDeepLinkApi: DeepLinkApi
    private lateinit var mockLoggingDeepLinkApi: DeepLinkApi
    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var mockEventServiceApi: EventServiceApi
    private lateinit var mockLoggingEventServiceApi: EventServiceApi
    private lateinit var mockClientServiceApi: ClientServiceApi
    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockPredictShardTrigger: Runnable
    private lateinit var mockDeviceInfoPayloadStorage: StringStorage
    private lateinit var mockContactFieldValueStorage: StringStorage
    private lateinit var mockContactTokenStorage: StringStorage
    private lateinit var mockClientStateStorage: StringStorage
    private lateinit var mockInApp: InAppApi
    private lateinit var mockLoggingInApp: InAppApi
    private lateinit var mockPush: PushApi
    private lateinit var mockPredict: PredictApi
    private lateinit var mockPredictRestricted: PredictRestrictedApi
    private lateinit var mockMobileEngageApi: MobileEngageApi
    private lateinit var mockLoggingMobileEngageApi: MobileEngageApi
    private lateinit var mockLoggingPredict: PredictApi
    private lateinit var mockConfig: ConfigApi
    private lateinit var mockMessageInbox: MessageInboxApi
    private lateinit var mockHardwareIdProvider: HardwareIdProvider
    private lateinit var mockLanguageProvider: LanguageProvider
    private lateinit var mockNotificationManagerHelper: NotificationSettings
    private lateinit var mockVersionProvider: VersionProvider
    private lateinit var mockLogic: Logic
    private lateinit var mockRecommendationFilter: RecommendationFilter
    private lateinit var inappEventHandler: EventHandler
    private lateinit var application: Application
    private lateinit var completionListener: CompletionListener
    private lateinit var mockResultListener: ResultListener<Try<List<Product>>>
    private lateinit var baseConfig: EmarsysConfig
    private lateinit var mobileEngageConfig: EmarsysConfig
    private lateinit var predictConfig: EmarsysConfig
    private lateinit var mobileEngageAndPredictConfig: EmarsysConfig
    private lateinit var deviceInfo: DeviceInfo
    private lateinit var latch: CountDownLatch
    private lateinit var predictResultListenerCallback: (Try<List<Product>>) -> Unit

    @Before
    fun setUp() {
        try {
            application = spy(getTargetContext().applicationContext as Application)
            completionListener = mock()
            mockResultListener = mock()
            activityLifecycleWatchdog = mock()
            currentActivityWatchdog = mock()
            mockCoreSQLiteDatabase = mock()
            mockMobileEngageInternal = mock()
            mockDeepLinkInternal = mock()
            mockDeepLinkApi = mock()
            mockLoggingDeepLinkApi = mock()
            mockEventServiceInternal = mock()
            mockEventServiceApi = mock()
            mockLoggingEventServiceApi = mock()
            mockClientServiceApi = mock()
            mockPredictShardTrigger = mock()
            mockLogShardTrigger = mock()
            mockLanguageProvider = mock()
            mockVersionProvider = mock()
            inappEventHandler = mock()
            mockDeviceInfoPayloadStorage = mock()
            mockContactFieldValueStorage = mock()
            mockContactTokenStorage = mock()
            mockClientStateStorage = mock()
            mockNotificationManagerHelper = mock()

            baseConfig = createConfig().build()
            mobileEngageConfig = createConfig()
                    .applicationCode(APPLICATION_CODE)
                    .contactFieldId(CONTACT_FIELD_ID)
                    .build()
            predictConfig = createConfig()
                    .merchantId(MERCHANT_ID)
                    .build()
            mobileEngageAndPredictConfig = createConfig()
                    .applicationCode(APPLICATION_CODE)
                    .contactFieldId(CONTACT_FIELD_ID)
                    .merchantId(MERCHANT_ID)
                    .build()
            mockRequestContext = mock()
            mockHardwareIdProvider = mock()
            mockMobileEngageApi = mock()
            mockLoggingMobileEngageApi = mock()
            mockInApp = mock()
            mockLoggingInApp = mock()
            mockPush = mock()
            mockPredict = mock()
            mockLoggingPredict = mock()
            mockPredictRestricted = mock()
            mockConfig = mock()
            mockMessageInbox = mock()
            mockLogic = mock()
            mockRecommendationFilter = mock()
            predictResultListenerCallback = mock()
            whenever(mockNotificationManagerHelper.channelSettings).thenReturn(listOf(ChannelSettings(channelId = "channelId")))
            whenever(mockNotificationManagerHelper.importance).thenReturn(-1000)
            whenever(mockNotificationManagerHelper.areNotificationsEnabled()).thenReturn(false)
            whenever(mockHardwareIdProvider.provideHardwareId()).thenReturn("hwid")
            whenever(mockLanguageProvider.provideLanguage(ArgumentMatchers.any(Locale::class.java))).thenReturn("language")
            whenever(mockVersionProvider.provideSdkVersion()).thenReturn("version")

            deviceInfo = DeviceInfo(
                    application, mockHardwareIdProvider, mockVersionProvider,
                    mockLanguageProvider, mockNotificationManagerHelper,
                    isAutomaticPushSendingEnabled = true, isGooglePlayAvailable = true
            )

            whenever(mockRequestContext.applicationCode).thenReturn(APPLICATION_CODE)
            whenever(mockRequestContext.deviceInfo).thenReturn(deviceInfo)
            whenever(mockVersionProvider.provideSdkVersion()).thenReturn(SDK_VERSION)
            whenever(mockContactFieldValueStorage.get()).thenReturn("test@test.com")
            whenever(mockContactTokenStorage.get()).thenReturn("contactToken")

            whenever(mockDeviceInfoPayloadStorage.get()).thenReturn("deviceInfo.deviceInfoPayload")

            setupEmarsysComponent(
                    FakeDependencyContainer(
                            activityLifecycleWatchdog = activityLifecycleWatchdog,
                            currentActivityWatchdog = currentActivityWatchdog,
                            coreSQLiteDatabase = mockCoreSQLiteDatabase,
                            deviceInfo = deviceInfo,
                            logShardTrigger = mockLogShardTrigger,
                            mobileEngageInternal = mockMobileEngageInternal,
                            loggingMobileEngageInternal = mockMobileEngageInternal,
                            deepLinkInternal = mockDeepLinkInternal,
                            loggingDeepLinkInternal = mockDeepLinkInternal,
                            eventServiceInternal = mockEventServiceInternal,
                            loggingEventServiceInternal = mockEventServiceInternal,
                            clientService = mockClientServiceApi,
                            loggingClientService = mockClientServiceApi,
                            requestContext = mockRequestContext,
                            predictShardTrigger = mockPredictShardTrigger,
                            deviceInfoPayloadStorage = mockDeviceInfoPayloadStorage,
                            contactFieldValueStorage = mockContactFieldValueStorage,
                            contactTokenStorage = mockContactTokenStorage,
                            clientStateStorage = mockClientStateStorage,
                            responseHandlersProcessor = ResponseHandlersProcessor(ArrayList()),
                            mobileEngage = mockMobileEngageApi,
                            loggingMobileEngage = mockLoggingMobileEngageApi,
                            inApp = mockInApp,
                            loggingInApp = mockLoggingInApp,
                            push = mockPush,
                            predict = mockPredict,
                            loggingPredict = mockLoggingPredict,
                            predictRestricted = mockPredictRestricted,
                            loggingPredictRestricted = mockPredictRestricted,
                            config = mockConfig,
                            eventService = mockEventServiceApi,
                            loggingEventService = mockLoggingEventServiceApi,
                            deepLink = mockDeepLinkApi,
                            loggingDeepLink = mockLoggingDeepLinkApi,
                            logger = mock()
                    )
            )
            latch = CountDownLatch(1)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testSetup_whenMobileEngageApplicationCodeAndMerchantIdAreNull_mobileEngageAndPredict_shouldBeDisabled() {
        val config = createConfig()
                .applicationCode(null)
                .merchantId(null)
                .build()
        setup(config)

        runBlockingOnCoreSdkThread()

        Assert.assertEquals(false, FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE))
        Assert.assertEquals(false, FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT))
    }

    @Test
    fun testSetup_whenMobileEngageApplicationCodeIsNotNull_mobileEngageFeature_shouldBeEnabled() {
        setup(mobileEngageConfig)

        Assert.assertTrue(FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE))
    }

    @Test
    fun testSetup_whenPredictMerchantIdIsNotNull_predictFeature_shouldBeEnabled() {
        setup(predictConfig)

        Assert.assertTrue(FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT))
    }

    @Test
    fun testSetup_shouldBeEnable_eventServiceV4() {
        setup(mobileEngageConfig)

        FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4) shouldBe true
    }

    @Test
    fun testSetup_eventServiceV4_shouldBeDisabled_whenMobileEngageIsDisabled() {
        setup(baseConfig)

        FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4) shouldBe false
    }

    @Test
    fun testSetup_initializesDependencyInjectionContainer() {
        IntegrationTestUtils.tearDownEmarsys()

        setup(baseConfig)

        val container = emarsys()
        Assert.assertEquals(DefaultEmarsysComponent::class.java, container.javaClass)
        Assert.assertEquals(isEmarsysComponentSetup(), true)
    }

    @Test
    fun testSetup_initializesRequestManager_withRequestModelRepositoryProxy() {
        IntegrationTestUtils.tearDownEmarsys()

        setup(mobileEngageConfig)

        var repository: Any? = null
        runBlockingOnCoreSdkThread {
            val requestManager: RequestManager = emarsys().requestManager
            repository = getInstanceField<Any>(
                    requestManager,
                    "requestRepository")
        }
        Assert.assertEquals(RequestRepositoryProxy::class.java, repository?.javaClass)
    }

    @Test
    fun testSetup_initializesCoreCompletionHandler_withNoFlippers() {
        IntegrationTestUtils.tearDownEmarsys()

        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread {
            val responseHandlersProcessor = emarsys().responseHandlersProcessor

            Assert.assertNotNull(responseHandlersProcessor)
            Assert.assertEquals(11, responseHandlersProcessor.responseHandlers.size.toLong())
            Assert.assertEquals(1, numberOfElementsIn(responseHandlersProcessor.responseHandlers, VisitorIdResponseHandler::class.java).toLong())
            Assert.assertEquals(1, numberOfElementsIn(responseHandlersProcessor.responseHandlers, XPResponseHandler::class.java).toLong())
            Assert.assertEquals(1, numberOfElementsIn(responseHandlersProcessor.responseHandlers, InAppMessageResponseHandler::class.java).toLong())
            Assert.assertEquals(1, numberOfElementsIn(responseHandlersProcessor.responseHandlers, InAppCleanUpResponseHandler::class.java).toLong())
            Assert.assertEquals(1, numberOfElementsIn(responseHandlersProcessor.responseHandlers, InAppCleanUpResponseHandlerV4::class.java).toLong())
            Assert.assertEquals(2, numberOfElementsIn(responseHandlersProcessor.responseHandlers, MobileEngageTokenResponseHandler::class.java).toLong())
            Assert.assertEquals(1, numberOfElementsIn(responseHandlersProcessor.responseHandlers, MobileEngageClientStateResponseHandler::class.java).toLong())
            Assert.assertEquals(1, numberOfElementsIn(responseHandlersProcessor.responseHandlers, ClientInfoResponseHandler::class.java).toLong())
            Assert.assertEquals(1, numberOfElementsIn(responseHandlersProcessor.responseHandlers, OnEventActionResponseHandler::class.java).toLong())
            Assert.assertEquals(1, numberOfElementsIn(responseHandlersProcessor.responseHandlers, DeviceEventStateResponseHandler::class.java).toLong())
        }
    }

    @Test
    fun testSetup_registersPredictTrigger_whenPredictIsEnabled() {
        setup(predictConfig)

        runBlockingOnCoreSdkThread {
            verify(mockCoreSQLiteDatabase).registerTrigger(
                    "shard",
                    TriggerType.AFTER,
                    TriggerEvent.INSERT,
                    mockPredictShardTrigger
            )
        }
    }

    @Test
    fun testSetup_doNotRegistersPredictTrigger_whenPredictIsDisabled() {
        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread {
            val argumentCaptor = ArgumentCaptor.forClass(Runnable::class.java)
            verify(mockCoreSQLiteDatabase, times(1)).registerTrigger(
                    ArgumentMatchers.any(String::class.java),
                    ArgumentMatchers.any(TriggerType::class.java),
                    ArgumentMatchers.any(TriggerEvent::class.java),
                    argumentCaptor.capture()
            )
            Assert.assertEquals(mockLogShardTrigger, argumentCaptor.value)
            verifyNoMoreInteractions(mockCoreSQLiteDatabase)
        }
    }

    @Test
    fun testSetup_registersLogTrigger() {
        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread {
            verify(mockCoreSQLiteDatabase).registerTrigger(
                    "shard",
                    TriggerType.AFTER,
                    TriggerEvent.INSERT,
                    mockLogShardTrigger
            )
        }
    }

    @Test
    fun testSetup_registers_activityLifecycleWatchdog() {
        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread {
            val captor = ArgumentCaptor.forClass(ActivityLifecycleWatchdog::class.java)
            verify(application, times(2)).registerActivityLifecycleCallbacks(captor.capture())
            getElementByType(captor.allValues, ActivityLifecycleWatchdog::class.java) shouldNotBe null
            getElementByType(captor.allValues, CurrentActivityWatchdog::class.java) shouldNotBe null
        }
    }

    @Test
    fun testSetup_registers_activityLifecycleWatchdog_withInAppStartAction() {
        IntegrationTestUtils.tearDownEmarsys()
        val captor = ArgumentCaptor.forClass(ActivityLifecycleWatchdog::class.java)

        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread {
            verify(application, times(2)).registerActivityLifecycleCallbacks(captor.capture())
            val actions = getElementByType(
                    captor.allValues,
                    ActivityLifecycleWatchdog::class.java
            )?.initializationActions?.toList()
            Assert.assertEquals(1, numberOfElementsIn(actions!!, InAppStartAction::class.java).toLong())
        }
    }

    @Test
    fun testSetup_registers_activityLifecycleWatchdog_withDeepLinkAction() {
        IntegrationTestUtils.tearDownEmarsys()
        val captor = ArgumentCaptor.forClass(ActivityLifecycleWatchdog::class.java)

        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread {
            verify(application, times(2)).registerActivityLifecycleCallbacks(captor.capture())
            val actions = getElementByType(
                    captor.allValues,
                    ActivityLifecycleWatchdog::class.java
            )?.activityCreatedActions?.toList()
            Assert.assertEquals(1, numberOfElementsIn(actions!!, DeepLinkAction::class.java).toLong())
        }
    }

    @Test
    fun testSetup_registers_currentActivityWatchDog() {
        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread {
            verify(application).registerActivityLifecycleCallbacks(currentActivityWatchdog)
        }
    }

    @Test
    fun testSetup_doesNotSetInAppEventHandler_whenMissingFromConfig() {
        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread {
            verifyZeroInteractions(mockInApp)
        }
    }

    @Test
    fun testSetup_sendClientInfo() {
        whenever(mockClientStateStorage.get()).thenReturn(null)
        whenever(mockContactFieldValueStorage.get()).thenReturn(null)
        whenever(mockContactTokenStorage.get()).thenReturn(null)
        whenever(mockRequestContext.hasContactIdentification()).thenReturn(false)
        whenever(mockDeviceInfoPayloadStorage.get()).thenReturn("hardwareInfoPayload")

        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread()

        verify(mockClientServiceApi).trackDeviceInfo(null)
    }

    @Test
    fun testSetup_doNotSendClientInfo_whenHashIsUnChanged() {
        whenever(mockContactTokenStorage.get()).thenReturn(null)
        whenever(mockContactFieldValueStorage.get()).thenReturn(null)

        val expectedDeviceInfo = deviceInfo.deviceInfoPayload
        whenever(mockClientStateStorage.get()).thenReturn("asdfsaf")
        whenever(mockDeviceInfoPayloadStorage.get()).thenReturn(expectedDeviceInfo)

        setup(mobileEngageConfig)

        verify(mockClientServiceApi, never()).trackDeviceInfo(null)
    }

    @Test
    fun testSetup_doNotSendClientInfo_whenAnonymousContactIsNotNeededToSend() {
        whenever(mockClientStateStorage.get()).thenReturn(null)
        whenever(mockContactFieldValueStorage.get()).thenReturn("asdf")
        whenever(mockContactTokenStorage.get()).thenReturn("asdf")

        setup(mobileEngageConfig)

        verify(mockClientServiceApi, never()).trackDeviceInfo(null)
    }

    @Test
    fun testSetup_sendAnonymousContact() {
        whenever(mockContactTokenStorage.get()).thenReturn(null)
        whenever(mockRequestContext.hasContactIdentification()).thenReturn(false)

        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread()
        verify(mockMobileEngageApi).setContact(null, null)
    }

    @Test
    fun testSetup_sendDeviceInfoAndAnonymousContact_inOrder() {
        whenever(mockRequestContext.hasContactIdentification()).thenReturn(false)
        whenever(mockContactTokenStorage.get()).thenReturn(null)

        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread()

        val inOrder = inOrder(mockMobileEngageApi, mockClientServiceApi)
        inOrder.verify(mockClientServiceApi).trackDeviceInfo(null)
        inOrder.verify(mockMobileEngageApi).setContact(null, null)
        inOrder.verifyNoMoreInteractions()

    }

    @Test
    fun testSetup_doNotSendAnonymousContact_whenContactIsIdentified() {
        whenever(mockContactTokenStorage.get()).thenReturn(null)
        whenever(mockRequestContext.hasContactIdentification()).thenReturn(true)

        setup(mobileEngageConfig)

        verify(mockMobileEngageApi, never()).setContact(null, null)
    }

    @Test
    fun testSetup_doNotSendAnonymousContact_whenContactTokenIsPresent() {
        whenever(mockRequestContext.hasContactIdentification()).thenReturn(false)

        setup(mobileEngageConfig)

        verify(mockMobileEngageApi, never()).setContact(null, null)
    }

    @Test
    fun testSetup_shouldNotCallTrackDeviceInfoAndSetContact_whenMobileEngageFeatureIsDisabled() {
        whenever(mockContactFieldValueStorage.get()).thenReturn(null)
        whenever(mockContactTokenStorage.get()).thenReturn(null)
        whenever(mockClientStateStorage.get()).thenReturn(null)

        setup(baseConfig)

        runBlockingOnCoreSdkThread()

        verify(mockMobileEngageApi, never()).setContact(null, null)
        verify(mockClientServiceApi, never()).trackDeviceInfo(null)
    }

    @Test
    fun testSetContactWithCompletionListener_delegatesToPredictInternal_whenPredictEnabled() {
        setup(predictConfig)

        setContact(CONTACT_ID, completionListener)
        runBlockingOnCoreSdkThread()

        runBlockingOnCoreSdkThread {
            verify(mockPredictRestricted).setContact(CONTACT_ID)
            verifyZeroInteractions(mockMobileEngageApi)
        }
    }

    @Test
    fun testSetContactWithCompletionListener_delegatesToMobileEngageInternal_whenMobileEngageEnabled() {
        setup(mobileEngageConfig)

        setContact(CONTACT_ID, completionListener)

        runBlockingOnCoreSdkThread {
            verifyZeroInteractions(mockPredictRestricted)
            verify(mockMobileEngageApi).setContact(CONTACT_ID, completionListener)
        }
    }

    @Test
    fun testSetAuthenticatedContactWithCompletionListener_delegatesToMobileEngageInternal_whenMobileEngageEnabled() {
        setup(mobileEngageConfig)

        setAuthenticatedContact(ID_TOKEN, completionListener)

        runBlockingOnCoreSdkThread {
            verifyZeroInteractions(mockPredictRestricted)
            verify(mockMobileEngageApi).setAuthenticatedContact(ID_TOKEN, completionListener)
        }
    }

    @Test
    fun testSetAuthenticatedContactWithCompletionListener_doNotDelegatesToMobileEngageApi_whenMobileEngageDisabled() {
        setup(predictConfig)

        setAuthenticatedContact(ID_TOKEN, completionListener)

        runBlockingOnCoreSdkThread()

        verifyZeroInteractions(mockMobileEngageApi)
    }

    @Test
    fun testSetAuthenticatedContactWithCompletionListener_delegatesToInternals_whenMobileEngageAndPredictEnabled() {
        setup(mobileEngageAndPredictConfig)
        FeatureRegistry.enableFeature(InnerFeature.PREDICT)

        setAuthenticatedContact(ID_TOKEN, completionListener)

        runBlockingOnCoreSdkThread()

        runBlockingOnCoreSdkThread()
        verify(mockMobileEngageApi).setAuthenticatedContact(ID_TOKEN, completionListener)
        FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT) shouldBe false
        verifyZeroInteractions(mockPredictRestricted)
    }

    @Test
    fun testSetAuthenticatedContactWithCompletionListener_doNotDelegatesToMobileEngageApi_whenMobileEngageAndPredictDisabled() {
        setup(baseConfig)

        setAuthenticatedContact(ID_TOKEN, completionListener)

        runBlockingOnCoreSdkThread {
            verifyZeroInteractions(mockPredictRestricted)
            setAuthenticatedContact(ID_TOKEN, completionListener)
        }
    }

    @Test
    fun testSetContactWithCompletionListener_doNotDelegatesToPredictInternal_whenPredictDisabled() {
        setup(mobileEngageConfig)

        setContact(CONTACT_ID, completionListener)

        runBlockingOnCoreSdkThread {
            verifyZeroInteractions(mockPredictRestricted)
        }
    }

    @Test
    fun testSetContactWithCompletionListener_doNotDelegatesToMobileEngageApi_whenMobileEngageDisabled() {
        setup(predictConfig)

        setContact(CONTACT_ID, completionListener)

        verifyZeroInteractions(mockMobileEngageApi)
    }

    @Test
    fun testSetContactWithCompletionListener_delegatesToInternals_whenBothFeaturesEnabled() {
        setup(mobileEngageAndPredictConfig)

        setContact(CONTACT_ID, completionListener)

        runBlockingOnCoreSdkThread {
            verify(mockPredictRestricted).setContact(CONTACT_ID)
            verify(mockMobileEngageApi).setContact(CONTACT_ID, completionListener)
        }
    }

    @Test
    fun testSetContactWithCompletionListener_delegatesToLoggingMobileEngageOnly_whenBothFeaturesDisabled() {
        setup(baseConfig)

        setContact(CONTACT_ID, completionListener)

        runBlockingOnCoreSdkThread {
            verifyZeroInteractions(mockPredictRestricted)
            verify(mockLoggingMobileEngageApi).setContact(CONTACT_ID, completionListener)
        }
    }

    @Test
    fun testClearContactWithCompletionListener_delegatesToPredictInternal_whenPredictIsEnabled() {
        setup(predictConfig)

        clearContact(completionListener)
        runBlockingOnCoreSdkThread()

        runBlockingOnCoreSdkThread {
            verifyZeroInteractions(mockMobileEngageApi)
            verify(mockPredictRestricted).clearContact()
        }
    }

    @Test
    fun testClearContactWithCompletionListener_delegatesToMobileApi_whenMobileEngageIsEnabled() {
        setup(mobileEngageConfig)

        clearContact(completionListener)

        runBlockingOnCoreSdkThread {
            verify(mockMobileEngageApi).clearContact(completionListener)
            verifyZeroInteractions(mockPredictRestricted)
        }
    }

    @Test
    fun testClearContactWithCompletionListener_doNotDelegatesToPredictInternal_whenPredictIsDisabled() {
        setup(mobileEngageConfig)

        clearContact(completionListener)
        runBlockingOnCoreSdkThread {
            verifyZeroInteractions(mockPredictRestricted)
        }
    }

    @Test
    fun testClearContactWithCompletionListener_doNotDelegatesToMobileEngageApi_whenMobileEngageIsDisabled() {
        setup(predictConfig)

        clearContact(completionListener)

        verifyZeroInteractions(mockMobileEngageApi)
    }

    @Test
    fun testClearContactWithCompletionListener_delegatesToInternals_whenBothEnabled() {
        setup(mobileEngageAndPredictConfig)

        clearContact(completionListener)

        runBlockingOnCoreSdkThread {
            verify(mockPredictRestricted).clearContact()
        }

        verify(mockMobileEngageApi).clearContact(completionListener)
    }

    @Test
    fun testClearContactWithCompletionListener_delegatesToLoggingMobileEngageApiOnly_whenBothDisabled() {
        setup(baseConfig)

        clearContact(completionListener)
        runBlockingOnCoreSdkThread {
            verifyZeroInteractions(mockPredictRestricted)
        }
        verify(mockLoggingMobileEngageApi).clearContact(completionListener)
    }

    @Test
    fun testTrackDeepLink_delegatesTo_deepLinkApi() {
        setup(createConfig().applicationCode(APPLICATION_CODE).build())
        val mockActivity: Activity = mock()
        val mockIntent: Intent = mock()
        trackDeepLink(mockActivity, mockIntent)

        runBlockingOnCoreSdkThread {
            verify(mockDeepLinkApi).trackDeepLinkOpen(mockActivity, mockIntent, null)
        }
    }

    @Test
    fun testTrackDeepLink_delegatesTo_loggingDeepLinkApi_whenMobileEngageIsNotEnabled() {
        val mockActivity: Activity = mock()
        val mockIntent: Intent = mock()
        trackDeepLink(mockActivity, mockIntent)

        runBlockingOnCoreSdkThread {
            verify(mockLoggingDeepLinkApi).trackDeepLinkOpen(mockActivity, mockIntent, null)
        }
    }

    @Test
    fun testTrackCustomEventWithCompletionListener_delegatesTo_eventServiceApi() {
        setup(createConfig().applicationCode(APPLICATION_CODE).build())
        val eventName = "eventName"
        val eventAttributes = HashMap<String, String>()
        trackCustomEvent(eventName, eventAttributes, completionListener)

        runBlockingOnCoreSdkThread {
            verify(mockEventServiceApi).trackCustomEventAsync(eventName, eventAttributes, completionListener)
        }
    }

    @Test
    fun testTrackCustomEventWithCompletionListener_delegatesTo_loggingEventServiceApi() {
        val eventName = "eventName"
        val eventAttributes = HashMap<String, String>()
        trackCustomEvent(eventName, eventAttributes, completionListener)
        runBlockingOnCoreSdkThread()

        verify(mockLoggingEventServiceApi).trackCustomEventAsync(eventName, eventAttributes, completionListener)
    }

    @Test
    fun testMobileEngageApiInstances_shouldAlwaysGetInstanceFromDI() {
        setup(predictConfig)

        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)
        Emarsys.inApp.isPaused
        verify(mockInApp).isPaused
        verifyZeroInteractions(mockLoggingInApp)
    }

    @Test
    fun testPredictApiInstances_shouldAlwaysGetInstanceFromDI() {
        setup(mobileEngageConfig)

        FeatureRegistry.enableFeature(InnerFeature.PREDICT)
        Emarsys.predict.trackItemView("testItemId")
        verify(mockPredict).trackItemView("testItemId")
        verifyZeroInteractions(mockLoggingPredict)
    }

    private fun createConfig(vararg experimentalFeatures: FlipperFeature): EmarsysConfig.Builder {
        return EmarsysConfig.Builder()
                .application(application)
                .contactFieldId(CONTACT_FIELD_ID)
                .enableExperimentalFeatures(*experimentalFeatures)
    }

    private fun runBlockingOnCoreSdkThread(callback: (() -> Unit)? = null) {
        val latch = CountDownLatch(1)
        var exception: Exception? = null
        emarsys().coreSdkHandler.post {
            try {
                callback?.invoke()
            } catch (e: Exception) {
                exception = e
            } finally {
                latch.countDown()
            }
        }
        latch.await()
        if (exception != null) {
            throw exception as Exception
        }

    }
}