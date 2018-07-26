package com.emarsys.mobileengage;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.di.DependencyInjection;
import com.emarsys.mobileengage.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.fake.FakeInboxResultListener;
import com.emarsys.mobileengage.fake.FakeResetBadgeCountResultListener;
import com.emarsys.mobileengage.fake.FakeStatusListener;
import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.testUtil.ConnectionTestUtils;
import com.emarsys.mobileengage.testUtil.DatabaseTestUtils;
import com.emarsys.mobileengage.testUtil.SharedPrefsUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class InboxV2IntegrationTest {

    private CountDownLatch latch;
    private CountDownLatch inboxLatch;
    private CountDownLatch resetLatch;
    private FakeStatusListener listener;
    private FakeInboxResultListener inboxListener;
    private FakeResetBadgeCountResultListener resetListener;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        DatabaseTestUtils.deleteCoreDatabase();
        DatabaseTestUtils.deleteMobileEngageDatabase();
        SharedPrefsUtils.deleteMobileEngageSharedPrefs();
        DependencyInjection.tearDown();

        Application context = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        ConnectionTestUtils.checkConnection(context);

        latch = new CountDownLatch(1);
        listener = new FakeStatusListener(latch, FakeStatusListener.Mode.MAIN_THREAD);
        MobileEngageConfig config = new MobileEngageConfig.Builder()
                .application(context)
                .credentials("14C19-A121F", "PaNkfOD90AVpYimMBuZopCpm8OWCrREu")
                .statusListener(listener)
                .disableDefaultChannel()
                .enableExperimentalFeatures(MobileEngageFeature.USER_CENTRIC_INBOX)
                .setDefaultInAppEventHandler(mock(EventHandler.class))
                .build();
        MobileEngage.setup(config);

        inboxLatch = new CountDownLatch(1);
        resetLatch = new CountDownLatch(1);
        inboxListener = new FakeInboxResultListener(inboxLatch, FakeInboxResultListener.Mode.MAIN_THREAD);
        resetListener = new FakeResetBadgeCountResultListener(resetLatch);
    }

    @After
    public void tearDown() {
        DatabaseTestUtils.deleteCoreDatabase();
        SharedPrefsUtils.deleteMobileEngageSharedPrefs();
        MobileEngage.coreSdkHandler.getLooper().quit();
        DependencyInjection.tearDown();
    }

    @Test
    public void fetchNotifications() throws InterruptedException {
        doAppLogin();

        MobileEngage.Inbox.fetchNotifications(inboxListener);
        inboxLatch.await();

        assertNull(inboxListener.errorCause);
        assertEquals(1, inboxListener.successCount);
        assertEquals(0, inboxListener.errorCount);
        assertNotNull(inboxListener.resultStatus);
    }

    @Test
    public void resetBadgeCount() throws InterruptedException {
        doAppLogin();

        MobileEngage.Inbox.resetBadgeCount(resetListener);
        resetLatch.await();

        assertNull(resetListener.errorCause);
        assertEquals(1, resetListener.successCount);
        assertEquals(0, resetListener.errorCount);
    }

    @Test
    public void testTrackMessageOpen() throws InterruptedException {
        doAppLogin();

        MobileEngage.Inbox.fetchNotifications(inboxListener);
        inboxLatch.await();

        List<Notification> notifications = inboxListener.resultStatus.getNotifications();

        assertTrue(notifications.size() > 0);

        latch = new CountDownLatch(1);
        listener = new FakeStatusListener(latch, FakeStatusListener.Mode.MAIN_THREAD);
        MobileEngage.setStatusListener(listener);

        MobileEngage.Inbox.trackMessageOpen(notifications.get(0));
        latch.await();

        assertNull(listener.errorCause);
        assertEquals(1, listener.onStatusLogCount);
        assertEquals(0, listener.onErrorCount);
    }

    private void doAppLogin() throws InterruptedException {
        MobileEngage.appLogin(3, "test@test.com");
        latch.await();
    }

}
