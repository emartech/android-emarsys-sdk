package com.emarsys

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Looper
import com.emarsys.Emarsys.Config.applicationCode
import com.emarsys.Emarsys.Config.changeApplicationCode
import com.emarsys.Emarsys.Config.changeMerchantId
import com.emarsys.Emarsys.Config.contactFieldId
import com.emarsys.Emarsys.InApp.isPaused
import com.emarsys.Emarsys.InApp.pause
import com.emarsys.Emarsys.InApp.resume
import com.emarsys.Emarsys.InApp.setEventHandler
import com.emarsys.Emarsys.Inbox.fetchNotifications
import com.emarsys.Emarsys.Inbox.resetBadgeCount
import com.emarsys.Emarsys.Inbox.trackNotificationOpen
import com.emarsys.Emarsys.Predict.recommendProducts
import com.emarsys.Emarsys.Predict.trackCart
import com.emarsys.Emarsys.Predict.trackCategoryView
import com.emarsys.Emarsys.Predict.trackItemView
import com.emarsys.Emarsys.Predict.trackPurchase
import com.emarsys.Emarsys.Predict.trackRecommendationClick
import com.emarsys.Emarsys.Predict.trackSearchTerm
import com.emarsys.Emarsys.Predict.trackTag
import com.emarsys.Emarsys.Push.clearPushToken
import com.emarsys.Emarsys.Push.setNotificationEventHandler
import com.emarsys.Emarsys.Push.setPushToken
import com.emarsys.Emarsys.Push.setSilentMesssageEventHandler
import com.emarsys.Emarsys.Push.trackMessageOpen
import com.emarsys.Emarsys.clearContact
import com.emarsys.Emarsys.setContact
import com.emarsys.Emarsys.setup
import com.emarsys.Emarsys.trackCustomEvent
import com.emarsys.Emarsys.trackDeepLink
import com.emarsys.config.ConfigApi
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.api.experimental.FlipperFeature
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerType
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.batch.BatchingShardTrigger
import com.emarsys.di.DefaultEmarsysDependencyContainer
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.feature.InnerFeature
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.InboxApi
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.api.inbox.Notification
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.deeplink.DeepLinkAction
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.iam.InAppStartAction
import com.emarsys.mobileengage.iam.model.requestRepositoryProxy.RequestRepositoryProxy
import com.emarsys.mobileengage.responsehandler.*
import com.emarsys.predict.PredictApi
import com.emarsys.predict.PredictInternal
import com.emarsys.predict.api.model.CartItem
import com.emarsys.predict.api.model.Logic
import com.emarsys.predict.api.model.Product
import com.emarsys.predict.api.model.RecommendationFilter
import com.emarsys.predict.response.VisitorIdResponseHandler
import com.emarsys.predict.response.XPResponseHandler
import com.emarsys.push.PushApi
import com.emarsys.testUtil.CollectionTestUtils.getElementByType
import com.emarsys.testUtil.CollectionTestUtils.numberOfElementsIn
import com.emarsys.testUtil.FeatureTestUtils.resetFeatures
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.ReflectionTestUtils.getInstanceField
import com.emarsys.testUtil.TimeoutUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import org.junit.*
import org.junit.rules.TestRule
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.*

class EmarsysTest {
    companion object {
        private const val APPLICATION_CODE = "56789876"
        private const val CONTACT_FIELD_ID = 3
        private const val MERCHANT_ID = "merchantId"
        private const val SDK_VERSION = "sdkVersion"
        private const val CONTACT_ID = "CONTACT_ID"
        private fun cacheMocks() {
            Mockito.mock(Application::class.java)
            Mockito.mock(Activity::class.java)
            Mockito.mock(Intent::class.java)
        }

        @BeforeClass
        @JvmStatic
        fun beforeAll() {
            val options: FirebaseOptions = FirebaseOptions.Builder()
                    .setApplicationId("com.emarsys.sdk")
                    .build()

            try {
                FirebaseApp.initializeApp(getTargetContext(), options)
            } catch (ignored: java.lang.Exception) {

            }
        }

        @AfterClass
        @JvmStatic
        fun afterAll() {
            FirebaseApp.clearInstancesForTest()
        }

        init {
            cacheMocks()
        }
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var activityLifecycleWatchdog: ActivityLifecycleWatchdog
    private lateinit var currentActivityWatchdog: CurrentActivityWatchdog
    private lateinit var mockCoreSQLiteDatabase: CoreSQLiteDatabase
    private lateinit var mockLogShardTrigger: Runnable
    private lateinit var mockMobileEngageInternal: MobileEngageInternal
    private lateinit var mockDeepLinkInternal: DeepLinkInternal
    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var mockClientServiceInternal: ClientServiceInternal
    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockPredictInternal: PredictInternal
    private lateinit var mockPredictShardTrigger: Runnable
    private lateinit var mockDeviceInfoHashStorage: Storage<Int>
    private lateinit var mockContactFieldValueStorage: Storage<String>
    private lateinit var mockContactTokenStorage: Storage<String>
    private lateinit var mockClientStateStorage: Storage<String>
    private lateinit var mockInbox: InboxApi
    private lateinit var mockLoggingInbox: InboxApi
    private lateinit var mockInApp: InAppApi
    private lateinit var mockLoggingInApp: InAppApi
    private lateinit var mockPush: PushApi
    private lateinit var mockLoggingPush: PushApi
    private lateinit var mockPredict: PredictApi
    private lateinit var mockLoggingPredict: PredictApi
    private lateinit var mockConfig: ConfigApi
    private lateinit var mockHardwareIdProvider: HardwareIdProvider
    private lateinit var mockLanguageProvider: LanguageProvider
    private lateinit var mockNotificationManagerHelper: NotificationManagerHelper
    private lateinit var mockVersionProvider: VersionProvider
    private lateinit var mockLogic: Logic
    private lateinit var mockRecommendationFilter: RecommendationFilter
    private lateinit var inappEventHandler: EventHandler
    private lateinit var oldInappEventHandler: com.emarsys.mobileengage.api.EventHandler
    private lateinit var application: Application
    private lateinit var completionListener: CompletionListener
    private lateinit var mockResultListener: ResultListener<Try<List<Product>>>
    private lateinit var baseConfig: EmarsysConfig
    private lateinit var mobileEngageConfig: EmarsysConfig
    private lateinit var predictConfig: EmarsysConfig
    private lateinit var configWithInAppEventHandler: EmarsysConfig
    private lateinit var deviceInfo: DeviceInfo

    @Before
    fun init() {
        application = Mockito.spy(getTargetContext().applicationContext as Application)
        completionListener = Mockito.mock(CompletionListener::class.java)
        mockResultListener = Mockito.mock(ResultListener::class.java) as ResultListener<Try<List<Product>>>
        activityLifecycleWatchdog = Mockito.mock(ActivityLifecycleWatchdog::class.java)
        currentActivityWatchdog = Mockito.mock(CurrentActivityWatchdog::class.java)
        mockCoreSQLiteDatabase = Mockito.mock(CoreSQLiteDatabase::class.java)
        mockMobileEngageInternal = Mockito.mock(MobileEngageInternal::class.java)
        mockDeepLinkInternal = Mockito.mock(DeepLinkInternal::class.java)
        mockEventServiceInternal = Mockito.mock(EventServiceInternal::class.java)
        mockEventServiceInternal = Mockito.mock(EventServiceInternal::class.java)
        mockClientServiceInternal = Mockito.mock(ClientServiceInternal::class.java)
        mockPredictInternal = Mockito.mock(PredictInternal::class.java)
        mockPredictShardTrigger = Mockito.mock(BatchingShardTrigger::class.java)
        mockLogShardTrigger = Mockito.mock(BatchingShardTrigger::class.java)
        mockLanguageProvider = Mockito.mock(LanguageProvider::class.java)
        mockVersionProvider = Mockito.mock(VersionProvider::class.java)
        inappEventHandler = Mockito.mock(EventHandler::class.java)
        oldInappEventHandler = Mockito.mock(com.emarsys.mobileengage.api.EventHandler::class.java)
        mockDeviceInfoHashStorage = Mockito.mock(Storage::class.java) as Storage<Int>
        mockContactFieldValueStorage = Mockito.mock(Storage::class.java) as Storage<String>
        mockContactTokenStorage = Mockito.mock(Storage::class.java) as Storage<String>
        mockClientStateStorage = Mockito.mock(Storage::class.java) as Storage<String>
        mockNotificationManagerHelper = Mockito.mock(NotificationManagerHelper::class.java)
        configWithInAppEventHandler = createConfig().mobileEngageApplicationCode(APPLICATION_CODE).inAppEventHandler { eventName, payload -> oldInappEventHandler.handleEvent(eventName, payload) }.build()
        baseConfig = createConfig().build()
        mobileEngageConfig = createConfig().mobileEngageApplicationCode(APPLICATION_CODE).build()
        predictConfig = createConfig().predictMerchantId(MERCHANT_ID).build()
        mockRequestContext = Mockito.mock(MobileEngageRequestContext::class.java)
        mockHardwareIdProvider = Mockito.mock(HardwareIdProvider::class.java)
        mockInbox = Mockito.mock(InboxApi::class.java)
        mockLoggingInbox = Mockito.mock(InboxApi::class.java)
        mockInApp = Mockito.mock(InAppApi::class.java)
        mockLoggingInApp = Mockito.mock(InAppApi::class.java)
        mockPush = Mockito.mock(PushApi::class.java)
        mockLoggingPush = Mockito.mock(PushApi::class.java)
        mockPredict = Mockito.mock(PredictApi::class.java)
        mockLoggingPredict = Mockito.mock(PredictApi::class.java)
        mockConfig = Mockito.mock(ConfigApi::class.java)
        mockLogic = Mockito.mock(Logic::class.java)
        mockRecommendationFilter = Mockito.mock(RecommendationFilter::class.java)
        Mockito.`when`(mockHardwareIdProvider.provideHardwareId()).thenReturn("hwid")
        Mockito.`when`(mockLanguageProvider.provideLanguage(ArgumentMatchers.any(Locale::class.java))).thenReturn("language")
        Mockito.`when`(mockVersionProvider.provideSdkVersion()).thenReturn("version")
        deviceInfo = DeviceInfo(application, mockHardwareIdProvider, mockVersionProvider,
                mockLanguageProvider, mockNotificationManagerHelper, true)
        Mockito.`when`(mockRequestContext.applicationCode).thenReturn(APPLICATION_CODE)
        Mockito.`when`(mockDeviceInfoHashStorage.get()).thenReturn(deviceInfo.hash)
        Mockito.`when`(mockVersionProvider.provideSdkVersion()).thenReturn(SDK_VERSION)
        Mockito.`when`(mockContactFieldValueStorage.get()).thenReturn("test@test.com")
        Mockito.`when`(mockContactTokenStorage.get()).thenReturn("contactToken")
        DependencyInjection.setup(FakeDependencyContainer(
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
                clientServiceInternal = mockClientServiceInternal,
                loggingClientServiceInternal = mockClientServiceInternal,
                predictInternal = mockPredictInternal,
                loggingPredictInternal = mockPredictInternal,
                requestContext = mockRequestContext,
                predictShardTrigger = mockPredictShardTrigger,
                deviceInfoHashStorage = mockDeviceInfoHashStorage,
                contactFieldValueStorage = mockContactFieldValueStorage,
                contactTokenStorage = mockContactTokenStorage,
                clientStateStorage = mockClientStateStorage,
                inbox = mockInbox,
                loggingInbox = mockLoggingInbox,
                inApp = mockInApp,
                loggingInApp = mockLoggingInApp,
                push = mockPush,
                loggingPush = mockLoggingPush,
                predict = mockPredict,
                loggingPredict = mockLoggingPredict,
                config = mockConfig
        ))
        resetFeatures()
    }

    @After
    fun tearDown() {
        application.unregisterActivityLifecycleCallbacks(activityLifecycleWatchdog)
        application.unregisterActivityLifecycleCallbacks(currentActivityWatchdog)
        try {
            val looper: Looper? = DependencyInjection.getContainer<DependencyContainer>().coreSdkHandler.looper
            looper?.quitSafely()
            DependencyInjection.tearDown()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testSetup_whenMobileEngageApplicationCodeAndMerchantIdAreNull_mobileEngageAndPredict_shouldBeDisabled() {
        val config = createConfig().mobileEngageApplicationCode(null).predictMerchantId(null).build()
        setup(config)
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
    fun testSetup_initializesDependencyInjectionContainer() {
        DependencyInjection.tearDown()
        setup(baseConfig)
        val container = DependencyInjection.getContainer<DependencyContainer>()
        Assert.assertEquals(DefaultEmarsysDependencyContainer::class.java, container.javaClass)
    }

    @Test
    fun testSetup_initializesRequestManager_withRequestModelRepositoryProxy() {
        DependencyInjection.tearDown()
        setup(mobileEngageConfig)
        val requestManager: RequestManager? = getInstanceField(
                DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>(),
                "requestManager")
        val repository = getInstanceField<Any>(
                requestManager!!,
                "requestRepository")
        Assert.assertEquals(RequestRepositoryProxy::class.java, repository?.javaClass)
    }

    @Test
    fun testSetup_initializesCoreCompletionHandler_withNoFlippers() {
        DependencyInjection.tearDown()
        setup(mobileEngageConfig)
        val responseHandlersProcessor = DependencyInjection
                .getContainer<DefaultEmarsysDependencyContainer>()
                .responseHandlersProcessor
        Assert.assertNotNull(responseHandlersProcessor)
        Assert.assertEquals(8, responseHandlersProcessor.responseHandlers.size.toLong())
        Assert.assertEquals(1, numberOfElementsIn(responseHandlersProcessor.responseHandlers, VisitorIdResponseHandler::class.java).toLong())
        Assert.assertEquals(1, numberOfElementsIn(responseHandlersProcessor.responseHandlers, XPResponseHandler::class.java).toLong())
        Assert.assertEquals(1, numberOfElementsIn(responseHandlersProcessor.responseHandlers, InAppMessageResponseHandler::class.java).toLong())
        Assert.assertEquals(1, numberOfElementsIn(responseHandlersProcessor.responseHandlers, InAppCleanUpResponseHandler::class.java).toLong())
        Assert.assertEquals(2, numberOfElementsIn(responseHandlersProcessor.responseHandlers, MobileEngageTokenResponseHandler::class.java).toLong())
        Assert.assertEquals(1, numberOfElementsIn(responseHandlersProcessor.responseHandlers, MobileEngageClientStateResponseHandler::class.java).toLong())
        Assert.assertEquals(1, numberOfElementsIn(responseHandlersProcessor.responseHandlers, ClientInfoResponseHandler::class.java).toLong())
    }

    @Test
    fun testSetup_registersPredictTrigger_whenPredictIsEnabled() {
        setup(predictConfig)
        Mockito.verify(mockCoreSQLiteDatabase).registerTrigger("shard", TriggerType.AFTER, TriggerEvent.INSERT, mockPredictShardTrigger)
    }

    @Test
    fun testSetup_doNotRegistersPredictTrigger_whenPredictIsDisabled() {
        setup(mobileEngageConfig)
        val argumentCaptor = ArgumentCaptor.forClass(Runnable::class.java)
        Mockito.verify(mockCoreSQLiteDatabase, Mockito.times(1)).registerTrigger(ArgumentMatchers.any(String::class.java), ArgumentMatchers.any(TriggerType::class.java), ArgumentMatchers.any(TriggerEvent::class.java), argumentCaptor.capture())
        Assert.assertEquals(mockLogShardTrigger, argumentCaptor.value)
        Mockito.verifyNoMoreInteractions(mockCoreSQLiteDatabase)
    }

    @Test
    fun testSetup_registersLogTrigger() {
        setup(mobileEngageConfig)
        Mockito.verify(mockCoreSQLiteDatabase).registerTrigger("shard", TriggerType.AFTER, TriggerEvent.INSERT, mockLogShardTrigger)
    }

    @Test
    fun testSetup_registers_activityLifecycleWatchdog() {
        setup(mobileEngageConfig)
        Mockito.verify(application).registerActivityLifecycleCallbacks(activityLifecycleWatchdog)
    }

    @Test
    fun testSetup_registers_activityLifecycleWatchdog_withInAppStartAction() {
        DependencyInjection.tearDown()
        val captor = ArgumentCaptor.forClass(ActivityLifecycleWatchdog::class.java)
        setup(mobileEngageConfig)
        Mockito.verify(application, Mockito.times(2)).registerActivityLifecycleCallbacks(captor.capture())
        val actions = getElementByType(captor.allValues, ActivityLifecycleWatchdog::class.java)?.applicationStartActions?.toList()
        Assert.assertEquals(1, numberOfElementsIn(actions!!, InAppStartAction::class.java).toLong())
    }

    @Test
    fun testSetup_registers_activityLifecycleWatchdog_withDeepLinkAction() {
        DependencyInjection.tearDown()
        val captor = ArgumentCaptor.forClass(ActivityLifecycleWatchdog::class.java)
        setup(mobileEngageConfig)
        Mockito.verify(application, Mockito.times(2)).registerActivityLifecycleCallbacks(captor.capture())
        val actions = getElementByType(captor.allValues, ActivityLifecycleWatchdog::class.java)?.activityCreatedActions?.toList()
        Assert.assertEquals(1, numberOfElementsIn(actions!!, DeepLinkAction::class.java).toLong())
    }

    @Test
    fun testSetup_registers_currentActivityWatchDog() {
        setup(mobileEngageConfig)
        Mockito.verify(application).registerActivityLifecycleCallbacks(currentActivityWatchdog)
    }

    @Test
    fun testSetup_setsInAppEventHandler_whenProvidedInConfig() {
        setup(configWithInAppEventHandler)
        Mockito.verify(mockInApp).setEventHandler(any())
    }

    @Test
    fun testSetup_doesNotSetInAppEventHandler_whenMissingFromConfig() {
        setup(mobileEngageConfig)
        Mockito.verifyZeroInteractions(mockInApp)
    }

    @Test
    fun testSetup_sendClientInfo() {
        Mockito.`when`(mockClientStateStorage.get()).thenReturn(null)
        Mockito.`when`(mockContactFieldValueStorage.get()).thenReturn(null)
        Mockito.`when`(mockContactTokenStorage.get()).thenReturn(null)
        setup(mobileEngageConfig)
        Mockito.verify(mockClientServiceInternal).trackDeviceInfo()
    }

    @Test
    fun testSetup_doNotSendClientInfo_whenHashIsUnChanged() {
        Mockito.`when`(mockClientStateStorage.get()).thenReturn("asdfsaf")
        setup(mobileEngageConfig)
        Mockito.verify(mockClientServiceInternal, Mockito.never()).trackDeviceInfo()
    }

    @Test
    fun testSetup_doNotSendClientInfo_whenAnonymousContactIsNotNeededToSend() {
        Mockito.`when`(mockClientStateStorage.get()).thenReturn(null)
        Mockito.`when`(mockContactFieldValueStorage.get()).thenReturn("asdf")
        Mockito.`when`(mockContactTokenStorage.get()).thenReturn("asdf")
        setup(mobileEngageConfig)
        Mockito.verify(mockClientServiceInternal, Mockito.never()).trackDeviceInfo()
    }

    @Test
    fun testSetup_sendAnonymousContact() {
        Mockito.`when`(mockContactFieldValueStorage.get()).thenReturn(null)
        Mockito.`when`(mockContactTokenStorage.get()).thenReturn(null)
        setup(mobileEngageConfig)
        Mockito.verify(mockMobileEngageInternal).setContact(null, null)
    }

    @Test
    fun testSetup_sendDeviceInfoAndAnonymousContact_inOrder() {
        Mockito.`when`(mockContactFieldValueStorage.get()).thenReturn(null)
        Mockito.`when`(mockContactTokenStorage.get()).thenReturn(null)
        Mockito.`when`(mockDeviceInfoHashStorage.get()).thenReturn(2345)
        setup(mobileEngageConfig)
        val inOrder = Mockito.inOrder(mockMobileEngageInternal, mockClientServiceInternal)
        inOrder.verify(mockClientServiceInternal).trackDeviceInfo()
        inOrder.verify(mockMobileEngageInternal).setContact(null, null)
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun testSetup_doNotSendAnonymousContact_whenContactFieldValueIsPresent() {
        setup(mobileEngageConfig)
        Mockito.verify(mockMobileEngageInternal, Mockito.never()).setContact(null, null)
    }

    @Test
    fun testSetup_doNotSendAnonymousContact_whenContactTokenIsPresent() {
        Mockito.`when`(mockContactFieldValueStorage.get()).thenReturn(null)
        setup(mobileEngageConfig)
        Mockito.verify(mockMobileEngageInternal, Mockito.never()).setContact(null, null)
    }

    @Test
    fun testSetContactWithCompletionListener_delegatesToPredictInternal_whenPredictEnabled() {
        setup(predictConfig)
        setContact(CONTACT_ID, completionListener)
        Mockito.verifyZeroInteractions(mockMobileEngageInternal)
        Mockito.verify(mockPredictInternal).setContact(CONTACT_ID)
    }

    @Test
    fun testSetContactWithCompletionListener_delegatesToMobileEngageInternal_whenMobileEngageEnabled() {
        setup(mobileEngageConfig)
        setContact(CONTACT_ID, completionListener)
        Mockito.verifyZeroInteractions(mockPredictInternal)
        Mockito.verify(mockMobileEngageInternal).setContact(CONTACT_ID, completionListener)
    }

    @Test
    fun testSetContactWithCompletionListener_doNotDelegatesToPredictInternal_whenPredictDisabled() {
        setup(mobileEngageConfig)
        setContact(CONTACT_ID, completionListener)
        Mockito.verifyZeroInteractions(mockPredictInternal)
    }

    @Test
    fun testSetContactWithCompletionListener_doNotDelegatesToMobileEngageInternal_whenMobileEngageDisabled() {
        setup(predictConfig)
        setContact(CONTACT_ID, completionListener)
        Mockito.verifyZeroInteractions(mockMobileEngageInternal)
    }

    @Test
    fun testSetContactWithCompletionListener_delegatesToInternals_whenBothFeaturesEnabled() {
        setup(createConfig().mobileEngageApplicationCode(APPLICATION_CODE).predictMerchantId(MERCHANT_ID).build())
        setContact(CONTACT_ID, completionListener)
        Mockito.verify(mockPredictInternal).setContact(CONTACT_ID)
        Mockito.verify(mockMobileEngageInternal).setContact(CONTACT_ID, completionListener)
    }

    @Test
    fun testSetContactWithCompletionListener_delegatesToMobileEngageOnly_whenBothFeaturesDisabled() {
        setup(baseConfig)
        setContact(CONTACT_ID, completionListener)
        Mockito.verifyZeroInteractions(mockPredictInternal)
        Mockito.verify(mockMobileEngageInternal).setContact(CONTACT_ID, completionListener)
    }

    @Test
    fun testSetContact_delegatesToMobileEngageInternal_whenMobileEngageIsEnabled() {
        setup(mobileEngageConfig)
        setContact(CONTACT_ID)
        Mockito.verify(mockMobileEngageInternal).setContact(CONTACT_ID, null)
    }

    @Test
    fun testSetContact_delegatesToInternal_whenPredictIsEnabled() {
        setup(predictConfig)
        setContact(CONTACT_ID)
        Mockito.verify(mockPredictInternal).setContact(CONTACT_ID)
    }

    @Test
    fun testSetContact_doNotDelegatesToMobileEngageInternal_whenMobileEngageIsDisabled() {
        setup(predictConfig)
        setContact(CONTACT_ID)
        Mockito.verifyZeroInteractions(mockMobileEngageInternal)
    }

    @Test
    fun testSetContact_doNotDelegatesToPredictInternal_whenPredictIsDisabled() {
        setup(mobileEngageConfig)
        setContact(CONTACT_ID)
        Mockito.verifyZeroInteractions(mockPredictInternal)
    }

    @Test
    fun testSetContact_delegatesToInternals_whenBothFeaturesAreEnabled() {
        setup(createConfig().mobileEngageApplicationCode(APPLICATION_CODE).predictMerchantId(MERCHANT_ID).build())
        setContact(CONTACT_ID)
        Mockito.verify(mockPredictInternal).setContact(CONTACT_ID)
        Mockito.verify(mockMobileEngageInternal).setContact(CONTACT_ID, null)
    }

    @Test
    fun testSetContact_delegatesToMobileEngageInternalOnly_whenBothFeaturesAreDisabled() {
        setup(baseConfig)
        setContact(CONTACT_ID)
        Mockito.verifyZeroInteractions(mockPredictInternal)
        Mockito.verify(mockMobileEngageInternal).setContact(CONTACT_ID, null)
    }

    @Test
    fun testClearContactWithCompletionListener_delegatesToPredictInternal_whenPredictIsEnabled() {
        setup(predictConfig)
        clearContact(completionListener)
        Mockito.verifyZeroInteractions(mockMobileEngageInternal)
        Mockito.verify(mockPredictInternal).clearContact()
    }

    @Test
    fun testClearContactWithCompletionListener_delegatesToMobileEngageInternal_whenMobileEngageIsEnabled() {
        setup(mobileEngageConfig)
        clearContact(completionListener)
        Mockito.verifyZeroInteractions(mockPredictInternal)
        Mockito.verify(mockMobileEngageInternal).clearContact(completionListener)
    }

    @Test
    fun testClearContactWithCompletionListener_doNotDelegatesToPredictInternal_whenPredictIsDisabled() {
        setup(mobileEngageConfig)
        clearContact(completionListener)
        Mockito.verifyZeroInteractions(mockPredictInternal)
    }

    @Test
    fun testClearContactWithCompletionListener_doNotDelegatesToMobileEngageInternal_whenMobileEngageIsDisabled() {
        setup(predictConfig)
        clearContact(completionListener)
        Mockito.verifyZeroInteractions(mockMobileEngageInternal)
    }

    @Test
    fun testClearContactWithCompletionListener_delegatesToInternals_whenBothEnabled() {
        setup(createConfig().mobileEngageApplicationCode(APPLICATION_CODE).predictMerchantId(MERCHANT_ID).build())
        clearContact(completionListener)
        Mockito.verify(mockPredictInternal).clearContact()
        Mockito.verify(mockMobileEngageInternal).clearContact(completionListener)
    }

    @Test
    fun testClearContactWithCompletionListener_delegatesToMobileEngageInternalOnly_whenBothDisabled() {
        setup(baseConfig)
        clearContact(completionListener)
        Mockito.verifyZeroInteractions(mockPredictInternal)
        Mockito.verify(mockMobileEngageInternal).clearContact(completionListener)
    }

    @Test
    fun testClearContact_delegatesToMobileEngageInternal_whenMobileEngageIsEnabled() {
        setup(mobileEngageConfig)
        clearContact()
        Mockito.verifyZeroInteractions(mockPredictInternal)
        Mockito.verify(mockMobileEngageInternal).clearContact(null)
    }

    @Test
    fun testClearContact_doNotDelegatesToPredictInternal_whenPredictIsDisabled() {
        setup(mobileEngageConfig)
        clearContact()
        Mockito.verifyZeroInteractions(mockPredictInternal)
    }

    @Test
    fun testClearContact_doNotDelegatesToMobileEngageInternal_whenMobileEngageIsDisabled() {
        setup(predictConfig)
        clearContact()
        Mockito.verifyZeroInteractions(mockMobileEngageInternal)
    }

    @Test
    fun testClearContact_delegatesToPredictInternal_whenPredictIsEnabled() {
        setup(predictConfig)
        clearContact()
        Mockito.verifyZeroInteractions(mockMobileEngageInternal)
        Mockito.verify(mockPredictInternal).clearContact()
    }

    @Test
    fun testClearContact_delegatesToInternals_whenBothFeaturesAreEnabled() {
        setup(createConfig().mobileEngageApplicationCode(APPLICATION_CODE).predictMerchantId(MERCHANT_ID).build())
        clearContact()
        Mockito.verify(mockPredictInternal).clearContact()
        Mockito.verify(mockMobileEngageInternal).clearContact(null)
    }

    @Test
    fun testClearContact_shouldCallMobileEngageOnly_whenBothFeaturesAreDisabled() {
        setup(baseConfig)
        clearContact()
        Mockito.verifyZeroInteractions(mockPredictInternal)
        Mockito.verify(mockMobileEngageInternal).clearContact(null)
    }

    @Test
    fun testTrackDeepLink_delegatesTo_deepLinkInternal() {
        val mockActivity = Mockito.mock(Activity::class.java)
        val mockIntent = Mockito.mock(Intent::class.java)
        trackDeepLink(mockActivity, mockIntent)
        Mockito.verify(mockDeepLinkInternal).trackDeepLinkOpen(mockActivity, mockIntent, null)
    }

    @Test
    fun testTrackDeepLinkWithCompletionListener_delegatesTo_deepLinkInternal() {
        val mockActivity = Mockito.mock(Activity::class.java)
        val mockIntent = Mockito.mock(Intent::class.java)
        trackDeepLink(mockActivity, mockIntent, completionListener)
        Mockito.verify(mockDeepLinkInternal).trackDeepLinkOpen(mockActivity, mockIntent, completionListener)
    }

    @Test
    fun testTrackCustomEvent_delegatesTo_mobileEngageInternal() {
        val eventName = "eventName"
        val eventAttributes = HashMap<String, String>()
        trackCustomEvent(eventName, eventAttributes)
        Mockito.verify(mockEventServiceInternal).trackCustomEvent(eventName, eventAttributes, null)
    }

    @Test
    fun testTrackCustomEventWithCompletionListener_delegatesTo_mobileEngageInternal() {
        val eventName = "eventName"
        val eventAttributes = HashMap<String, String>()
        trackCustomEvent(eventName, eventAttributes, completionListener)
        Mockito.verify(mockEventServiceInternal).trackCustomEvent(eventName, eventAttributes, completionListener)
    }

    @Test
    fun testConfig_changeApplicationCode_delegatesTo_configInstance() {
        setup(mobileEngageConfig)
        changeApplicationCode(APPLICATION_CODE)
        Mockito.verify(mockConfig).changeApplicationCode(APPLICATION_CODE, null as CompletionListener?)
    }

    @Test
    fun testConfig_changeApplicationCode_withCompletionListener_delegatesTo_configInstance() {
        setup(mobileEngageConfig)
        changeApplicationCode(APPLICATION_CODE, completionListener)
        Mockito.verify(mockConfig).changeApplicationCode(APPLICATION_CODE, completionListener)
    }

    @Test
    fun testConfig_getApplicationCode_delegatesTo_configInstance() {
        setup(mobileEngageConfig)
        Mockito.`when`(mockConfig.applicationCode).thenReturn(APPLICATION_CODE)
        val applicationCode = applicationCode
        Mockito.verify(mockConfig).applicationCode
        Assert.assertEquals(APPLICATION_CODE, applicationCode)
    }

    @Test
    fun testConfig_changeMerchantId_delegatesTo_configInstance() {
        setup(predictConfig)
        changeMerchantId(MERCHANT_ID)
        Mockito.verify(mockConfig).changeMerchantId(MERCHANT_ID)
    }

    @Test
    fun testConfig_getContactFieldId_delegatesTo_configInstance() {
        setup(baseConfig)
        Mockito.`when`(mockConfig.contactFieldId).thenReturn(CONTACT_FIELD_ID)
        val contactFieldId = contactFieldId
        Mockito.verify(mockConfig).contactFieldId
        Assert.assertEquals(CONTACT_FIELD_ID.toLong(), contactFieldId.toLong())
    }

    @Test
    fun testPush_trackMessageOpen_delegatesTo_pushInstance() {
        setup(mobileEngageConfig)
        val mockIntent = Mockito.mock(Intent::class.java)
        trackMessageOpen(mockIntent)
        Mockito.verify(mockPush).trackMessageOpen(mockIntent)
    }

    @Test
    fun testPush_trackMessageOpen_withCompletionListener_delegatesTo_pushInstance() {
        setup(mobileEngageConfig)
        val mockIntent = Mockito.mock(Intent::class.java)
        val mockCompletionListener = Mockito.mock(CompletionListener::class.java)
        trackMessageOpen(mockIntent, mockCompletionListener)
        Mockito.verify(mockPush).trackMessageOpen(mockIntent, mockCompletionListener)
    }

    @Test
    fun testPush_setPushToken_delegatesTo_pushInstance() {
        setup(mobileEngageConfig)
        setPushToken("pushToken")
        Mockito.verify(mockPush).setPushToken("pushToken")
    }

    @Test
    fun testPush_setPushToken_withCompletionListener_delegatesTo_pushInstance() {
        setup(mobileEngageConfig)
        val mockCompletionListener = Mockito.mock(CompletionListener::class.java)
        setPushToken("pushToken", mockCompletionListener)
        Mockito.verify(mockPush).setPushToken("pushToken", mockCompletionListener)
    }

    @Test
    fun testPush_clearPushToken_delegatesTo_pushInstance() {
        setup(mobileEngageConfig)
        clearPushToken()
        Mockito.verify(mockPush).clearPushToken()
    }

    @Test
    fun testPush_clearPushToken_withCompletionListener_delegatesTo_pushInstance() {
        setup(mobileEngageConfig)
        val mockCompletionListener = Mockito.mock(CompletionListener::class.java)
        clearPushToken(mockCompletionListener)
        Mockito.verify(mockPush).clearPushToken(mockCompletionListener)
    }

    @Test
    fun testPush_setNotificationEventhandler_delegatesTo_pushInstance() {
        setup(mobileEngageConfig)
        val mockEventHandler: EventHandler = mock()
        setNotificationEventHandler(mockEventHandler)
        Mockito.verify(mockPush).setNotificationEventHandler(mockEventHandler)
    }

    @Test
    fun testPush_setSilentMessageEventhandler_delegatesTo_pushInstance() {
        setup(mobileEngageConfig)
        val mockEventHandler: EventHandler = mock()
        setSilentMesssageEventHandler(mockEventHandler)
        Mockito.verify(mockPush).setSilentMessageEventHandler(mockEventHandler)
    }

    @Test
    fun testPredict_trackCart_delegatesTo_predictInstance() {
        setup(predictConfig)
        val cartItems: List<CartItem> = ArrayList()
        trackCart(cartItems)
        Mockito.verify(mockPredict).trackCart(cartItems)
    }

    @Test
    fun testPredict_trackPurchase_delegatesTo_predictInstance() {
        setup(predictConfig)
        val cartItems: List<CartItem> = ArrayList()
        trackPurchase("orderId", cartItems)
        Mockito.verify(mockPredict).trackPurchase("orderId", cartItems)
    }

    @Test
    fun testPredict_trackItemView_delegatesTo_predictInstance() {
        setup(predictConfig)
        trackItemView("itemId")
        Mockito.verify(mockPredict).trackItemView("itemId")
    }

    @Test
    fun testPredict_trackCategoryView_delegatesTo_predictInstance() {
        setup(predictConfig)
        trackCategoryView("categoryPath")
        Mockito.verify(mockPredict).trackCategoryView("categoryPath")
    }

    @Test
    fun testPredict_trackSearchTerm_delegatesTo_predictInstance() {
        setup(predictConfig)
        trackSearchTerm("searchTerm")
        Mockito.verify(mockPredict).trackSearchTerm("searchTerm")
    }

    @Test
    fun testPredict_trackTag_delegatesTo_predictInstance() {
        setup(predictConfig)
        trackTag("testTag", HashMap())
        Mockito.verify(mockPredict).trackTag("testTag", HashMap())
    }

    @Test
    fun testPredict_recommendProducts_delegatesTo_predictInstance() {
        setup(predictConfig)
        recommendProducts(mockLogic, mockResultListener)
        Mockito.verify(mockPredict).recommendProducts(mockLogic, mockResultListener)
    }

    @Test
    fun testPredict_recommendProductsWithLimit_delegatesTo_predictInstance() {
        setup(predictConfig)
        recommendProducts(mockLogic, 5, mockResultListener)
        Mockito.verify(mockPredict).recommendProducts(mockLogic, 5, mockResultListener)
    }

    @Test
    fun testPredict_recommendProductsWithFilters_delegatesTo_predictInstance() {
        setup(predictConfig)
        recommendProducts(mockLogic, listOf(mockRecommendationFilter), mockResultListener)
        Mockito.verify(mockPredict).recommendProducts(mockLogic, listOf(mockRecommendationFilter), mockResultListener)
    }

    @Test
    fun testPredict_recommendProductsWithLimitAndFilters_delegatesTo_predictInstance() {
        setup(predictConfig)
        recommendProducts(mockLogic, listOf(mockRecommendationFilter), 123, mockResultListener)
        Mockito.verify(mockPredict).recommendProducts(mockLogic, listOf(mockRecommendationFilter), 123, mockResultListener)
    }

    @Test
    fun testPredict_trackRecommendationClick_delegatesTo_predictInstance() {
        setup(predictConfig)
        val product = Product.Builder("itemId", "title", "https://emarsys.com", "RELATED", "AAAA").build()
        trackRecommendationClick(product)
        Mockito.verify(mockPredict).trackRecommendationClick(product)
    }

    @Test
    fun testInApp_pause_delegatesTo_inAppInstance() {
        setup(mobileEngageConfig)
        pause()
        Mockito.verify(mockInApp).pause()
    }

    @Test
    fun testInApp_resume_delegatesTo_inAppInstance() {
        setup(mobileEngageConfig)
        resume()
        Mockito.verify(mockInApp).resume()
    }

    @Test
    fun testInApp_isPaused_delegatesTo_inAppInstance() {
        setup(mobileEngageConfig)
        isPaused
        Mockito.verify(mockInApp).isPaused
    }

    @Test
    fun testInApp_setEventHandler_delegatesTo_inAppInstance() {
        setup(mobileEngageConfig)
        val mockEventHandler = Mockito.mock(EventHandler::class.java)
        setEventHandler(mockEventHandler)
        Mockito.verify(mockInApp).setEventHandler(mockEventHandler)
    }

    @Test
    fun testInbox_fetchNotification_delegatesTo_inboxInstance() {
        setup(mobileEngageConfig)
        val mockResultListener = Mockito.mock(ResultListener::class.java) as ResultListener<Try<NotificationInboxStatus>>
        fetchNotifications(mockResultListener)
        Mockito.verify(mockInbox).fetchNotifications(mockResultListener)
    }

    @Test
    fun testInbox_trackNotificationOpen_delegatesTo_inboxInstance() {
        setup(mobileEngageConfig)
        val mockNotification = Mockito.mock(Notification::class.java)
        trackNotificationOpen(mockNotification)
        Mockito.verify(mockInbox).trackNotificationOpen(mockNotification)
    }

    @Test
    fun testInbox_trackNotificationOpen_withCompletionListener_delegatesTo_inboxInstance() {
        setup(mobileEngageConfig)
        val mockNotification = Mockito.mock(Notification::class.java)
        val mockCompletionListener = Mockito.mock(CompletionListener::class.java)
        trackNotificationOpen(mockNotification, mockCompletionListener)
        Mockito.verify(mockInbox).trackNotificationOpen(mockNotification, mockCompletionListener)
    }

    @Test
    fun testInbox_resetBadgeCount_delegatesTo_inboxInstance() {
        setup(mobileEngageConfig)
        resetBadgeCount()
        Mockito.verify(mockInbox).resetBadgeCount()
    }

    @Test
    fun testInbox_resetBadgeCount_withCompletionListener_delegatesTo_inboxInstance() {
        setup(mobileEngageConfig)
        val mockCompletionListener = Mockito.mock(CompletionListener::class.java)
        resetBadgeCount(mockCompletionListener)
        Mockito.verify(mockInbox).resetBadgeCount(mockCompletionListener)
    }

    @Test
    fun testMobileEngageApiInstances_shouldAlwaysGetInstanceFromDI() {
        setup(predictConfig)
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)
        isPaused
        Mockito.verify(mockInApp).isPaused
        Mockito.verifyZeroInteractions(mockLoggingInApp)
    }

    @Test
    fun testPredictApiInstances_shouldAlwaysGetInstanceFromDI() {
        setup(mobileEngageConfig)
        FeatureRegistry.enableFeature(InnerFeature.PREDICT)
        trackItemView("testItemId")
        Mockito.verify(mockPredict).trackItemView("testItemId")
        Mockito.verifyZeroInteractions(mockLoggingPredict)
    }

    private fun createConfig(vararg experimentalFeatures: FlipperFeature): EmarsysConfig.Builder {
        return EmarsysConfig.Builder()
                .application(application)
                .contactFieldId(CONTACT_FIELD_ID)
                .enableExperimentalFeatures(*experimentalFeatures)
    }
}