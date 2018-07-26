package com.emarsys.mobileengage;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.di.DependencyInjection;
import com.emarsys.mobileengage.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.fake.FakeStatusListener;
import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.ConnectionTestUtils;
import com.emarsys.mobileengage.testUtil.DatabaseTestUtils;
import com.emarsys.mobileengage.testUtil.ExperimentalTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class MobileEngageIntegrationTest {

    static {
        mock(Activity.class);
    }

    private CountDownLatch latch;
    private FakeStatusListener listener;

    private Application context;
    private Activity activity;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        DatabaseTestUtils.deleteCoreDatabase();
        DatabaseTestUtils.deleteMobileEngageDatabase();
        DependencyInjection.tearDown();

        context = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        activity = mock(Activity.class, Mockito.RETURNS_DEEP_STUBS);
        clearStorages();

        ConnectionTestUtils.checkConnection(context);

        latch = new CountDownLatch(1);
        listener = new FakeStatusListener(latch, FakeStatusListener.Mode.MAIN_THREAD);
        MobileEngageConfig config = new MobileEngageConfig.Builder()
                .application(context)
                .credentials("14C19-A121F", "PaNkfOD90AVpYimMBuZopCpm8OWCrREu")
                .statusListener(listener)
                .disableDefaultChannel()
                .enableExperimentalFeatures(MobileEngageFeature.IN_APP_MESSAGING)
                .setDefaultInAppEventHandler(mock(EventHandler.class))
                .build();
        MobileEngage.setup(config);
    }

    @After
    public void tearDown() {
        MobileEngage.coreSdkHandler.getLooper().quit();
        clearStorages();
        MobileEngage.InApp.setPaused(false);
        DependencyInjection.tearDown();
    }

    @Test
    public void testAppLogin_anonymous() throws Exception {
        eventuallyAssertSuccess(MobileEngage.appLogin());
    }

    @Test
    public void testAppLogin() throws Exception {
        eventuallyAssertSuccess(MobileEngage.appLogin(345, "contactFieldValue"));
    }

    @Test
    public void testAppLogout() throws Exception {
        eventuallyAssertSuccess(MobileEngage.appLogout());
    }

    @Test
    public void testTrackCustomEvent_V3_noAttributes() throws Exception {
        doAppLogin();

        eventuallyAssertSuccess(MobileEngage.trackCustomEvent("customEvent", null));
    }

    @Test
    public void testTrackCustomEvent_V3_emptyAttributes() throws Exception {
        doAppLogin();

        eventuallyAssertSuccess(MobileEngage.trackCustomEvent("customEvent", new HashMap<String, String>()));
    }

    @Test
    public void testTrackCustomEvent_V3_withPausedInApp() throws Exception {
        doAppLogin();

        MobileEngage.InApp.setPaused(true);

        eventuallyAssertSuccess(MobileEngage.trackCustomEvent("customEvent", new HashMap<String, String>()));
    }

    @Test
    public void testTrackCustomEvent_V3_withResumedInApp() throws Exception {
        doAppLogin();

        MobileEngage.InApp.setPaused(false);

        eventuallyAssertSuccess(MobileEngage.trackCustomEvent("customEvent", new HashMap<String, String>()));
    }

    @Test
    public void testTrackCustomEvent_V3_withAttributes() throws Exception {
        doAppLogin();

        Map<String, String> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");

        eventuallyAssertSuccess(MobileEngage.trackCustomEvent("customEvent", attributes));
    }

    @Test
    public void testTrackCustomEvent_V2_noAttributes() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        doAppLogin();

        eventuallyAssertSuccess(MobileEngage.trackCustomEvent("customEvent", null));
    }

    @Test
    public void testTrackCustomEvent_V2_emptyAttributes() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        doAppLogin();

        eventuallyAssertSuccess(MobileEngage.trackCustomEvent("customEvent", new HashMap<String, String>()));
    }

    @Test
    public void testTrackCustomEvent_V2_withAttributes() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        doAppLogin();

        Map<String, String> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");

        eventuallyAssertSuccess(MobileEngage.trackCustomEvent("customEvent", attributes));
    }

    @Test
    public void testTrackMessageOpen_intent_V2() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();
        Intent intent = new Intent();
        Bundle payload = new Bundle();
        payload.putString("key1", "value1");
        payload.putString("u", "{\"sid\": \"dd8_zXfDdndBNEQi\"}");
        intent.putExtra("payload", payload);

        eventuallyAssertSuccess(MobileEngage.trackMessageOpen(intent));
    }

    @Test
    public void testTrackMessageOpen_intent_V3() throws Exception {
        doAppLogin();

        Intent intent = new Intent();
        Bundle payload = new Bundle();
        payload.putString("key1", "value1");
        payload.putString("u", "{\"sid\": \"dd8_zXfDdndBNEQi\"}");
        intent.putExtra("payload", payload);

        eventuallyAssertSuccess(MobileEngage.trackMessageOpen(intent));
    }

    @Test
    public void testTrackMessageOpen_notification() throws Exception {
        Notification notification = new Notification(
                "id",
                "161e_D/1UiO/jCmE4",
                "title",
                null,
                new HashMap<String, String>(),
                new JSONObject(),
                2000,
                new Date().getTime());

        eventuallyAssertSuccess(MobileEngage.Inbox.trackMessageOpen(notification));
    }

    @Test
    public void testDeepLinkOpen_intent() throws Exception {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&ems_dl=1_2_3_4_5_6"));

        MobileEngage.trackDeepLink(activity, intent);
        eventuallyAssertSuccess();
    }

    private void doAppLogin() throws InterruptedException {
        MobileEngage.appLogin(3, "test@test.com");
        latch.await();

        latch = new CountDownLatch(1);
        listener.latch = latch;
        listener.onStatusLogCount = 0;
    }

    private void eventuallyAssertSuccess(String id) throws Exception {
        latch.await();
        eventuallyAssertSuccess();
        assertEquals(id, listener.successId);
    }

    private void eventuallyAssertSuccess() throws Exception {
        latch.await();
        assertNull(listener.errorCause);
        assertEquals(1, listener.onStatusLogCount);
        assertEquals(0, listener.onErrorCount);
        assertNotNull(listener.successLog);
        assertNull(listener.errorId);
    }

    private void clearStorages() {
        new MeIdStorage(context).remove();
        new AppLoginStorage(context).remove();
    }
}