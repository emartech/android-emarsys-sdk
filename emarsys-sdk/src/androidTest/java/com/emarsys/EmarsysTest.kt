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
import com.emarsys.config.ConfigInternal
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.TransitionSafeCurrentActivityWatchdog
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
import com.emarsys.core.provider.clientid.ClientIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseHandlersProcessor
import com.emarsys.core.storage.StringStorage
import com.emarsys.deeplink.DeepLinkApi
import com.emarsys.di.DefaultEmarsysComponent
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.emarsys
import com.emarsys.di.isEmarsysComponentSetup
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.eventservice.EventServiceApi
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.MessageInboxApi
import com.emarsys.mobileengage.MobileEngageApi
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.deeplink.DeepLinkAction
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.mobileengage.device.DeviceInfoStartAction
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.geofence.FetchGeofencesAction
import com.emarsys.mobileengage.iam.AppStartAction
import com.emarsys.mobileengage.iam.model.requestRepositoryProxy.RequestRepositoryProxy
import com.emarsys.mobileengage.responsehandler.ClientInfoResponseHandler
import com.emarsys.mobileengage.responsehandler.DeviceEventStateResponseHandler
import com.emarsys.mobileengage.responsehandler.InAppCleanUpResponseHandler
import com.emarsys.mobileengage.responsehandler.InAppCleanUpResponseHandlerV4
import com.emarsys.mobileengage.responsehandler.InAppMessageResponseHandler
import com.emarsys.mobileengage.responsehandler.MobileEngageClientStateResponseHandler
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler
import com.emarsys.mobileengage.responsehandler.OnEventActionResponseHandler
import com.emarsys.predict.PredictApi
import com.emarsys.predict.PredictRestrictedApi
import com.emarsys.predict.api.model.Logic
import com.emarsys.predict.api.model.Product
import com.emarsys.predict.api.model.RecommendationFilter
import com.emarsys.predict.response.VisitorIdResponseHandler
import com.emarsys.predict.response.XPResponseHandler
import com.emarsys.push.PushApi
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.CollectionTestUtils.getElementByType
import com.emarsys.testUtil.CollectionTestUtils.numberOfElementsIn
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.ReflectionTestUtils.getInstanceField
import com.emarsys.testUtil.rules.ConnectionRule
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Called
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.Rule
import java.util.concurrent.CountDownLatch

class EmarsysTest : AnnotationSpec() {
    @Rule
    @JvmField
    val duplicateThreadRule = DuplicatedThreadRule("CoreSDKHandlerThread")


    @Rule
    @JvmField
    val connectionRule = ConnectionRule(getTargetContext().applicationContext as Application)

    companion object {
        private const val APPLICATION_CODE = "EMS11-C3FD3"
        private const val CONTACT_FIELD_ID = 3
        private const val MERCHANT_ID = "merchantId"
        private const val SDK_VERSION = "sdkVersion"
        private const val CONTACT_FIELD_VALUE = "CONTACT_ID"
        private const val OPEN_ID_TOKEN = "testIdToken"
    }

    private lateinit var mockActivityLifecycleWatchdog: ActivityLifecycleWatchdog
    private lateinit var mockCurrentActivityWatchdog: TransitionSafeCurrentActivityWatchdog
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
    private lateinit var mockConfigInternal: ConfigInternal
    private lateinit var mockMessageInbox: MessageInboxApi
    private lateinit var mockClientIdProvider: ClientIdProvider
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
        application = spyk(getTargetContext().applicationContext as Application)
        completionListener = mockk(relaxed = true)
        mockResultListener = mockk(relaxed = true)
        mockActivityLifecycleWatchdog = mockk(relaxed = true)
        mockCurrentActivityWatchdog = mockk(relaxed = true)
        mockCoreSQLiteDatabase = mockk(relaxed = true)
        mockMobileEngageInternal = mockk(relaxed = true)
        mockDeepLinkInternal = mockk(relaxed = true)
        mockDeepLinkApi = mockk(relaxed = true)
        mockLoggingDeepLinkApi = mockk(relaxed = true)
        mockEventServiceInternal = mockk(relaxed = true)
        mockEventServiceApi = mockk(relaxed = true)
        mockLoggingEventServiceApi = mockk(relaxed = true)
        mockClientServiceApi = mockk(relaxed = true)
        mockPredictShardTrigger = mockk(relaxed = true)
        mockLogShardTrigger = mockk(relaxed = true)
        mockLanguageProvider = mockk(relaxed = true)
        mockVersionProvider = mockk(relaxed = true)
        inappEventHandler = mockk(relaxed = true)
        mockDeviceInfoPayloadStorage = mockk(relaxed = true)
        mockContactFieldValueStorage = mockk(relaxed = true)
        mockContactTokenStorage = mockk(relaxed = true)
        mockClientStateStorage = mockk(relaxed = true)
        mockNotificationManagerHelper = mockk(relaxed = true)

        baseConfig = createConfig().build()
        mobileEngageConfig = createConfig()
            .applicationCode(APPLICATION_CODE)
            .build()
        predictConfig = createConfig()
            .merchantId(MERCHANT_ID)
            .build()
        mobileEngageAndPredictConfig = createConfig()
            .applicationCode(APPLICATION_CODE)
            .merchantId(MERCHANT_ID)
            .build()
        mockRequestContext = mockk(relaxed = true)
        mockClientIdProvider = mockk(relaxed = true)
        mockMobileEngageApi = mockk(relaxed = true)
        mockLoggingMobileEngageApi = mockk(relaxed = true)
        mockInApp = mockk(relaxed = true)
        mockLoggingInApp = mockk(relaxed = true)
        mockPush = mockk(relaxed = true)
        mockPredict = mockk(relaxed = true)
        mockLoggingPredict = mockk(relaxed = true)
        mockPredictRestricted = mockk(relaxed = true)
        mockConfig = mockk(relaxed = true)
        mockConfigInternal = mockk(relaxed = true)
        mockMessageInbox = mockk(relaxed = true)
        mockLogic = mockk(relaxed = true)
        mockRecommendationFilter = mockk(relaxed = true)
        predictResultListenerCallback = mockk(relaxed = true)
        every { (mockNotificationManagerHelper.channelSettings) } returns
                listOf(
                    ChannelSettings(channelId = "channelId")
                )

        every { mockNotificationManagerHelper.importance } returns -1000
        every { mockNotificationManagerHelper.areNotificationsEnabled } returns false
        every { mockClientIdProvider.provideClientId() } returns "hwid"
        every {
            mockLanguageProvider.provideLanguage(any())
        } returns "language"

        every { mockVersionProvider.provideSdkVersion() } returns "version"

        deviceInfo = DeviceInfo(
            application, mockClientIdProvider, mockVersionProvider,
            mockLanguageProvider, mockNotificationManagerHelper,
            isAutomaticPushSendingEnabled = true, isGooglePlayAvailable = true
        )

        every { mockRequestContext.applicationCode } returns APPLICATION_CODE
        every { mockRequestContext.deviceInfo } returns deviceInfo
        every { mockVersionProvider.provideSdkVersion() } returns SDK_VERSION
        every { mockContactFieldValueStorage.get() } returns "test@test.com"
        every { mockContactTokenStorage.get() } returns "contactToken"

        every { mockDeviceInfoPayloadStorage.get() } returns "deviceInfo.deviceInfoPayload"

        setupEmarsysComponent(
            FakeDependencyContainer(
                activityLifecycleWatchdog = mockActivityLifecycleWatchdog,
                coreSQLiteDatabase = mockCoreSQLiteDatabase,
                deviceInfo = deviceInfo,
                logShardTrigger = mockLogShardTrigger,
                mobileEngageInternal = mockMobileEngageInternal,
                loggingMobileEngageInternal = mockMobileEngageInternal,
                deepLinkInternal = mockDeepLinkInternal,
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
                configInternal = mockConfigInternal,
                eventService = mockEventServiceApi,
                loggingEventService = mockLoggingEventServiceApi,
                deepLink = mockDeepLinkApi,
                logger = mockk(relaxed = true)
            )
        )
        latch = CountDownLatch(1)
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testSetup_whenMobileEngageApplicationCodeAndMerchantIdAreNull_mobileEngageAndPredict_shouldBeDisabled_andShouldNotFetchRemoteConfig() {
        val config = createConfig()
            .applicationCode(null)
            .merchantId(null)
            .build()
        setup(config)

        runBlockingOnCoreSdkThread()

        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
        FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT) shouldBe false
        verify(exactly = 0) { (mockConfigInternal).refreshRemoteConfig(any()) }
    }

    @Test
    fun testSetup_shouldNotFetchRemoteConfig_when_ApplicationCode_is_invalid() {
        listOf("", "null", "nil", "0", null).forEach {
            verifyRemoteConfigFetchWithInvalidAppcode(it)
        }
    }

    @Test
    fun testSetup_whenMobileEngageApplicationCodeIsNotNull_mobileEngageFeature_shouldBeEnabled_andFetchRemoteConfig() {
        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread()

        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe true
        verify { mockConfigInternal.refreshRemoteConfig(any()) }
    }

    @Test
    fun testSetup_whenPredictMerchantIdIsNotNull_predictFeature_shouldBeEnabled() {
        setup(predictConfig)

        FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT) shouldBe true
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
        container.javaClass shouldBe DefaultEmarsysComponent::class.java
        isEmarsysComponentSetup() shouldBe true
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
                "requestRepository"
            )
        }
        repository?.javaClass shouldBe RequestRepositoryProxy::class.java
    }

    @Test
    fun testSetup_initializesCoreCompletionHandler_withNoFlippers() {
        IntegrationTestUtils.tearDownEmarsys()

        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread {
            val responseHandlersProcessor = emarsys().responseHandlersProcessor

            responseHandlersProcessor shouldNotBe null
            responseHandlersProcessor.responseHandlers.size shouldBe 11
            numberOfElementsIn(
                responseHandlersProcessor.responseHandlers,
                VisitorIdResponseHandler::class.java
            ).toLong() shouldBe 1
            numberOfElementsIn(
                responseHandlersProcessor.responseHandlers,
                XPResponseHandler::class.java
            ).toLong() shouldBe 1
            numberOfElementsIn(
                responseHandlersProcessor.responseHandlers,
                InAppMessageResponseHandler::class.java
            ).toLong() shouldBe 1
            numberOfElementsIn(
                responseHandlersProcessor.responseHandlers,
                InAppCleanUpResponseHandler::class.java
            ).toLong() shouldBe 1
            numberOfElementsIn(
                responseHandlersProcessor.responseHandlers,
                InAppCleanUpResponseHandlerV4::class.java
            ).toLong() shouldBe 1
            numberOfElementsIn(
                responseHandlersProcessor.responseHandlers,
                MobileEngageTokenResponseHandler::class.java
            ).toLong() shouldBe 2
            numberOfElementsIn(
                responseHandlersProcessor.responseHandlers,
                MobileEngageClientStateResponseHandler::class.java
            ).toLong() shouldBe 1
            numberOfElementsIn(
                responseHandlersProcessor.responseHandlers,
                ClientInfoResponseHandler::class.java
            ).toLong() shouldBe 1
            numberOfElementsIn(
                responseHandlersProcessor.responseHandlers,
                OnEventActionResponseHandler::class.java
            ).toLong() shouldBe 1
            numberOfElementsIn(
                responseHandlersProcessor.responseHandlers,
                DeviceEventStateResponseHandler::class.java
            ).toLong() shouldBe 1

        }
    }

    @Test
    fun testSetup_registersPredictTrigger_whenPredictIsEnabled() {
        setup(predictConfig)

        runBlockingOnCoreSdkThread {
            verify {
                mockCoreSQLiteDatabase.registerTrigger(
                    "shard",
                    TriggerType.AFTER,
                    TriggerEvent.INSERT,
                    mockPredictShardTrigger
                )
            }
        }
    }

    @Test
    fun testSetup_doNotRegistersPredictTrigger_whenPredictIsDisabled() {
        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread {
            val slots = mutableListOf<Runnable>()
            verify(exactly = 2) {
                mockCoreSQLiteDatabase.registerTrigger(
                    any(),
                    any(),
                    any(),
                    capture(slots)
                )
            }

            slots[0] shouldBe mockPredictShardTrigger
            slots[1] shouldBe mockLogShardTrigger
        }
    }

    @Test
    fun testSetup_registersLogTrigger() {
        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread {
            verify {
                mockCoreSQLiteDatabase.registerTrigger(
                    "shard",
                    TriggerType.AFTER,
                    TriggerEvent.INSERT,
                    mockLogShardTrigger
                )
            }
        }
    }

    @Test
    fun testSetup_registers_activityLifecycleWatchdog() {
        setup(mobileEngageConfig)
        runBlockingOnCoreSdkThread {
            val watchdogSlot = slot<ActivityLifecycleWatchdog>()

            verify(exactly = 1) {
                application
                    .registerActivityLifecycleCallbacks(capture(watchdogSlot))
            }
            val allRegisteredWatchdogs = listOf(watchdogSlot.captured)
            getElementByType(
                allRegisteredWatchdogs,
                ActivityLifecycleWatchdog::class.java
            ) shouldNotBe null
        }
        val currentActivityWatchdogSlot = slot<TransitionSafeCurrentActivityWatchdog>()
        verify(exactly = 1) {
            application
                .registerActivityLifecycleCallbacks(capture(currentActivityWatchdogSlot))
        }
        val allRegisteredWatchdogs = listOf(currentActivityWatchdogSlot.captured)
        getElementByType(
            allRegisteredWatchdogs,
            TransitionSafeCurrentActivityWatchdog::class.java
        ) shouldNotBe null
    }


    @Test
    fun testSetup_registers_activityLifecycleWatchdogs() {
        IntegrationTestUtils.tearDownEmarsys()
        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread {
            verify(exactly = 2) { application.registerActivityLifecycleCallbacks(any<ActivityLifecycleWatchdog>()) }

        }
    }

    @Test
    fun testSetup_registers_startActions() {
        IntegrationTestUtils.tearDownEmarsys()
        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread {
            val actions = emarsys().activityLifecycleActionRegistry.lifecycleActions

            numberOfElementsIn(actions, AppStartAction::class.java).toLong() shouldBe 1
            numberOfElementsIn(actions, DeepLinkAction::class.java).toLong() shouldBe 1
            numberOfElementsIn(actions, DeviceInfoStartAction::class.java).toLong() shouldBe 1
            numberOfElementsIn(actions, FetchGeofencesAction::class.java).toLong() shouldBe 1
        }
    }

    @Test
    fun testSetup_registers_currentActivityWatchDog() {
        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread()
        application.registerActivityLifecycleCallbacks(mockActivityLifecycleWatchdog)
        application.registerActivityLifecycleCallbacks(mockCurrentActivityWatchdog)
    }

    @Test
    fun testSetup_doesNotSetInAppEventHandler_whenMissingFromConfig() {
        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread {
            verify {
                mockInApp wasNot Called
            }
        }
    }

    @Test
    fun testSetup_sendClientInfo() {
        every { mockClientStateStorage.get() } returns null
        every { mockContactFieldValueStorage.get() } returns null
        every { mockContactTokenStorage.get() } returns null
        every { mockRequestContext.hasContactIdentification() } returns false
        every { mockDeviceInfoPayloadStorage.get() } returns "hardwareInfoPayload"

        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread()

        verify { mockClientServiceApi.trackDeviceInfo(null) }
    }

    @Test
    fun testSetup_doNotSendClientInfo_whenHashIsUnChanged() {
        every { mockContactTokenStorage.get() } returns null
        every { mockContactFieldValueStorage.get() } returns null

        val expectedDeviceInfo = deviceInfo.deviceInfoPayload
        every { mockClientStateStorage.get() } returns "asdfsaf"
        every { mockDeviceInfoPayloadStorage.get() } returns expectedDeviceInfo

        setup(mobileEngageConfig)

        verify(exactly = 0) { mockClientServiceApi.trackDeviceInfo(null) }
    }

    @Test
    fun testSetup_doNotSendClientInfo_whenAnonymousContactIsNotNeededToSend() {
        every { mockClientStateStorage.get() } returns null
        every { mockContactFieldValueStorage.get() } returns "asdf"
        every { mockContactTokenStorage.get() } returns "asdf"

        setup(mobileEngageConfig)

        verify(exactly = 0) { mockClientServiceApi.trackDeviceInfo(null) }
    }

    @Test
    fun testSetup_shouldSetAnonymousContact_withClearContactCall() {
        every { mockContactTokenStorage.get() } returns null
        every { mockRequestContext.hasContactIdentification() } returns false
        every { mockClientStateStorage.get() } returns null
        every { mockContactFieldValueStorage.get() } returns null
        every { mockDeviceInfoPayloadStorage.get() } returns "hardwareInfoPayload"
        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread()
        verify { mockMobileEngageApi.clearContact(null) }
    }

    @Test
    fun testSetup_sendDeviceInfoAndAnonymousContact_inOrder() {
        every { mockClientStateStorage.get() } returns null
        every { mockContactFieldValueStorage.get() } returns null
        every { mockContactTokenStorage.get() } returns null
        every { mockRequestContext.hasContactIdentification() } returns false
        every { mockDeviceInfoPayloadStorage.get() } returns "hardwareInfoPayload"

        setup(mobileEngageConfig)

        runBlockingOnCoreSdkThread()

        verifyOrder {
            mockClientServiceApi.trackDeviceInfo(null)
            mockMobileEngageApi.clearContact(null)
        }
        confirmVerified(mockMobileEngageApi, mockClientServiceApi)
    }

    @Test
    fun testSetup_doNotSendAnonymousContact_whenContactIsIdentified() {
        every { mockContactTokenStorage.get() } returns null
        every { mockRequestContext.hasContactIdentification() } returns true

        setup(mobileEngageConfig)

        verify(exactly = 0) { mockMobileEngageApi.setContact(null, null, null) }
    }

    @Test
    fun testSetup_doNotSendAnonymousContact_whenContactTokenIsPresent() {
        every { mockRequestContext.hasContactIdentification() } returns false

        setup(mobileEngageConfig)

        verify(exactly = 0) { mockMobileEngageApi.setContact(null, null, null) }
    }

    @Test
    fun testSetup_shouldNotCallTrackDeviceInfoAndSetContact_whenMobileEngageFeatureIsDisabled() {
        every { mockContactFieldValueStorage.get() } returns null
        every { mockContactTokenStorage.get() } returns null
        every { mockClientStateStorage.get() } returns null

        setup(baseConfig)

        runBlockingOnCoreSdkThread()

        verify(exactly = 0) { mockMobileEngageApi.setContact(null, null, null) }
        verify(exactly = 0) { mockClientServiceApi.trackDeviceInfo(null) }
    }

    @Test
    fun testSetContactWithCompletionListener_delegatesToPredictInternal_whenPredictEnabled() {
        setup(predictConfig)

        setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE, completionListener)
        runBlockingOnCoreSdkThread()

        runBlockingOnCoreSdkThread {
            verify { mockPredictRestricted.setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE) }
            verify {
                mockMobileEngageApi wasNot Called
            }
        }
    }

    @Test
    fun testSetContactWithCompletionListener_delegatesToMobileEngageInternal_whenMobileEngageEnabled() {
        setup(mobileEngageConfig)

        setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE, completionListener)

        runBlockingOnCoreSdkThread {
            verify {
                mockPredictRestricted wasNot Called
                mockMobileEngageApi.setContact(
                    CONTACT_FIELD_ID,
                    CONTACT_FIELD_VALUE,
                    completionListener
                )
            }
        }
    }

    @Test
    fun testSetAuthenticatedContactWithCompletionListener_delegatesToMobileEngageInternal_whenMobileEngageEnabled() {
        setup(mobileEngageConfig)

        setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, completionListener)

        runBlockingOnCoreSdkThread {
            verify {
                mockPredictRestricted wasNot Called
                mockMobileEngageApi.setAuthenticatedContact(
                    CONTACT_FIELD_ID,
                    OPEN_ID_TOKEN,
                    completionListener
                )
            }
        }
    }

    @Test
    fun testSetAuthenticatedContactWithCompletionListener_doNotDelegatesToMobileEngageApi_whenMobileEngageDisabled() {
        setup(predictConfig)

        setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, completionListener)

        runBlockingOnCoreSdkThread()

        verify { mockMobileEngageApi wasNot Called }
    }

    @Test
    fun testSetAuthenticatedContactWithCompletionListener_delegatesToInternals_whenMobileEngageAndPredictEnabled() {
        setup(mobileEngageAndPredictConfig)
        FeatureRegistry.enableFeature(InnerFeature.PREDICT)

        setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, completionListener)

        runBlockingOnCoreSdkThread()

        runBlockingOnCoreSdkThread()
        verify {
            mockMobileEngageApi.setAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN,
                completionListener
            )
        }
        FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT) shouldBe false
        verify { mockPredictRestricted wasNot Called }
    }

    @Test
    fun testSetAuthenticatedContactWithCompletionListener_doNotDelegatesToMobileEngageApi_whenMobileEngageAndPredictDisabled() {
        setup(baseConfig)

        setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, completionListener)

        runBlockingOnCoreSdkThread {
            verify { mockPredictRestricted wasNot Called }
            setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, completionListener)
        }
    }

    @Test
    fun testSetContactWithCompletionListener_doNotDelegatesToPredictInternal_whenPredictDisabled() {
        setup(mobileEngageConfig)

        setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE, completionListener)

        runBlockingOnCoreSdkThread {
            verify { mockPredictRestricted wasNot Called }
        }
    }

    @Test
    fun testSetContactWithCompletionListener_doNotDelegatesToMobileEngageApi_whenMobileEngageDisabled() {
        setup(predictConfig)

        setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE, completionListener)

        verify { mockMobileEngageApi wasNot Called }
    }

    @Test
    fun testSetContactWithCompletionListener_delegatesToInternals_whenBothFeaturesEnabled() {
        setup(mobileEngageAndPredictConfig)

        setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE, completionListener)

        runBlockingOnCoreSdkThread {
            verify {
                mockPredictRestricted.setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)
                mockMobileEngageApi.setContact(
                    CONTACT_FIELD_ID,
                    CONTACT_FIELD_VALUE,
                    completionListener
                )
            }
        }
    }

    @Test
    fun testSetContactWithCompletionListener_delegatesToLoggingMobileEngageOnly_whenBothFeaturesDisabled() {
        setup(baseConfig)

        setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE, completionListener)

        runBlockingOnCoreSdkThread {
            verify {
                mockPredictRestricted wasNot Called
                mockLoggingMobileEngageApi.setContact(
                    CONTACT_FIELD_ID,
                    CONTACT_FIELD_VALUE,
                    completionListener
                )
            }
        }
    }

    @Test
    fun testClearContactWithCompletionListener_delegatesToPredictInternal_whenPredictIsEnabled() {
        setup(predictConfig)

        clearContact(completionListener)
        runBlockingOnCoreSdkThread()

        runBlockingOnCoreSdkThread {
            verify {
                mockMobileEngageApi wasNot Called
                mockPredictRestricted.clearContact()
            }
        }
    }

    @Test
    fun testClearContactWithCompletionListener_delegatesToMobileApi_whenMobileEngageIsEnabled() {
        setup(mobileEngageConfig)

        clearContact(completionListener)

        runBlockingOnCoreSdkThread {
            verify {
                mockMobileEngageApi.clearContact(completionListener)
                mockPredictRestricted wasNot Called
            }
        }
    }

    @Test
    fun testClearContactWithCompletionListener_doNotDelegatesToPredictInternal_whenPredictIsDisabled() {
        setup(mobileEngageConfig)

        clearContact(completionListener)
        runBlockingOnCoreSdkThread {
            verify { mockPredictRestricted wasNot Called }
        }
    }

    @Test
    fun testClearContactWithCompletionListener_doNotDelegatesToMobileEngageApi_whenMobileEngageIsDisabled() {
        setup(predictConfig)

        clearContact(completionListener)

        verify { mockMobileEngageApi wasNot Called }
    }

    @Test
    fun testClearContactWithCompletionListener_delegatesToInternals_whenBothEnabled() {
        setup(mobileEngageAndPredictConfig)

        clearContact(completionListener)

        runBlockingOnCoreSdkThread {
            verify { mockPredictRestricted.clearContact() }
        }

        verify { mockMobileEngageApi.clearContact(completionListener) }
    }

    @Test
    fun testClearContactWithCompletionListener_delegatesToLoggingMobileEngageApiOnly_whenBothDisabled() {
        setup(baseConfig)

        clearContact(completionListener)
        runBlockingOnCoreSdkThread {
            verify { mockPredictRestricted wasNot Called }
        }
        verify { mockLoggingMobileEngageApi.clearContact(completionListener) }
    }

    @Test
    fun testTrackDeepLink_delegatesTo_deepLinkApi() {
        setup(createConfig().applicationCode(APPLICATION_CODE).build())
        val mockActivity: Activity = mockk(relaxed = true)
        val mockIntent: Intent = mockk(relaxed = true)
        trackDeepLink(mockActivity, mockIntent)

        runBlockingOnCoreSdkThread {
            verify { mockDeepLinkApi.trackDeepLinkOpen(mockActivity, mockIntent, null) }
        }
    }

    @Test
    fun testTrackCustomEventWithCompletionListener_delegatesTo_eventServiceApi() {
        setup(createConfig().applicationCode(APPLICATION_CODE).build())
        val eventName = "eventName"
        val eventAttributes = HashMap<String, String>()
        trackCustomEvent(eventName, eventAttributes, completionListener)

        runBlockingOnCoreSdkThread {
            verify {
                mockEventServiceApi.trackCustomEventAsync(
                    eventName,
                    eventAttributes,
                    completionListener
                )
            }
        }
    }

    @Test
    fun testTrackCustomEventWithCompletionListener_delegatesTo_loggingEventServiceApi() {
        val eventName = "eventName"
        val eventAttributes = HashMap<String, String>()
        trackCustomEvent(eventName, eventAttributes, completionListener)
        runBlockingOnCoreSdkThread()

        verify {
            mockLoggingEventServiceApi.trackCustomEventAsync(
                eventName,
                eventAttributes,
                completionListener
            )
        }
    }

    @Test
    fun testMobileEngageApiInstances_shouldAlwaysGetInstanceFromDI() {
        setup(predictConfig)

        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)
        Emarsys.inApp.isPaused
        verify {
            mockInApp.isPaused
            mockLoggingInApp wasNot Called
        }
    }

    @Test
    fun testPredictApiInstances_shouldAlwaysGetInstanceFromDI() {
        setup(mobileEngageConfig)

        FeatureRegistry.enableFeature(InnerFeature.PREDICT)
        Emarsys.predict.trackItemView("testItemId")
        verify {
            mockPredict.trackItemView("testItemId")
            mockLoggingPredict wasNot Called
        }

    }

    private fun createConfig(vararg experimentalFeatures: FlipperFeature): EmarsysConfig.Builder {
        return EmarsysConfig.Builder()
            .application(application)
            .enableExperimentalFeatures(*experimentalFeatures)
    }

    private fun verifyRemoteConfigFetchWithInvalidAppcode(appCode: String?) {
        val config = createConfig()
            .applicationCode(appCode)
            .merchantId(null)
            .build()
        setup(config)

        runBlockingOnCoreSdkThread()

        verify(exactly = 0) { mockConfigInternal.refreshRemoteConfig(any()) }
    }

    private fun runBlockingOnCoreSdkThread(callback: (() -> Unit)? = null) {
        val latch = CountDownLatch(1)
        var exception: Exception? = null
        emarsys().concurrentHandlerHolder.coreHandler.post {
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