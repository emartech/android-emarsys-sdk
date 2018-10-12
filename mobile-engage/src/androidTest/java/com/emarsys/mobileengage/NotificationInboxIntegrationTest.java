package com.emarsys.mobileengage;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.di.DependencyInjection;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.fake.FakeInboxResultListener;
import com.emarsys.mobileengage.fake.FakeResetBadgeCountResultListener;
import com.emarsys.testUtil.ConnectionTestUtils;
import com.emarsys.testUtil.DatabaseTestUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

@Ignore
public class NotificationInboxIntegrationTest {

    private CountDownLatch latch;
    private CountDownLatch inboxLatch;
    private CountDownLatch resetLatch;
    private FakeInboxResultListener inboxListener;
    private FakeResetBadgeCountResultListener resetListener;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        DatabaseTestUtils.deleteCoreDatabase();
        DependencyInjection.tearDown();

        Application context = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        ConnectionTestUtils.checkConnection(context);

        latch = new CountDownLatch(1);
        MobileEngageConfig config = new MobileEngageConfig.Builder()
                .application(context)
                .credentials("14C19-A121F", "PaNkfOD90AVpYimMBuZopCpm8OWCrREu")
                .disableDefaultChannel()
                .build();
        MobileEngage.setup(config);

        inboxLatch = new CountDownLatch(1);
        resetLatch = new CountDownLatch(1);
        inboxListener = new FakeInboxResultListener(inboxLatch, FakeInboxResultListener.Mode.MAIN_THREAD);
        resetListener = new FakeResetBadgeCountResultListener(resetLatch);
    }

    @After
    public void tearDown() {
        DependencyInjection.tearDown();
    }

    @Test
    public void fetchNotifications() throws InterruptedException {
        MobileEngage.appLogin(3, "test@test.com");
        latch.await();

        inboxLatch.await();

        assertNull(inboxListener.errorCause);
        assertEquals(1, inboxListener.successCount);
        assertEquals(0, inboxListener.errorCount);
        assertNotNull(inboxListener.resultStatus);
    }

    @Test
    public void resetBadgeCount() throws InterruptedException {
        MobileEngage.appLogin(3, "test@test.com");
        latch.await();

        resetLatch.await();

        assertNull(resetListener.errorCause);
        assertEquals(1, resetListener.successCount);
        assertEquals(0, resetListener.errorCount);
    }
}
