package com.emarsys.mobileengage;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.emarsys.core.DefaultCoreCompletionHandler;
import com.emarsys.core.activity.ActivityLifecycleAction;
import com.emarsys.core.activity.ActivityLifecycleWatchdog;
import com.emarsys.core.api.experimental.FlipperFeature;
import com.emarsys.core.di.DependencyContainer;
import com.emarsys.core.di.DependencyInjection;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModelRepository;
import com.emarsys.mobileengage.api.EventHandler;
import com.emarsys.mobileengage.api.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.deeplink.DeepLinkAction;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.iam.InAppStartAction;
import com.emarsys.mobileengage.iam.model.requestRepositoryProxy.RequestRepositoryProxy;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.mobileengage.inbox.InboxInternal_V1;
import com.emarsys.mobileengage.inbox.InboxInternal_V2;
import com.emarsys.mobileengage.testUtil.ExperimentalTestUtils;
import com.emarsys.testUtil.CollectionTestUtils;
import com.emarsys.testUtil.DatabaseTestUtils;
import com.emarsys.testUtil.ReflectionTestUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Ignore
@RunWith(AndroidJUnit4.class)
public class MobileEngageTest {

    static {
        mock(Application.class);
        mock(Activity.class);
        mock(Intent.class);
    }

    private static final String APPLICATION_CODE = "56789876";
    private static final String APPLICATION_PASSWORD = "secret";

    private DefaultCoreCompletionHandler coreCompletionHandler;
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
        coreCompletionHandler = mock(DefaultCoreCompletionHandler.class);
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
        Assert.assertEquals(MobileEngageDependencyContainer.class, container.getClass());
    }

    @Test
    public void testSetup_initializesMobileEngageInstance() {
        MobileEngage.instance = null;
        MobileEngage.setup(baseConfig);

        assertNotNull(MobileEngage.instance);
    }


    @Test
    public void testSetup_initializesInAppPaused_withFalse() {
        MobileEngage.InApp.setPaused(true);

        MobileEngage.setup(baseConfig);

        assertFalse(MobileEngage.InApp.isPaused());
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
    @Ignore
    public void testSetup_initializesMobileEngageUtils() {
        MobileEngageConfig disabled = new MobileEngageConfig.Builder()
                .from(baseConfig)
                .enableIdlingResource(false)
                .build();

        MobileEngageConfig enabled = new MobileEngageConfig.Builder()
                .from(baseConfig)
                .enableIdlingResource(true)
                .build();

        MobileEngage.setup(enabled);
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
        verify(mobileEngageInternal).appLogout(null);
    }

    @Test
    public void testAppLogout_clearsApploginParameters_inRequestContext() {
        MobileEngage.setup(baseConfig);
        MobileEngage.appLogin();
        MobileEngage.appLogout();

        assertNull(MobileEngage.requestContext.getAppLoginParameters());
    }

    @Test
    public void testTrackCustomEvent_callsInternal() {
        Map<String, String> attributes = mock(Map.class);
        MobileEngage.trackCustomEvent("event", attributes);
        verify(mobileEngageInternal).trackCustomEvent("event", attributes, null);
    }

    @Test
    public void testTrackMessageOpen_intent_callsInternal() {
        Intent intent = mock(Intent.class);
        MobileEngage.trackMessageOpen(intent);
        verify(mobileEngageInternal).trackMessageOpen(intent, null);
    }

    @Test
    public void testTrackDeepLinkOpen_callsInternal() {
        Intent intent = mock(Intent.class);
        Activity activity = mock(Activity.class, RETURNS_DEEP_STUBS);

        MobileEngage.trackDeepLink(activity, intent);

        verify(deepLinkInternal).trackDeepLinkOpen(activity, intent, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackDeepLinkOpen_throwExceptionWhenIntentIsNull() {
        MobileEngage.trackDeepLink(mock(Activity.class, RETURNS_DEEP_STUBS), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackDeepLinkOpen_throwExceptionWhenActivityIsNull() {
        MobileEngage.trackDeepLink(null, new Intent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackDeepLinkOpen_throwExceptionWhenIntentFromActivity_isNull() {
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
    public void testResetBadgeCount_zeroArgs_callsInternal_withNullListener() {
        MobileEngage.Inbox.resetBadgeCount();
        verify(inboxInternal).resetBadgeCount(null);
    }

    private MobileEngageConfig createConfigWithFlippers(FlipperFeature... experimentalFeatures) {
        return new MobileEngageConfig.Builder()
                .application(spy(application))
                .credentials(APPLICATION_CODE, APPLICATION_PASSWORD)
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