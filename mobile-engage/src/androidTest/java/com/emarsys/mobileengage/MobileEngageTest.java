package com.emarsys.mobileengage;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.activity.ActivityLifecycleAction;
import com.emarsys.core.activity.ActivityLifecycleWatchdog;
import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.core.request.RequestIdProvider;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModelRepository;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.deeplink.DeepLinkAction;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.di.DefaultDependencyContainer;
import com.emarsys.mobileengage.di.DependencyContainer;
import com.emarsys.mobileengage.di.DependencyInjection;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.experimental.FlipperFeature;
import com.emarsys.mobileengage.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.fake.FakeRequestManager;
import com.emarsys.mobileengage.fake.FakeStatusListener;
import com.emarsys.mobileengage.iam.InAppStartAction;
import com.emarsys.mobileengage.iam.model.requestRepositoryProxy.RequestRepositoryProxy;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.mobileengage.inbox.InboxInternal_V1;
import com.emarsys.mobileengage.inbox.InboxInternal_V2;
import com.emarsys.mobileengage.inbox.InboxResultListener;
import com.emarsys.mobileengage.inbox.ResetBadgeCountResultListener;
import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.responsehandler.AbstractResponseHandler;
import com.emarsys.mobileengage.responsehandler.InAppCleanUpResponseHandler;
import com.emarsys.mobileengage.responsehandler.InAppMessageResponseHandler;
import com.emarsys.mobileengage.responsehandler.MeIdResponseHandler;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.CollectionTestUtils;
import com.emarsys.mobileengage.testUtil.CurrentActivityWatchdogTestUtils;
import com.emarsys.mobileengage.testUtil.DatabaseTestUtils;
import com.emarsys.mobileengage.testUtil.ExperimentalTestUtils;
import com.emarsys.mobileengage.testUtil.ReflectionTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.emarsys.mobileengage.fake.FakeRequestManager.ResponseType.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MobileEngageTest {

    static {
        mock(Application.class);
        mock(Activity.class);
        mock(Intent.class);
    }

    private static final String appID = "56789876";
    private static final String appSecret = "secret";

    private MobileEngageCoreCompletionHandler coreCompletionHandler;
    private MobileEngageInternal mobileEngageInternal;
    private InboxInternal inboxInternal;
    private DeepLinkInternal deepLinkInternal;
    private Application application;
    private MobileEngageConfig baseConfig;
    private MobileEngageConfig userCentricConfig;
    private MobileEngageConfig inAppConfig;
    private MobileEngageConfig fullConfig;
    private RequestContext requestContext;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() throws Exception {
        DatabaseTestUtils.deleteMobileEngageDatabase();
        DatabaseTestUtils.deleteCoreDatabase();
        DependencyInjection.tearDown();

        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        coreCompletionHandler = mock(MobileEngageCoreCompletionHandler.class);
        mobileEngageInternal = mock(MobileEngageInternal.class);
        inboxInternal = mock(InboxInternal.class);
        deepLinkInternal = mock(DeepLinkInternal.class);
        baseConfig = createConfigWithFlippers();
        userCentricConfig = createConfigWithFlippers(MobileEngageFeature.USER_CENTRIC_INBOX);
        inAppConfig = createConfigWithFlippers(MobileEngageFeature.IN_APP_MESSAGING);
        fullConfig = createConfigWithFlippers(MobileEngageFeature.IN_APP_MESSAGING, MobileEngageFeature.USER_CENTRIC_INBOX);
        requestContext = mock(RequestContext.class);

        MobileEngage.inboxInstance = inboxInternal;
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.deepLinkInstance = deepLinkInternal;
        MobileEngage.completionHandler = coreCompletionHandler;
        MobileEngage.requestContext = requestContext;

        CurrentActivityWatchdogTestUtils.resetCurrentActivityWatchdog();
    }

    @After
    public void tearDown() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();
        MobileEngage.InApp.setPaused(false);
        MobileEngage.coreSdkHandler.getLooper().quit();
        DependencyInjection.tearDown();
    }

    @Test
    public void testSetup_initializesDependencyInjectionContainer() throws Exception {
        DependencyContainer container = ReflectionTestUtils.getStaticField(DependencyInjection.class, "container");
        Assert.assertNull(container);

        MobileEngage.setup(baseConfig);

        container = ReflectionTestUtils.getStaticField(DependencyInjection.class, "container");
        Assert.assertNotNull(container);
        Assert.assertEquals(DefaultDependencyContainer.class, container.getClass());
    }

    @Test
    public void testSetup_initializesMobileEngageInstance() {
        MobileEngage.instance = null;
        MobileEngage.setup(baseConfig);

        assertNotNull(MobileEngage.instance);
    }

    @Test
    public void testSetup_initializesCoreCompletionHandler_withMeIdResponseHandler_whenInAppIsOn() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();
        MobileEngage.completionHandler = null;
        MobileEngage.setup(inAppConfig);

        MobileEngageCoreCompletionHandler coreCompletionHandler = MobileEngage.completionHandler;
        assertNotNull(coreCompletionHandler);
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(coreCompletionHandler.responseHandlers, MeIdResponseHandler.class));
    }

    @Test
    public void testSetup_initializesCoreCompletionHandler_withMeIdResponseHandler_whenUserCentricInboxIsOn() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();
        MobileEngage.completionHandler = null;
        MobileEngage.setup(userCentricConfig);

        MobileEngageCoreCompletionHandler coreCompletionHandler = MobileEngage.completionHandler;
        assertNotNull(coreCompletionHandler);
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(coreCompletionHandler.responseHandlers, MeIdResponseHandler.class));
        assertEquals(0, CollectionTestUtils.numberOfElementsIn(coreCompletionHandler.responseHandlers, InAppMessageResponseHandler.class));
        assertEquals(0, CollectionTestUtils.numberOfElementsIn(coreCompletionHandler.responseHandlers, InAppCleanUpResponseHandler.class));
    }

    @Test
    public void testSetup_initializesCoreCompletionHandler_withMeIdResponseHandler_onlyOnce_WhenBothUserCentricInboxAndInAppIsOn() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();
        MobileEngage.completionHandler = null;
        MobileEngage.setup(fullConfig);

        MobileEngageCoreCompletionHandler coreCompletionHandler = MobileEngage.completionHandler;
        assertNotNull(coreCompletionHandler);
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(coreCompletionHandler.responseHandlers, MeIdResponseHandler.class));
    }

    @Test
    public void testSetup_initializesInAppPaused_withFalse() {
        MobileEngage.InApp.setPaused(true);

        MobileEngage.setup(baseConfig);

        assertFalse(MobileEngage.InApp.isPaused());
    }

    @Test
    public void testSetup_whenInAppMessagingFlipperIsOff_initializesCoreCompletionHandler_withoutMeIdResponseHandler() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        MobileEngage.completionHandler = null;
        MobileEngage.setup(baseConfig);

        MobileEngageCoreCompletionHandler coreCompletionHandler = MobileEngage.completionHandler;
        assertNotNull(coreCompletionHandler);
        assertEquals(0, CollectionTestUtils.numberOfElementsIn(coreCompletionHandler.responseHandlers, MeIdResponseHandler.class));
    }

    @Test
    public void testSetup_initializesCoreCompletionHandler_withInAppMessageResponseHandler() {
        MobileEngage.completionHandler = null;
        MobileEngage.setup(inAppConfig);

        MobileEngageCoreCompletionHandler coreCompletionHandler = MobileEngage.completionHandler;
        assertNotNull(coreCompletionHandler);
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(coreCompletionHandler.responseHandlers, InAppMessageResponseHandler.class));
    }

    @Test
    public void testSetup_whenInAppMessagingFlipperIsOff_initializesCoreCompletionHandler_withoutInAppMessageResponseHandler() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        MobileEngage.completionHandler = null;
        MobileEngage.setup(baseConfig);

        MobileEngageCoreCompletionHandler coreCompletionHandler = MobileEngage.completionHandler;
        assertNotNull(coreCompletionHandler);
        assertEquals(0, CollectionTestUtils.numberOfElementsIn(coreCompletionHandler.responseHandlers, InAppMessageResponseHandler.class));
    }

    @Test
    public void testSetup_initializesCoreCompletionHandler_withInAppCleanUpResponseHandler() {
        MobileEngage.completionHandler = null;
        MobileEngage.setup(inAppConfig);

        MobileEngageCoreCompletionHandler coreCompletionHandler = MobileEngage.completionHandler;
        assertNotNull(coreCompletionHandler);
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(coreCompletionHandler.responseHandlers, InAppCleanUpResponseHandler.class));
    }

    @Test
    public void testSetup_whenInAppMessagingFlipperIsOff_initializesCoreCompletionHandler_withoutInAppCleanUpResponseHandler() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        MobileEngage.completionHandler = null;
        MobileEngage.setup(baseConfig);

        MobileEngageCoreCompletionHandler coreCompletionHandler = MobileEngage.completionHandler;
        assertNotNull(coreCompletionHandler);
        assertEquals(0, CollectionTestUtils.numberOfElementsIn(coreCompletionHandler.responseHandlers, InAppCleanUpResponseHandler.class));
    }

    @Test
    public void testSetup_initializesRequestManager_withRequestModelProxy_whenInAppFlipperIsOn() throws Exception {
        MobileEngage.setup(inAppConfig);

        Field repositoryField = RequestManager.class.getDeclaredField("requestRepository");
        repositoryField.setAccessible(true);
        Object repository = repositoryField.get(MobileEngage.instance.manager);
        assertEquals(RequestRepositoryProxy.class, repository.getClass());
    }

    @Test
    public void testSetup_initializesRequestManager_withRequestModelProxy_whenUserCentricInboxFlipperIsOn() throws Exception {
        MobileEngage.setup(userCentricConfig);

        Field repositoryField = RequestManager.class.getDeclaredField("requestRepository");
        repositoryField.setAccessible(true);
        Object repository = repositoryField.get(MobileEngage.instance.manager);
        assertEquals(RequestRepositoryProxy.class, repository.getClass());
    }

    @Test
    public void testSetup_whenFlipperIsOff_initializesRequestManager_withPlainRequestModelRepository() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();
        MobileEngage.setup(baseConfig);

        Field repositoryField = RequestManager.class.getDeclaredField("requestRepository");
        repositoryField.setAccessible(true);
        Object repository = repositoryField.get(MobileEngage.instance.manager);
        assertEquals(RequestModelRepository.class, repository.getClass());
    }

    @Test
    public void testSetup_initializesInboxInstance() {
        MobileEngage.inboxInstance = null;
        MobileEngage.setup(baseConfig);

        assertNotNull(MobileEngage.inboxInstance);
        assertEquals(InboxInternal_V1.class, MobileEngage.inboxInstance.getClass());
    }

    @Test
    public void testSetup_initializesInboxInstance_V2_withUserCentricFlipper() {
        MobileEngage.inboxInstance = null;
        MobileEngage.setup(userCentricConfig);

        assertNotNull(MobileEngage.inboxInstance);
        assertEquals(InboxInternal_V2.class, MobileEngage.inboxInstance.getClass());
    }

    @Test
    public void testSetup_initializesDeepLinkInstance() {
        MobileEngage.deepLinkInstance = null;
        MobileEngage.setup(baseConfig);

        assertNotNull(MobileEngage.deepLinkInstance);
    }

    @Test
    public void testSetup_registers_currentActivityWatchDog() throws Exception {
        CurrentActivityWatchdogTestUtils.resetCurrentActivityWatchdog();

        MobileEngage.setup(baseConfig);

        try {
            CurrentActivityWatchdog.getCurrentActivity();
        } catch (Exception e) {
            fail("getCurrentActivity should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testSetup_registers_activityLifecycleWatchdog() throws Exception {
        MobileEngageConfig config = createConfigWithFlippers();
        Application spyApplication = config.getApplication();

        MobileEngage.setup(config);

        verify(spyApplication).registerActivityLifecycleCallbacks(any(ActivityLifecycleWatchdog.class));
    }

    @Test
    public void testSetup_registers_activityLifecycleWatchdog_withInAppStartAction() throws Exception {
        ArgumentCaptor<ActivityLifecycleWatchdog> captor = ArgumentCaptor.forClass(ActivityLifecycleWatchdog.class);

        MobileEngageConfig config = createConfigWithFlippers(MobileEngageFeature.IN_APP_MESSAGING);
        Application spyApplication = config.getApplication();

        MobileEngage.setup(config);

        verify(spyApplication, atLeastOnce()).registerActivityLifecycleCallbacks(captor.capture());
        ActivityLifecycleAction[] actions = CollectionTestUtils.getElementByType(captor.getAllValues(), ActivityLifecycleWatchdog.class).getApplicationStartActions();

        assertEquals(1, CollectionTestUtils.numberOfElementsIn(actions, InAppStartAction.class));
    }

    @Test
    public void testSetup_registers_activityLifecycleWatchdog_withDeepLinkAction() throws Exception {
        ArgumentCaptor<ActivityLifecycleWatchdog> captor = ArgumentCaptor.forClass(ActivityLifecycleWatchdog.class);

        MobileEngageConfig config = createConfigWithFlippers();
        Application spyApplication = config.getApplication();

        MobileEngage.setup(config);

        verify(spyApplication, times(2)).registerActivityLifecycleCallbacks(captor.capture());
        ActivityLifecycleAction[] actions = CollectionTestUtils.getElementByType(captor.getAllValues(), ActivityLifecycleWatchdog.class).getActivityCreatedActions();

        assertEquals(1, CollectionTestUtils.numberOfElementsIn(actions, DeepLinkAction.class));
    }

    @Test
    public void testSetup_initializesWithConfig() {
        MobileEngage.config = null;
        MobileEngage.setup(baseConfig);

        assertEquals(baseConfig, MobileEngage.getConfig());
    }

    @Test
    public void testSetup_initializesMobileEngageUtils() {
        MobileEngageConfig disabled = new MobileEngageConfig.Builder()
                .from(baseConfig)
                .enableIdlingResource(false)
                .build();

        MobileEngageConfig enabled = new MobileEngageConfig.Builder()
                .from(baseConfig)
                .enableIdlingResource(true)
                .build();

        MobileEngageUtils.setup(disabled);
        assertNull(MobileEngageUtils.getIdlingResource());

        MobileEngage.setup(enabled);
        assertNotNull(MobileEngageUtils.getIdlingResource());
    }

    @Test
    public void testSetup_initializesInstances_withTheSame_requestContext() {
        MobileEngage.setup(baseConfig);

        assertThat(MobileEngage.requestContext, Matchers.allOf(
                Matchers.is(MobileEngage.instance.getRequestContext()),
                Matchers.is(((InboxInternal_V1) MobileEngage.inboxInstance).getRequestContext())
        ));
    }

    @Test
    public void testSetup_initializesInstances_withTheSame_requestContext_withUserCentricFlipper() {
        MobileEngage.setup(userCentricConfig);

        assertThat(MobileEngage.requestContext, Matchers.allOf(
                Matchers.is(MobileEngage.instance.getRequestContext()),
                Matchers.is(((InboxInternal_V2) MobileEngage.inboxInstance).getRequestContext())
        ));
    }

    @Test
    public void testSetPushToken_callsInternal() {
        String pushtoken = "pushtoken";
        MobileEngage.setPushToken(pushtoken);
        verify(mobileEngageInternal).setPushToken(pushtoken);
    }

    @Test
    public void testSetStatusListener_callsInternal() {
        MobileEngageStatusListener listener = mock(MobileEngageStatusListener.class);
        MobileEngage.setStatusListener(listener);
        verify(coreCompletionHandler).setStatusListener(listener);
    }

    @Test
    public void testSetStatusListener_shouldSwapListener() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        FakeStatusListener originalListener = new FakeStatusListener();
        FakeStatusListener newListener = new FakeStatusListener(latch);

        MobileEngageCoreCompletionHandler completionHandler = new MobileEngageCoreCompletionHandler(new ArrayList<AbstractResponseHandler>(), originalListener);
        RequestManager succeedingManager = new FakeRequestManager(
                SUCCESS,
                null,
                completionHandler);

        RequestIdProvider requestIdProvider = mock(RequestIdProvider.class);
        when(requestIdProvider.provideId()).thenReturn("REQUEST_ID");

        MobileEngageInternal internal = new MobileEngageInternal(
                baseConfig,
                succeedingManager,
                mock(Handler.class),
                completionHandler,
                new RequestContext(
                        baseConfig,
                        mock(DeviceInfo.class),
                        new AppLoginStorage(application),
                        mock(MeIdStorage.class),
                        mock(MeIdSignatureStorage.class),
                        mock(TimestampProvider.class),
                        requestIdProvider
                ));

        MobileEngage.completionHandler = completionHandler;
        MobileEngage.instance = internal;

        MobileEngage.setStatusListener(newListener);
        MobileEngage.appLogin();

        latch.await();

        assertEquals(0, originalListener.onStatusLogCount);
        assertEquals(0, originalListener.onErrorCount);
        assertEquals(1, newListener.onStatusLogCount);
        assertEquals(0, newListener.onErrorCount);
    }

    @Test
    public void testAppLogin_anonymous_callsInternalMobileEngage() {
        MobileEngage.appLogin();
        verify(mobileEngageInternal).appLogin();
    }

    @Test
    public void testAppLogin_anonymous_setsApploginParameters_inRequestContext() {
        MobileEngage.setup(baseConfig);
        MobileEngage.appLogin();

        assertEquals(new AppLoginParameters(), MobileEngage.requestContext.getAppLoginParameters());
    }

    @Test
    public void testAppLogin_withUser_callsInternalMobileEngage() {
        MobileEngage.appLogin(4, "CONTACT_FIELD_VALUE");

        verify(mobileEngageInternal).appLogin(4, "CONTACT_FIELD_VALUE");
    }

    @Test
    public void testAppLogin_withUser_setsApploginParameters_inRequestContext() {
        MobileEngage.setup(baseConfig);
        int contactFieldId = 4;
        String contactFieldValue = "CONTACT_FIELD_VALUE";
        MobileEngage.appLogin(contactFieldId, contactFieldValue);

        assertEquals(new AppLoginParameters(contactFieldId, contactFieldValue), MobileEngage.requestContext.getAppLoginParameters());
    }

    @Test
    public void testAppLogout_callsInternalMobileEngage() {
        MobileEngage.appLogout();
        verify(mobileEngageInternal).appLogout();
    }

    @Test
    public void testAppLogout_clearsApploginParameters_inRequestContext() {
        MobileEngage.setup(baseConfig);
        MobileEngage.appLogin();
        MobileEngage.appLogout();

        assertNull(MobileEngage.requestContext.getAppLoginParameters());
    }

    @Test
    public void testTrackCustomEvent_callsInternal() throws Exception {
        Map<String, String> attributes = mock(Map.class);
        MobileEngage.trackCustomEvent("event", attributes);
        verify(mobileEngageInternal).trackCustomEvent("event", attributes);
    }

    @Test
    public void testTrackMessageOpen_intent_callsInternal() {
        Intent intent = mock(Intent.class);
        MobileEngage.trackMessageOpen(intent);
        verify(mobileEngageInternal).trackMessageOpen(intent);
    }

    @Test
    public void testTrackMessageOpen_message_callsInternal() throws JSONException {
        Notification message = new Notification("id", "sid", "title", null, new HashMap<String, String>(), new JSONObject(), 7200, new Date().getTime());
        MobileEngage.Inbox.trackMessageOpen(message);
        verify(inboxInternal).trackMessageOpen(message);
    }

    @Test
    public void testTrackDeepLinkOpen_callsInternal() throws Exception {
        Intent intent = mock(Intent.class);
        Activity activity = mock(Activity.class, RETURNS_DEEP_STUBS);

        MobileEngage.trackDeepLink(activity, intent);

        verify(deepLinkInternal).trackDeepLinkOpen(activity, intent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackDeepLinkOpen_throwExceptionWhenIntentIsNull() throws Exception {
        MobileEngage.trackDeepLink(mock(Activity.class, RETURNS_DEEP_STUBS), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackDeepLinkOpen_throwExceptionWhenActivityIsNull() throws Exception {
        MobileEngage.trackDeepLink(null, new Intent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackDeepLinkOpen_throwExceptionWhenIntentFromActivity_isNull() throws Exception {
        MobileEngage.trackDeepLink(mock(Activity.class), new Intent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetup_whenConfigIsNull() {
        MobileEngage.setup(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppLogin_whenContactFieldValueIsNull() {
        MobileEngage.appLogin(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackCustomEvent_whenEventNameIsNull() {
        MobileEngage.trackCustomEvent(null, new HashMap<String, String>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackMessageOpen_intent_whenIntentIsNull() {
        MobileEngage.trackMessageOpen(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFetchNotifications_whenListenerIsNull() {
        MobileEngage.Inbox.fetchNotifications(null);
    }

    @Test
    public void testFetchNotifications_callsInternal() {
        InboxResultListener inboxListenerMock = mock(InboxResultListener.class);
        MobileEngage.Inbox.fetchNotifications(inboxListenerMock);
        verify(inboxInternal).fetchNotifications(inboxListenerMock);
    }

    @Test
    public void testResetBadgeCount_callsInternal() {
        ResetBadgeCountResultListener listener = mock(ResetBadgeCountResultListener.class);
        MobileEngage.Inbox.resetBadgeCount(listener);
        verify(inboxInternal).resetBadgeCount(listener);
    }

    @Test
    public void testResetBadgeCount_zeroArgs_callsInternal_withNullListener() {
        MobileEngage.Inbox.resetBadgeCount();
        verify(inboxInternal).resetBadgeCount(null);
    }

    private MobileEngageConfig createConfigWithFlippers(FlipperFeature... experimentalFeatures) {
        return new MobileEngageConfig.Builder()
                .application(spy(application))
                .credentials(appID, appSecret)
                .disableDefaultChannel()
                .enableExperimentalFeatures(experimentalFeatures)
                .setDefaultInAppEventHandler(new EventHandler() {
                    @Override
                    public void handleEvent(String eventName, JSONObject payload) {

                    }
                })
                .build();
    }

    @Test
    public void testPurgeNotificationCache_callsInternal() {
        MobileEngage.Inbox.purgeNotificationCache();
        verify(inboxInternal).purgeNotificationCache();
    }
}