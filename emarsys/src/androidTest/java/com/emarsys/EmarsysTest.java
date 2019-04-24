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
import com.emarsys.core.api.result.Try;
import com.emarsys.core.concurrency.CoreSdkHandler;
import com.emarsys.core.database.CoreSQLiteDatabase;
import com.emarsys.core.database.trigger.TriggerEvent;
import com.emarsys.core.database.trigger.TriggerType;
import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.device.LanguageProvider;
import com.emarsys.core.di.DependencyContainer;
import com.emarsys.core.di.DependencyInjection;
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
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.RefreshTokenInternal;
import com.emarsys.mobileengage.api.EventHandler;
import com.emarsys.mobileengage.api.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;
import com.emarsys.mobileengage.deeplink.DeepLinkAction;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.iam.InAppInternal;
import com.emarsys.mobileengage.iam.InAppStartAction;
import com.emarsys.mobileengage.iam.model.requestRepositoryProxy.RequestRepositoryProxy;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.mobileengage.inbox.InboxInternal_V1;
import com.emarsys.mobileengage.inbox.InboxInternal_V2;
import com.emarsys.mobileengage.responsehandler.ClientInfoResponseHandler;
import com.emarsys.mobileengage.responsehandler.InAppCleanUpResponseHandler;
import com.emarsys.mobileengage.responsehandler.InAppMessageResponseHandler;
import com.emarsys.mobileengage.responsehandler.MeIdResponseHandler;
import com.emarsys.mobileengage.responsehandler.MobileEngageClientStateResponseHandler;
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler;
import com.emarsys.predict.PredictInternal;
import com.emarsys.predict.api.model.CartItem;
import com.emarsys.predict.response.VisitorIdResponseHandler;
import com.emarsys.testUtil.CollectionTestUtils;
import com.emarsys.testUtil.ExperimentalTestUtils;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.RandomTestUtils;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertTrue;
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
    private static final String APPLICATION_PASSWORD = "secret";
    private static final int CONTACT_FIELD_ID = 3;
    private static final String MERCHANT_ID = "merchantId";
    private static final String SDK_VERSION = "sdkVersion";
    private static final String CONTACT_ID = "CONTACT_ID";

    private CoreSdkHandler mockCoreSdkHandler;
    private ActivityLifecycleWatchdog activityLifecycleWatchdog;
    private CurrentActivityWatchdog currentActivityWatchdog;
    private MobileEngageInternal mockMobileEngageInternal;
    private PredictInternal mockPredictInternal;
    private InboxInternal mockInboxInternal;
    private InAppInternal mockInAppInternal;
    private DeepLinkInternal mockDeepLinkInternal;
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
    private EmarsysConfig configWithInAppEventHandler;
    private EmarsysConfig userCentricInboxConfig;
    private RefreshTokenInternal mockRefreshTokenInternal;
    private Storage<Integer> mockDeviceInfoHashStorage;
    private Storage<String> mockContactFieldValueStorage;
    private Storage<String> mockContactTokenStorage;
    private Storage<String> mockClientStateStorage;
    private ResponseHandlersProcessor mockResponseHandlersProcessor;
    private DeviceInfo deviceInfo;
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
        mockInboxInternal = mock(InboxInternal.class);
        mockInAppInternal = mock(InAppInternal.class);
        mockDeepLinkInternal = mock(DeepLinkInternal.class);
        mockPredictInternal = mock(PredictInternal.class);
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

        baseConfig = createConfig(false);
        configWithInAppEventHandler = createConfig(true);
        userCentricInboxConfig = createConfig(false, MobileEngageFeature.USER_CENTRIC_INBOX);

        HardwareIdProvider hardwareIdProvider = mock(HardwareIdProvider.class);
        deviceInfo = new DeviceInfo(application, hardwareIdProvider, mockVersionProvider, mockLanguageProvider);

        when(mockDeviceInfoHashStorage.get()).thenReturn(deviceInfo.getHash());
        when(mockVersionProvider.provideSdkVersion()).thenReturn(SDK_VERSION);
        when(mockContactFieldValueStorage.get()).thenReturn("test@test.com");
        when(mockContactTokenStorage.get()).thenReturn("asdfasfaghdsgf");

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
                mockInboxInternal,
                mockInAppInternal,
                mockDeepLinkInternal,
                null,
                null,
                null,
                null,
                mockPredictInternal,
                mockPredictShardTrigger,
                runnerProxy,
                logger,
                mockRefreshTokenInternal,
                mockDeviceInfoHashStorage,
                mockContactFieldValueStorage,
                mockContactTokenStorage,
                mockClientStateStorage,
                mockResponseHandlersProcessor
        ));
    }

    @After
    public void tearDown() {
        Looper looper = DependencyInjection.getContainer().getCoreSdkHandler().getLooper();
        if (looper != null) {
            looper.quit();
        }
        DependencyInjection.tearDown();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetup_config_mustNotBeNull() {
        Emarsys.setup(null);
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

        Emarsys.setup(baseConfig);

        assertNotNull(DependencyInjection.<EmarysDependencyContainer>getContainer().getMobileEngageInternal());
    }

    @Test
    public void testSetup_initializes_inboxInstance_V1() {
        DependencyInjection.tearDown();
        ExperimentalTestUtils.resetExperimentalFeatures();

        Emarsys.setup(baseConfig);

        InboxInternal inboxInternal = DependencyInjection.<EmarysDependencyContainer>getContainer().getInboxInternal();
        assertNotNull(inboxInternal);
        assertEquals(InboxInternal_V1.class, inboxInternal.getClass());
    }

    @Test
    public void testSetup_initializes_inboxInstance_V2() {
        DependencyInjection.tearDown();
        ExperimentalTestUtils.resetExperimentalFeatures();

        Emarsys.setup(userCentricInboxConfig);

        InboxInternal inboxInternal = DependencyInjection.<EmarysDependencyContainer>getContainer().getInboxInternal();
        assertNotNull(inboxInternal);
        assertEquals(InboxInternal_V2.class, inboxInternal.getClass());
    }

    @Test
    public void testSetup_initializes_deepLinkInstance() {
        DependencyInjection.tearDown();
        ExperimentalTestUtils.resetExperimentalFeatures();

        Emarsys.setup(baseConfig);

        assertNotNull(DependencyInjection.<EmarysDependencyContainer>getContainer().getDeepLinkInternal());
    }

    @Test
    public void testSetup_initializes_predictInstance() {
        DependencyInjection.tearDown();

        Emarsys.setup(baseConfig);

        assertNotNull(DependencyInjection.<EmarysDependencyContainer>getContainer().getPredictInternal());
    }

    @Test
    public void testSetup_initializesRequestManager_withRequestModelRepositoryProxy() {
        DependencyInjection.tearDown();
        ExperimentalTestUtils.resetExperimentalFeatures();

        Emarsys.setup(baseConfig);

        RequestManager requestManager = ReflectionTestUtils.getInstanceField(
                DependencyInjection.<DefaultEmarsysDependencyContainer>getContainer(),
                "requestManager");
        Object repository = ReflectionTestUtils.getInstanceField(
                requestManager,
                "requestRepository");
        assertEquals(RequestRepositoryProxy.class, repository.getClass());
    }

    @Test
    public void testSetup_initializesRequestManager_withRequestModelRepositoryProxy_withInboxFlipper() {
        DependencyInjection.tearDown();
        ExperimentalTestUtils.resetExperimentalFeatures();

        Emarsys.setup(userCentricInboxConfig);

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
        ExperimentalTestUtils.resetExperimentalFeatures();

        Emarsys.setup(baseConfig);

        ResponseHandlersProcessor responseHandlersProcessor = DependencyInjection
                .<DefaultEmarsysDependencyContainer>getContainer()
                .getResponseHandlersProcessor();

        assertNotNull(responseHandlersProcessor);
        assertEquals(8, responseHandlersProcessor.getResponseHandlers().size());
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), VisitorIdResponseHandler.class));
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), MeIdResponseHandler.class));
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), InAppMessageResponseHandler.class));
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), InAppCleanUpResponseHandler.class));
        assertEquals(2, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), MobileEngageTokenResponseHandler.class));
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), MobileEngageClientStateResponseHandler.class));
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), ClientInfoResponseHandler.class));

    }

    @Test
    public void testSetup_initializesCoreCompletionHandler_whenUserCentricInboxIsOn() {
        DependencyInjection.tearDown();
        ExperimentalTestUtils.resetExperimentalFeatures();

        Emarsys.setup(userCentricInboxConfig);

        ResponseHandlersProcessor responseHandlersProcessor = DependencyInjection
                .<DefaultEmarsysDependencyContainer>getContainer()
                .getResponseHandlersProcessor();

        assertNotNull(responseHandlersProcessor);
        assertEquals(8, responseHandlersProcessor.getResponseHandlers().size());
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), VisitorIdResponseHandler.class));
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), MeIdResponseHandler.class));
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), InAppMessageResponseHandler.class));
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), InAppCleanUpResponseHandler.class));
        assertEquals(2, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), MobileEngageTokenResponseHandler.class));
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), MobileEngageClientStateResponseHandler.class));
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(responseHandlersProcessor.getResponseHandlers(), ClientInfoResponseHandler.class));
    }

    @Test
    public void testSetup_registersPredictTrigger() {
        Emarsys.setup(baseConfig);

        verify(mockCoreDatabase).registerTrigger("shard", TriggerType.AFTER, TriggerEvent.INSERT, mockPredictShardTrigger);
    }

    @Test
    public void testSetup_registersLogTrigger() {
        Emarsys.setup(baseConfig);

        verify(mockCoreDatabase).registerTrigger("shard", TriggerType.AFTER, TriggerEvent.INSERT, mockLogShardTrigger);
    }

    @Test
    public void testSetup_registers_activityLifecycleWatchdog() {
        Emarsys.setup(baseConfig);

        verify(application).registerActivityLifecycleCallbacks(activityLifecycleWatchdog);
    }

    @Test
    public void testSetup_registers_activityLifecycleWatchdog_withInAppStartAction() {
        DependencyInjection.tearDown();

        ArgumentCaptor<ActivityLifecycleWatchdog> captor = ArgumentCaptor.forClass(ActivityLifecycleWatchdog.class);

        Emarsys.setup(baseConfig);

        verify(application, times(2)).registerActivityLifecycleCallbacks(captor.capture());
        ActivityLifecycleAction[] actions = CollectionTestUtils.getElementByType(captor.getAllValues(), ActivityLifecycleWatchdog.class).getApplicationStartActions();

        assertEquals(1, CollectionTestUtils.numberOfElementsIn(actions, InAppStartAction.class));
    }

    @Test
    public void testSetup_registers_activityLifecycleWatchdog_withDeepLinkAction() {
        DependencyInjection.tearDown();

        ArgumentCaptor<ActivityLifecycleWatchdog> captor = ArgumentCaptor.forClass(ActivityLifecycleWatchdog.class);

        Emarsys.setup(baseConfig);

        verify(application, times(2)).registerActivityLifecycleCallbacks(captor.capture());
        ActivityLifecycleAction[] actions = CollectionTestUtils.getElementByType(captor.getAllValues(), ActivityLifecycleWatchdog.class).getActivityCreatedActions();

        assertEquals(1, CollectionTestUtils.numberOfElementsIn(actions, DeepLinkAction.class));
    }

    @Test
    public void testSetup_registers_currentActivityWatchDog() {
        Emarsys.setup(baseConfig);

        verify(application).registerActivityLifecycleCallbacks(currentActivityWatchdog);
    }

    @Test
    public void testSetup_setsInAppEventHandler_whenProvidedInConfig() {
        Emarsys.setup(configWithInAppEventHandler);

        verify(mockInAppInternal).setEventHandler(inappEventHandler);
    }

    @Test
    public void testSetup_doesNotSetInAppEventHandler_whenMissingFromConfig() {
        Emarsys.setup(baseConfig);

        verifyZeroInteractions(mockInAppInternal);
    }

    @Test
    public void testSetup_sendClientInfo() {
        when(mockClientStateStorage.get()).thenReturn(null);
        when(mockContactFieldValueStorage.get()).thenReturn(null);
        when(mockContactTokenStorage.get()).thenReturn(null);

        Emarsys.setup(baseConfig);

        verify(mockMobileEngageInternal).trackDeviceInfo();
    }

    @Test
    public void testSetup_doNotSendClientInfo_whenHashIsUnChanged() {
        when(mockClientStateStorage.get()).thenReturn("asdfsaf");

        Emarsys.setup(baseConfig);

        verify(mockMobileEngageInternal, never()).trackDeviceInfo();
    }

    @Test
    public void testSetup_doNotSendClientInfo_whenAnonymousContactIsNotNeededToSend() {
        when(mockClientStateStorage.get()).thenReturn(null);
        when(mockContactFieldValueStorage.get()).thenReturn("asdf");
        when(mockContactTokenStorage.get()).thenReturn("asdf");

        Emarsys.setup(baseConfig);

        verify(mockMobileEngageInternal, never()).trackDeviceInfo();

    }

    @Test
    public void testSetup_sendAnonymousContact() {
        when(mockContactFieldValueStorage.get()).thenReturn(null);
        when(mockContactTokenStorage.get()).thenReturn(null);

        Emarsys.setup(baseConfig);

        verify(mockMobileEngageInternal).setContact(null, null);
    }

    @Test
    public void testSetup_sendDeviceInfoAndAnonymousContact_inOrder() {
        when(mockContactFieldValueStorage.get()).thenReturn(null);
        when(mockContactTokenStorage.get()).thenReturn(null);
        when(mockDeviceInfoHashStorage.get()).thenReturn(2345);

        Emarsys.setup(baseConfig);

        InOrder inOrder = inOrder(mockMobileEngageInternal);
        inOrder.verify(mockMobileEngageInternal).trackDeviceInfo();
        inOrder.verify(mockMobileEngageInternal).setContact(null, null);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testSetup_doNotSendAnonymousContact_whenContactFieldValueIsPresent() {
        Emarsys.setup(baseConfig);

        verify(mockMobileEngageInternal, never()).setContact(null, null);
    }

    @Test
    public void testSetup_doNotSendAnonymousContact_whenContactTokenIsPresent() {
        when(mockContactFieldValueStorage.get()).thenReturn(null);

        Emarsys.setup(baseConfig);

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

        verify(mockMobileEngageInternal).trackCustomEvent(eventName, eventAttributes, null);
    }

    @Test
    public void testTrackCustomEventWithCompletionListener_delegatesTo_mobileEngageInternal() {
        String eventName = "eventName";
        HashMap<String, String> eventAttributes = new HashMap<>();

        Emarsys.trackCustomEvent(eventName, eventAttributes, completionListener);

        verify(mockMobileEngageInternal).trackCustomEvent(eventName, eventAttributes, completionListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPush_trackMessageOpen_intent_mustNotBeNull() {
        Emarsys.Push.trackMessageOpen(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPush_trackMessageOpenWithCompletionListener_intent_mustNotBeNull() {
        Emarsys.Push.trackMessageOpen(null, completionListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPush_trackMessageOpenWithCompletionListener_completionListener_mustNotBeNull() {
        Emarsys.Push.trackMessageOpen(mock(Intent.class), null);
    }

    @Test
    public void testPush_trackMessageOpen_delegatesTo_mobileEngageInternal() {
        Intent intent = mock(Intent.class);

        Emarsys.Push.trackMessageOpen(intent);

        verify(mockMobileEngageInternal).trackMessageOpen(intent, null);
    }

    @Test
    public void testPush_trackMessageOpenWithCompletionListener_delegatesTo_mobileEngageInternal() {
        Intent intent = mock(Intent.class);

        Emarsys.Push.trackMessageOpen(intent, completionListener);

        verify(mockMobileEngageInternal).trackMessageOpen(intent, completionListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPush_setPushToken_token_mustNotBeNull() {
        Emarsys.Push.setPushToken(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPush_setPushTokenWithCompletionListener_token_mustNotBeNull() {
        Emarsys.Push.setPushToken(null, completionListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPush_setPushTokenWithCompletionListener_completionListener_mustNotBeNull() {
        Emarsys.Push.setPushToken("pushToken", null);
    }

    @Test
    public void testPush_setPushToken_delegatesTo_mobileEngageInternal() {
        String pushToken = "pushToken";

        Emarsys.Push.setPushToken(pushToken);

        verify(mockMobileEngageInternal).setPushToken(pushToken, null);
    }

    @Test
    public void testPush_setPushToken_completionListener_delegatesTo_mobileEngageInternal() {
        String pushToken = "pushToken";

        Emarsys.Push.setPushToken(pushToken, completionListener);

        verify(mockMobileEngageInternal).setPushToken(pushToken, completionListener);
    }

    @Test
    public void testPush_removePushToken_delegatesTo_mobileEngageInternal() {
        Emarsys.Push.removePushToken();

        verify(mockMobileEngageInternal).removePushToken(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPush_removePushTokenWithCompletionListener_completionListener_mustNotBeNull() {
        Emarsys.Push.removePushToken(null);
    }

    @Test
    public void testPush_removePushTokenWithCompletionListener_delegatesTo_mobileEngageInternal() {
        Emarsys.Push.removePushToken(completionListener);

        verify(mockMobileEngageInternal).removePushToken(completionListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_trackCart_items_mustNotBeNull() {
        Emarsys.Predict.trackCart(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_trackCart_itemElements_mustNotBeNull() {
        Emarsys.Predict.trackCart(Arrays.asList(
                mock(CartItem.class),
                null,
                mock(CartItem.class)));
    }

    @Test
    public void testPredict_trackCart_delegatesTo_predictInternal() {
        List<CartItem> itemList = Arrays.asList(
                createItem("itemId0", 200.0, 100.0),
                createItem("itemId1", 201.0, 101.0),
                createItem("itemId2", 202.0, 102.0));

        Emarsys.Predict.trackCart(itemList);

        verify(mockPredictInternal).trackCart(itemList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_trackPurchase_orderIdMustNotBeNull() {
        Emarsys.Predict.trackPurchase(null, new ArrayList<CartItem>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_trackPurchase_itemsMustNotBeNull() {
        Emarsys.Predict.trackPurchase("id", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_trackPurchase_itemElements_mustNotBeNull() {
        Emarsys.Predict.trackPurchase("id", Arrays.asList(
                mock(CartItem.class),
                null,
                mock(CartItem.class)
        ));
    }

    @Test
    public void testPredict_trackPurchase_delegatesTo_predictInternal() {
        String orderId = "id";

        List<CartItem> itemList = Arrays.asList(
                createItem("itemId0", 200.0, 100.0),
                createItem("itemId1", 201.0, 101.0),
                createItem("itemId2", 202.0, 102.0));

        Emarsys.Predict.trackPurchase(orderId, itemList);

        verify(mockPredictInternal).trackPurchase(orderId, itemList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_testTrackItemView_itemViewId_mustNotBeNull() {
        Emarsys.Predict.trackItemView(null);
    }

    @Test
    public void testPredict_trackItemView_delegatesTo_predictInternal() {
        String itemId = RandomTestUtils.randomString();

        Emarsys.Predict.trackItemView(itemId);

        verify(mockPredictInternal).trackItemView(itemId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_testTrackCategoryView_categoryPath_mustNotBeNull() {
        Emarsys.Predict.trackCategoryView(null);
    }

    @Test
    public void testPredict_trackCategoryView_delegatesTo_predictInternal() {
        String categoryPath = RandomTestUtils.randomString();

        Emarsys.Predict.trackCategoryView(categoryPath);

        verify(mockPredictInternal).trackCategoryView(categoryPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_trackSearchTerm_searchTerm_mustNotBeNull() {
        Emarsys.Predict.trackSearchTerm(null);
    }

    @Test
    public void testPredict_trackSearchTerm_delegatesTo_predictInternal() {
        String searchTerm = RandomTestUtils.randomString();

        Emarsys.Predict.trackSearchTerm(searchTerm);

        verify(mockPredictInternal).trackSearchTerm(searchTerm);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInbox_fetchNotifications_resultListener_mustNotBeNull() {
        Emarsys.Inbox.fetchNotifications(null);
    }

    @Test
    public void testInbox_fetchNotifications_delegatesTo_inboxInternal() {
        ResultListener<Try<NotificationInboxStatus>> resultListener = new ResultListener<Try<NotificationInboxStatus>>() {
            @Override
            public void onResult(Try<NotificationInboxStatus> result) {
            }
        };

        Emarsys.Inbox.fetchNotifications(resultListener);
        verify(mockInboxInternal).fetchNotifications(resultListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInbox_trackNotificationOpen_notification_mustNotBeNull() {
        Emarsys.Inbox.trackNotificationOpen(null);
    }

    @Test
    public void testInbox_trackNotificationOpen_delegatesTo_inboxInternal() {
        Notification notification = mock(Notification.class);

        Emarsys.Inbox.trackNotificationOpen(notification);
        verify(mockInboxInternal).trackNotificationOpen(notification, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInbox_trackNotificationOpen_notification_resultListener_notification_mustNotBeNull() {
        Emarsys.Inbox.trackNotificationOpen(null, mock(CompletionListener.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInbox_trackNotificationOpen_notification_resultListener_resultListener_mustNotBeNull() {
        Emarsys.Inbox.trackNotificationOpen(mock(Notification.class), null);
    }

    @Test
    public void testInbox_trackNotificationOpen_notification_resultListener_delegatesTo_inboxInternal() {
        Notification notification = mock(Notification.class);
        CompletionListener resultListener = mock(CompletionListener.class);

        Emarsys.Inbox.trackNotificationOpen(notification, resultListener);

        verify(mockInboxInternal).trackNotificationOpen(notification, resultListener);
    }

    @Test
    public void testInbox_resetBadgeCount_delegatesTo_inboxInternal() {
        Emarsys.Inbox.resetBadgeCount();

        verify(mockInboxInternal).resetBadgeCount(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInbox_resetBadgeCount_withCompletionListener_resultListener_mustNotBeNull() {
        Emarsys.Inbox.resetBadgeCount(null);
    }

    @Test
    public void testInbox_resetBadgeCount_withCompletionListener_delegatesTo_inboxInternal() {
        CompletionListener mockResultListener = mock(CompletionListener.class);

        Emarsys.Inbox.resetBadgeCount(mockResultListener);

        verify(mockInboxInternal).resetBadgeCount(mockResultListener);
    }

    @Test
    public void testInbox_purgeNotificationCache_delegatesTo_inboxInternal() {
        Emarsys.Inbox.purgeNotificationCache();

        verify(mockInboxInternal).purgeNotificationCache();
    }

    @Test
    public void testInApp_pause_delegatesToInternal() {
        Emarsys.InApp.pause();

        verify(mockInAppInternal).pause();
    }

    @Test
    public void testInApp_resume_delegatesToInternal() {
        Emarsys.InApp.resume();

        verify(mockInAppInternal).resume();
    }

    @Test
    public void testInApp_isPaused_delegatesToInternal() {
        when(mockInAppInternal.isPaused()).thenReturn(true);

        boolean result = Emarsys.InApp.isPaused();

        verify(mockInAppInternal).isPaused();
        assertTrue(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInApp_setEventHandler_eventHandler_mustNotBeNull() {
        Emarsys.InApp.setEventHandler(null);
    }

    @Test
    public void testInApp_setEventHandler_delegatesToInternal() {
        EventHandler eventHandler = mock(EventHandler.class);

        Emarsys.InApp.setEventHandler(eventHandler);

        verify(mockInAppInternal).setEventHandler(eventHandler);
    }

    private CartItem createItem(final String id, final double price, final double quantity) {
        return new CartItem() {
            @Override
            public String getItemId() {
                return id;
            }

            @Override
            public double getPrice() {
                return price;
            }

            @Override
            public double getQuantity() {
                return quantity;
            }
        };
    }

    private EmarsysConfig createConfig(boolean withInApp, FlipperFeature... experimentalFeatures) {
        EmarsysConfig.Builder builder = new EmarsysConfig.Builder()
                .application(application)
                .mobileEngageCredentials(APPLICATION_CODE, APPLICATION_PASSWORD)
                .predictMerchantId(MERCHANT_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .enableExperimentalFeatures(experimentalFeatures);
        if (withInApp) {
            builder.inAppEventHandler(inappEventHandler);
        }
        return builder.build();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void cacheMocks() {
        mock(Application.class);
        mock(Activity.class);
        mock(Intent.class);
    }
}
