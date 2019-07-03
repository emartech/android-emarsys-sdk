package com.emarsys;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.emarsys.config.EmarsysConfig;
import com.emarsys.core.RunnerProxy;
import com.emarsys.core.activity.ActivityLifecycleAction;
import com.emarsys.core.activity.ActivityLifecycleWatchdog;
import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.core.api.experimental.FlipperFeature;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.concurrency.CoreSdkHandler;
import com.emarsys.core.database.CoreSQLiteDatabase;
import com.emarsys.core.database.trigger.TriggerEvent;
import com.emarsys.core.database.trigger.TriggerType;
import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.device.LanguageProvider;
import com.emarsys.core.di.DependencyContainer;
import com.emarsys.core.di.DependencyInjection;
import com.emarsys.core.feature.FeatureRegistry;
import com.emarsys.core.notification.NotificationManagerHelper;
import com.emarsys.core.provider.hardwareid.HardwareIdProvider;
import com.emarsys.core.provider.version.VersionProvider;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.response.ResponseHandlersProcessor;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.batch.BatchingShardTrigger;
import com.emarsys.core.util.log.Logger;
import com.emarsys.di.DefaultEmarsysDependencyContainer;
import com.emarsys.di.EmarysDependencyContainer;
import com.emarsys.di.FakeDependencyContainer;
import com.emarsys.feature.InnerFeature;
import com.emarsys.inapp.InAppApi;
import com.emarsys.inapp.InAppProxy;
import com.emarsys.inbox.InboxApi;
import com.emarsys.inbox.InboxProxy;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.RefreshTokenInternal;
import com.emarsys.mobileengage.api.EventHandler;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.client.ClientServiceInternal;
import com.emarsys.mobileengage.deeplink.DeepLinkAction;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.event.EventServiceInternal;
import com.emarsys.mobileengage.iam.InAppStartAction;
import com.emarsys.mobileengage.iam.model.requestRepositoryProxy.RequestRepositoryProxy;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.responsehandler.ClientInfoResponseHandler;
import com.emarsys.mobileengage.responsehandler.InAppCleanUpResponseHandler;
import com.emarsys.mobileengage.responsehandler.InAppMessageResponseHandler;
import com.emarsys.mobileengage.responsehandler.MobileEngageClientStateResponseHandler;
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler;
import com.emarsys.predict.PredictApi;
import com.emarsys.predict.PredictInternal;
import com.emarsys.predict.PredictProxy;
import com.emarsys.predict.api.model.CartItem;
import com.emarsys.predict.response.VisitorIdResponseHandler;
import com.emarsys.push.PushApi;
import com.emarsys.push.PushProxy;
import com.emarsys.testUtil.CollectionTestUtils;
import com.emarsys.testUtil.FeatureTestUtils;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.ReflectionTestUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class EmarsysTest {

    static {
        cacheMocks();
    }

    private static final String APPLICATION_CODE = "56789876";
    private static final int CONTACT_FIELD_ID = 3;
    private static final String MERCHANT_ID = "merchantId";
    private static final String SDK_VERSION = "sdkVersion";
    private static final String CONTACT_ID = "CONTACT_ID";

    private CoreSdkHandler mockCoreSdkHandler;
    private ActivityLifecycleWatchdog activityLifecycleWatchdog;
    private CurrentActivityWatchdog currentActivityWatchdog;
    private MobileEngageInternal mockMobileEngageInternal;
    private PredictInternal mockPredictInternal;
    private EventServiceInternal mockEventServiceInternal;
    private ClientServiceInternal mockClientServiceInternal;
    private DeepLinkInternal mockDeepLinkInternal;
    private RefreshTokenInternal mockRefreshTokenInternal;
    private LanguageProvider mockLanguageProvider;
    private VersionProvider mockVersionProvider;
    private EventHandler inappEventHandler;
    private CoreSQLiteDatabase mockCoreDatabase;
    private Runnable mockPredictShardTrigger;
    private Runnable mockLogShardTrigger;
    private RunnerProxy runnerProxy;
    private Logger logger;

    private Application application;
    private CompletionListener completionListener;

    private EmarsysConfig baseConfig;
    private EmarsysConfig mobileEngageConfig;
    private EmarsysConfig predictConfig;
    private EmarsysConfig configWithInAppEventHandler;
    private Storage<Integer> mockDeviceInfoHashStorage;
    private Storage<String> mockContactFieldValueStorage;
    private Storage<String> mockContactTokenStorage;
    private Storage<String> mockClientStateStorage;
    private ResponseHandlersProcessor mockResponseHandlersProcessor;
    private NotificationManagerHelper mockNotificationManagerHelper;
    private DeviceInfo deviceInfo;
    private NotificationCache mockNotificationCache;

    private PredictApi mockPredict;
    private PushApi mockPush;
    private InAppApi mockInApp;
    private InboxApi mockInbox;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        application = spy((Application) InstrumentationRegistry.getTargetContext().getApplicationContext());

        completionListener = mock(CompletionListener.class);

        mockCoreSdkHandler = mock(CoreSdkHandler.class);
        activityLifecycleWatchdog = mock(ActivityLifecycleWatchdog.class);
        currentActivityWatchdog = mock(CurrentActivityWatchdog.class);
        mockMobileEngageInternal = mock(MobileEngageInternal.class);
        mockEventServiceInternal = mock(EventServiceInternal.class);
        mockDeepLinkInternal = mock(DeepLinkInternal.class);
        mockPredictInternal = mock(PredictInternal.class);
        mockEventServiceInternal = mock(EventServiceInternal.class);
        mockClientServiceInternal = mock(ClientServiceInternal.class);
        mockCoreDatabase = mock(CoreSQLiteDatabase.class);
        mockPredictShardTrigger = mock(BatchingShardTrigger.class);
        mockLogShardTrigger = mock(BatchingShardTrigger.class);
        mockLanguageProvider = mock(LanguageProvider.class);
        mockVersionProvider = mock(VersionProvider.class);
        inappEventHandler = mock(EventHandler.class);
        runnerProxy = new RunnerProxy();
        logger = mock(Logger.class);
        mockRefreshTokenInternal = mock(RefreshTokenInternal.class);
        mockDeviceInfoHashStorage = mock(Storage.class);
        mockContactFieldValueStorage = mock(Storage.class);
        mockContactTokenStorage = mock(Storage.class);
        mockClientStateStorage = mock(Storage.class);
        mockResponseHandlersProcessor = mock(ResponseHandlersProcessor.class);
        mockNotificationManagerHelper = mock(NotificationManagerHelper.class);
        mockNotificationCache = mock(NotificationCache.class);
        configWithInAppEventHandler = createConfig().mobileEngageApplicationCode(APPLICATION_CODE).inAppEventHandler(inappEventHandler).build();
        baseConfig = createConfig().build();
        mobileEngageConfig = createConfig().mobileEngageApplicationCode(APPLICATION_CODE).build();
        predictConfig = createConfig().predictMerchantId(MERCHANT_ID).build();

        mockInbox = mock(InboxApi.class);
        mockInApp = mock(InAppApi.class);
        mockPush = mock(PushApi.class);
        mockPredict = mock(PredictApi.class);

        HardwareIdProvider hardwareIdProvider = mock(HardwareIdProvider.class);
        deviceInfo = new DeviceInfo(application, hardwareIdProvider, mockVersionProvider, mockLanguageProvider, mockNotificationManagerHelper, true);

        when(mockDeviceInfoHashStorage.get()).thenReturn(deviceInfo.getHash());
        when(mockVersionProvider.provideSdkVersion()).thenReturn(SDK_VERSION);
        when(mockContactFieldValueStorage.get()).thenReturn("test@test.com");
        when(mockContactTokenStorage.get()).thenReturn("contactToken");

        DependencyInjection.setup(new FakeDependencyContainer(
                mockCoreSdkHandler,
                activityLifecycleWatchdog,
                currentActivityWatchdog,
                mockCoreDatabase,
                deviceInfo,
                null,
                null,
                null,
                mockLogShardTrigger,
                mockMobileEngageInternal,
                null,
                null,
                null,
                mockRefreshTokenInternal,
                mockDeepLinkInternal,
                mockEventServiceInternal,
                mockClientServiceInternal,
                null,
                null,
                null,
                null,
                mockPredictInternal,
                mockPredictShardTrigger,
                runnerProxy,
                logger,
                mockDeviceInfoHashStorage,
                mockContactFieldValueStorage,
                mockContactTokenStorage,
                mockClientStateStorage,
                mockResponseHandlersProcessor,
                mockNotificationCache,
                null,
                mockInbox,
                mockInApp,
                mockPush,
                mockPredict
        ));
        FeatureTestUtils.resetFeatures();
    }

    @After
    public void tearDown() {
        try {
            Looper looper = DependencyInjection.getContainer().getCoreSdkHandler().getLooper();
            if (looper != null) {
                looper.quit();
            }
            DependencyInjection.tearDown();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetup_config_mustNotBeNull() {
        Emarsys.setup(null);
    }

    @Test
    public void testSetup_whenMobileEngageApplicationCodeAndMerchantIdAreNull_mobileEngageAndPredict_shouldBeDisabled() {
        EmarsysConfig config = createConfig().mobileEngageApplicationCode(null).predictMerchantId(null).build();

        Emarsys.setup(config);

        Assert.assertEquals(false, FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE));
        Assert.assertEquals(false, FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT));
    }

    @Test
    public void testSetup_whenMobileEngageApplicationCodeIsNotNull_mobileEngageFeature_shouldBeEnabled() {
        Emarsys.setup(mobileEngageConfig);

        Assert.assertTrue(FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE));
    }

    @Test
    public void testSetup_whenPredictMerchantIdIsNotNull_predictFeature_shouldBeEnabled() {
        Emarsys.setup(predictConfig);

        Assert.assertTrue(FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT));
    }

    @Test
    public void testSetup_initializesDependencyInjectionContainer() {
        DependencyInjection.tearDown();

        Emarsys.setup(baseConfig);

        DependencyContainer container = DependencyInjection.getContainer();
        Assert.assertEquals(DefaultEmarsysDependencyContainer.class, container.getClass());
    }

    @Test
    public void testSetup_initializes_mobileEngageInstance() {
        DependencyInjection.tearDown();

        Emarsys.setup(mobileEngageConfig);

        assertNotNull(DependencyInjection.<EmarysDependencyContainer>getContainer().getMobileEngageInternal());
    }

    @Test
    public void testSetup_initializes_ClientInstance() {
        DependencyInjection.tearDown();

        Emarsys.setup(mobileEngageConfig);

        assertNotNull(DependencyInjection.<EmarysDependencyContainer>getContainer().getClientServiceInternal());
    }

    @Test
    public void testSetup_initializes_PushInstance() {
        DependencyInjection.tearDown();

        Emarsys.setup(mobileEngageConfig);

        PushApi push = Emarsys.getPush();

        assertNotNull(DependencyInjection.<EmarysDependencyContainer>getContainer().getPushInternal());
        assertNotNull(push);
        assertEquals(PushProxy.class, push.getClass());
    }

    @Test
    public void testSetup_initializes_inboxInstance_V1() {
        DependencyInjection.tearDown();

        Emarsys.setup(baseConfig);

        InboxApi inbox = Emarsys.getInbox();

        assertNotNull(DependencyInjection.<EmarysDependencyContainer>getContainer().getInboxInternal());
        assertNotNull(inbox);
        assertEquals(InboxProxy.class, inbox.getClass());

    }

    @Test
    public void testSetup_initializes_deepLinkInstance() {
        Emarsys.setup(mobileEngageConfig);

        assertNotNull(DependencyInjection.<EmarysDependencyContainer>getContainer().getDeepLinkInternal());
    }

    @Test
    public void testSetup_initializes_predictInstance() {
        DependencyInjection.tearDown();

        Emarsys.setup(mobileEngageConfig);

        PredictApi predict = Emarsys.getPredict();

        assertNotNull(DependencyInjection.<EmarysDependencyContainer>getContainer().getPredictInternal());
        assertNotNull(predict);
        assertEquals(PredictProxy.class, predict.getClass());

    }

    @Test
    public void testSetup_initializes_inAppInstance() {
        DependencyInjection.tearDown();

        Emarsys.setup(mobileEngageConfig);

        InAppApi inApp = Emarsys.getInApp();

        assertNotNull(DependencyInjection.<EmarysDependencyContainer>getContainer().getInAppInternal());
        assertNotNull(inApp);
        assertEquals(InAppProxy.class, inApp.getClass());

    }

    @Test
    public void testSetup_initializesRequestManager_withRequestModelRepositoryProxy() {
        DependencyInjection.tearDown();

        Emarsys.setup(mobileEngageConfig);

        RequestManager requestManager = ReflectionTestUtils.getInstanceField(
                DependencyInjection.<DefaultEmarsysDependencyContainer>getContainer(),
                "requestManager");
        Object repository = ReflectionTestUtils.getInstanceField(
                requestManager,
                "requestRepository");
        assertEquals(RequestRepositoryProxy.class, repository.getClass());
    }

    @Test
    public void testSetup_initializesCoreCompletionHandler_withNoFlippers() {
        DependencyInjection.tearDown();

        Emarsys.setup(mobileEngageConfig);

        ResponseHandlersProcessor responseHandlersProcessor = DependencyInjection
                .<DefaultEmarsysDependencyContainer>getContainer()
                .getResponseHandlersProcessor();

        assertNotNull(responseHandlersProcessor);
        assertEquals(7, responseHandlersProcessor.getResponseHandlers().size());
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), VisitorIdResponseHandler.class));
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), InAppMessageResponseHandler.class));
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), InAppCleanUpResponseHandler.class));
        assertEquals(2, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), MobileEngageTokenResponseHandler.class));
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), MobileEngageClientStateResponseHandler.class));
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), ClientInfoResponseHandler.class));

    }

    @Test
    public void testSetup_registersPredictTrigger() {
        Emarsys.setup(predictConfig);

        verify(mockCoreDatabase).registerTrigger("shard", TriggerType.AFTER, TriggerEvent.INSERT, mockPredictShardTrigger);
    }

    @Test
    public void testSetup_registersLogTrigger() {
        Emarsys.setup(mobileEngageConfig);

        verify(mockCoreDatabase).registerTrigger("shard", TriggerType.AFTER, TriggerEvent.INSERT, mockLogShardTrigger);
    }

    @Test
    public void testSetup_registers_activityLifecycleWatchdog() {
        Emarsys.setup(mobileEngageConfig);

        verify(application).registerActivityLifecycleCallbacks(activityLifecycleWatchdog);
    }

    @Test
    public void testSetup_registers_activityLifecycleWatchdog_withInAppStartAction() {
        DependencyInjection.tearDown();

        ArgumentCaptor<ActivityLifecycleWatchdog> captor = ArgumentCaptor.forClass(ActivityLifecycleWatchdog.class);

        Emarsys.setup(mobileEngageConfig);

        verify(application, times(2)).registerActivityLifecycleCallbacks(captor.capture());
        ActivityLifecycleAction[] actions = CollectionTestUtils.getElementByType(captor.getAllValues(), ActivityLifecycleWatchdog.class).getApplicationStartActions();

        assertEquals(1, CollectionTestUtils.numberOfElementsIn(actions, InAppStartAction.class));
    }

    @Test
    public void testSetup_registers_activityLifecycleWatchdog_withDeepLinkAction() {
        DependencyInjection.tearDown();

        ArgumentCaptor<ActivityLifecycleWatchdog> captor = ArgumentCaptor.forClass(ActivityLifecycleWatchdog.class);

        Emarsys.setup(mobileEngageConfig);

        verify(application, times(2)).registerActivityLifecycleCallbacks(captor.capture());
        ActivityLifecycleAction[] actions = CollectionTestUtils.getElementByType(captor.getAllValues(), ActivityLifecycleWatchdog.class).getActivityCreatedActions();

        assertEquals(1, CollectionTestUtils.numberOfElementsIn(actions, DeepLinkAction.class));
    }

    @Test
    public void testSetup_registers_currentActivityWatchDog() {
        Emarsys.setup(mobileEngageConfig);

        verify(application).registerActivityLifecycleCallbacks(currentActivityWatchdog);
    }

    @Test
    public void testSetup_setsInAppEventHandler_whenProvidedInConfig() {
        Emarsys.setup(configWithInAppEventHandler);

        verify(mockInApp).setEventHandler(inappEventHandler);
    }

    @Test
    public void testSetup_doesNotSetInAppEventHandler_whenMissingFromConfig() {
        Emarsys.setup(mobileEngageConfig);

        verifyZeroInteractions(mockInApp);
    }

    @Test
    public void testSetup_sendClientInfo() {
        when(mockClientStateStorage.get()).thenReturn(null);
        when(mockContactFieldValueStorage.get()).thenReturn(null);
        when(mockContactTokenStorage.get()).thenReturn(null);

        Emarsys.setup(mobileEngageConfig);

        verify(mockClientServiceInternal).trackDeviceInfo();
    }

    @Test
    public void testSetup_doNotSendClientInfo_whenHashIsUnChanged() {
        when(mockClientStateStorage.get()).thenReturn("asdfsaf");

        Emarsys.setup(mobileEngageConfig);

        verify(mockClientServiceInternal, never()).trackDeviceInfo();
    }

    @Test
    public void testSetup_doNotSendClientInfo_whenAnonymousContactIsNotNeededToSend() {
        when(mockClientStateStorage.get()).thenReturn(null);
        when(mockContactFieldValueStorage.get()).thenReturn("asdf");
        when(mockContactTokenStorage.get()).thenReturn("asdf");

        Emarsys.setup(mobileEngageConfig);

        verify(mockClientServiceInternal, never()).trackDeviceInfo();

    }

    @Test
    public void testSetup_sendAnonymousContact() {
        when(mockContactFieldValueStorage.get()).thenReturn(null);
        when(mockContactTokenStorage.get()).thenReturn(null);

        Emarsys.setup(mobileEngageConfig);

        verify(mockMobileEngageInternal).setContact(null, null);
    }

    @Test
    public void testSetup_sendDeviceInfoAndAnonymousContact_inOrder() {
        when(mockContactFieldValueStorage.get()).thenReturn(null);
        when(mockContactTokenStorage.get()).thenReturn(null);
        when(mockDeviceInfoHashStorage.get()).thenReturn(2345);

        Emarsys.setup(mobileEngageConfig);

        InOrder inOrder = inOrder(mockMobileEngageInternal, mockClientServiceInternal);
        inOrder.verify(mockClientServiceInternal).trackDeviceInfo();
        inOrder.verify(mockMobileEngageInternal).setContact(null, null);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testSetup_doNotSendAnonymousContact_whenContactFieldValueIsPresent() {
        Emarsys.setup(mobileEngageConfig);

        verify(mockMobileEngageInternal, never()).setContact(null, null);
    }

    @Test
    public void testSetup_doNotSendAnonymousContact_whenContactTokenIsPresent() {
        when(mockContactFieldValueStorage.get()).thenReturn(null);

        Emarsys.setup(mobileEngageConfig);

        verify(mockMobileEngageInternal, never()).setContact(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetContact_contactId_mustNotBeNull() {
        Emarsys.setContact(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetContactWithCompletionListener_contactId_mustNotBeNull() {
        Emarsys.setContact(null, completionListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetContactWithCompletionListener_completionListener_mustNotBeNull() {
        Emarsys.setContact(CONTACT_ID, null);
    }

    @Test
    public void testSetContact_delegatesTo_mobileEngageInternal() {

        Emarsys.setContact(CONTACT_ID);

        verify(mockMobileEngageInternal).setContact(CONTACT_ID, null);
    }

    @Test
    public void testSetContactWithCompletionListener_delegatesToMobileEngageInternal() {
        Emarsys.setContact(CONTACT_ID, completionListener);

        verify(mockMobileEngageInternal).setContact(CONTACT_ID, completionListener);
    }

    @Test
    public void testSetContact_delegatesTo_predictInternal() {
        Emarsys.setContact(CONTACT_ID);

        verify(mockPredictInternal).setContact(CONTACT_ID);
    }

    @Test
    public void testSetContactWithCompletionListener_delegatesTo_predictInternal() {
        Emarsys.setContact(CONTACT_ID, completionListener);

        verify(mockPredictInternal).setContact(CONTACT_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClearContactWithCompletionListener_completionListener_mustNotBeNull() {
        Emarsys.clearContact(null);
    }

    @Test
    public void testClearContact_delegatesTo_mobileEngageInternal() {
        Emarsys.clearContact();

        verify(mockMobileEngageInternal).clearContact(null);
    }

    @Test
    public void testClearContactWithCompletionListener_delegatesTo_mobileEngageInternal() {
        Emarsys.clearContact(completionListener);

        verify(mockMobileEngageInternal).clearContact(completionListener);
    }

    @Test
    public void testClearContact_delegatesTo_predictInternal() {
        Emarsys.clearContact();

        verify(mockPredictInternal).clearContact();
    }

    @Test
    public void testClearContactWithCompletionListener_delegatesTo_predictInternal() {
        Emarsys.clearContact(completionListener);

        verify(mockPredictInternal).clearContact();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackDeepLink_activity_mustNotBeNull() {
        Emarsys.trackDeepLink(null, mock(Intent.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackDeepLink_intent_mustNotBeNull() {
        Emarsys.trackDeepLink(mock(Activity.class), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackDeepLinkWithCompletionListener_activity_mustNotBeNull() {
        Emarsys.trackDeepLink(null, mock(Intent.class), completionListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackDeepLinkWithCompletionListener_intent_mustNotBeNull() {
        Emarsys.trackDeepLink(mock(Activity.class), null, completionListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackDeepLinkWithCompletionListener_completionListener_mustNotBeNull() {
        Emarsys.trackDeepLink(mock(Activity.class), mock(Intent.class), null);
    }

    @Test
    public void testTrackDeepLink_delegatesTo_deepLinkInternal() {
        Activity mockActivity = mock(Activity.class);
        Intent mockIntent = mock(Intent.class);

        Emarsys.trackDeepLink(mockActivity, mockIntent);

        verify(mockDeepLinkInternal).trackDeepLinkOpen(mockActivity, mockIntent, null);
    }

    @Test
    public void testTrackDeepLinkWithCompletionListener_delegatesTo_deepLinkInternal() {
        Activity mockActivity = mock(Activity.class);
        Intent mockIntent = mock(Intent.class);

        Emarsys.trackDeepLink(mockActivity, mockIntent, completionListener);

        verify(mockDeepLinkInternal).trackDeepLinkOpen(mockActivity, mockIntent, completionListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackCustomEvent_eventName_mustNotBeNull() {
        Emarsys.trackCustomEvent(null, new HashMap<String, String>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackCustomEventWithCompletionListener_eventName_mustNotBeNull() {
        Emarsys.trackCustomEvent(null, new HashMap<String, String>(), completionListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackCustomEventWithCompletionListener_completionListener_mustNotBeNull() {
        Emarsys.trackCustomEvent("eventName", new HashMap<String, String>(), null);
    }

    @Test
    public void testTrackCustomEvent_delegatesTo_mobileEngageInternal() {
        String eventName = "eventName";
        HashMap<String, String> eventAttributes = new HashMap<>();

        Emarsys.trackCustomEvent(eventName, eventAttributes);

        verify(mockEventServiceInternal).trackCustomEvent(eventName, eventAttributes, null);
    }

    @Test
    public void testTrackCustomEventWithCompletionListener_delegatesTo_mobileEngageInternal() {
        String eventName = "eventName";
        HashMap<String, String> eventAttributes = new HashMap<>();

        Emarsys.trackCustomEvent(eventName, eventAttributes, completionListener);

        verify(mockEventServiceInternal).trackCustomEvent(eventName, eventAttributes, completionListener);
    }

    @Test
    public void testPush_trackMessageOpen_delegatesTo_pushInstance() {
        Emarsys.setup(mobileEngageConfig);

        Intent mockIntent = mock(Intent.class);
        Emarsys.Push.trackMessageOpen(mockIntent);

        verify(mockPush).trackMessageOpen(mockIntent);
    }

    @Test
    public void testPush_trackMessageOpen_withCompletionListener_delegatesTo_pushInstance() {
        Emarsys.setup(mobileEngageConfig);

        Intent mockIntent = mock(Intent.class);
        CompletionListener mockCompletionListener = mock(CompletionListener.class);
        Emarsys.Push.trackMessageOpen(mockIntent, mockCompletionListener);

        verify(mockPush).trackMessageOpen(mockIntent, mockCompletionListener);
    }

    @Test
    public void testPush_setPushToken_delegatesTo_pushInstance() {
        Emarsys.setup(mobileEngageConfig);

        Emarsys.Push.setPushToken("pushToken");

        verify(mockPush).setPushToken("pushToken");
    }

    @Test
    public void testPush_setPushToken_withCompletionListener_delegatesTo_pushInstance() {
        Emarsys.setup(mobileEngageConfig);

        CompletionListener mockCompletionListener = mock(CompletionListener.class);
        Emarsys.Push.setPushToken("pushToken", mockCompletionListener);

        verify(mockPush).setPushToken("pushToken", mockCompletionListener);
    }

    @Test
    public void testPush_clearPushToken_delegatesTo_pushInstance() {
        Emarsys.setup(mobileEngageConfig);

        Emarsys.Push.clearPushToken();

        verify(mockPush).clearPushToken();
    }

    @Test
    public void testPush_clearPushToken_withCompletionListener_delegatesTo_pushInstance() {
        Emarsys.setup(mobileEngageConfig);

        CompletionListener mockCompletionListener = mock(CompletionListener.class);
        Emarsys.Push.clearPushToken(mockCompletionListener);

        verify(mockPush).clearPushToken(mockCompletionListener);
    }

    @Test
    public void testPredict_trackCart_delegatesTo_predictInstance() {
        Emarsys.setup(mobileEngageConfig);

        List<CartItem> cartItems = new ArrayList<>();

        Emarsys.Predict.trackCart(cartItems);

        verify(mockPredict).trackCart(cartItems);
    }

    @Test
    public void testPredict_trackPurchase_delegatesTo_predictInstance() {
        Emarsys.setup(mobileEngageConfig);
        List<CartItem> cartItems = new ArrayList<>();
        Emarsys.Predict.trackPurchase("orderId", cartItems);

        verify(mockPredict).trackPurchase("orderId", cartItems);
    }

    @Test
    public void testPredict_trackItemView_delegatesTo_predictInstance() {
        Emarsys.setup(mobileEngageConfig);

        Emarsys.Predict.trackItemView("itemId");

        verify(mockPredict).trackItemView("itemId");
    }

    @Test
    public void testPredict_trackCategoryView_delegatesTo_predictInstance() {
        Emarsys.setup(mobileEngageConfig);

        Emarsys.Predict.trackCategoryView("categoryPath");

        verify(mockPredict).trackCategoryView("categoryPath");
    }

    @Test
    public void testPredict_trackSearchTerm_delegatesTo_predictInstance() {
        Emarsys.setup(mobileEngageConfig);

        Emarsys.Predict.trackSearchTerm("searchTerm");

        verify(mockPredict).trackSearchTerm("searchTerm");
    }

    @Test
    public void testInApp_pause_delegatesTo_inAppInstance() {
        Emarsys.setup(mobileEngageConfig);

        Emarsys.InApp.pause();

        verify(mockInApp).pause();
    }

    @Test
    public void testInApp_resume_delegatesTo_inAppInstance() {
        Emarsys.setup(mobileEngageConfig);

        Emarsys.InApp.resume();

        verify(mockInApp).resume();
    }

    @Test
    public void testInApp_isPaused_delegatesTo_inAppInstance() {
        Emarsys.setup(mobileEngageConfig);

        Emarsys.InApp.isPaused();

        verify(mockInApp).isPaused();
    }

    @Test
    public void testInApp_setEventHandler_delegatesTo_inAppInstance() {
        Emarsys.setup(mobileEngageConfig);

        EventHandler mockEventHandler = mock(EventHandler.class);

        Emarsys.InApp.setEventHandler(mockEventHandler);

        verify(mockInApp).setEventHandler(mockEventHandler);
    }

    @Test
    public void testInbox_fetchNotification_delegatesTo_inboxInstance() {
        Emarsys.setup(mobileEngageConfig);

        ResultListener mockResultListener = mock(ResultListener.class);

        Emarsys.Inbox.fetchNotifications(mockResultListener);

        verify(mockInbox).fetchNotifications(mockResultListener);
    }

    @Test
    public void testInbox_trackNotificationOpen_delegatesTo_inboxInstance() {
        Emarsys.setup(mobileEngageConfig);

        Notification mockNotification = mock(Notification.class);

        Emarsys.Inbox.trackNotificationOpen(mockNotification);

        verify(mockInbox).trackNotificationOpen(mockNotification);
    }

    @Test
    public void testInbox_trackNotificationOpen_withCompletionListener_delegatesTo_inboxInstance() {
        Emarsys.setup(mobileEngageConfig);

        Notification mockNotification = mock(Notification.class);
        CompletionListener mockCompletionListener = mock(CompletionListener.class);

        Emarsys.Inbox.trackNotificationOpen(mockNotification, mockCompletionListener);

        verify(mockInbox).trackNotificationOpen(mockNotification, mockCompletionListener);
    }

    @Test
    public void testInbox_resetBadgeCount_delegatesTo_inboxInstance() {
        Emarsys.setup(mobileEngageConfig);


        Emarsys.Inbox.resetBadgeCount();

        verify(mockInbox).resetBadgeCount();
    }

    @Test
    public void testInbox_resetBadgeCount_withCompletionListener_delegatesTo_inboxInstance() {
        Emarsys.setup(mobileEngageConfig);

        CompletionListener mockCompletionListener = mock(CompletionListener.class);

        Emarsys.Inbox.resetBadgeCount(mockCompletionListener);

        verify(mockInbox).resetBadgeCount(mockCompletionListener);
    }

    private EmarsysConfig.Builder createConfig(FlipperFeature... experimentalFeatures) {
        EmarsysConfig.Builder builder = new EmarsysConfig.Builder()
                .application(application)
                .contactFieldId(CONTACT_FIELD_ID)
                .enableExperimentalFeatures(experimentalFeatures);
        return builder;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void cacheMocks() {
        mock(Application.class);
        mock(Activity.class);
        mock(Intent.class);
    }
}

