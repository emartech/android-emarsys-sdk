package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.request.RequestIdProvider;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.fake.FakeRequestManager;
import com.emarsys.mobileengage.fake.FakeStatusListener;
import com.emarsys.mobileengage.responsehandler.AbstractResponseHandler;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.emarsys.mobileengage.fake.FakeRequestManager.ResponseType.FAILURE;
import static com.emarsys.mobileengage.fake.FakeRequestManager.ResponseType.SUCCESS;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MobileEngageInternalStatusListenerTest {

    public static final String EVENT_NAME = "event";
    private static String APPLICATION_ID = "user";
    private static String APPLICATION_SECRET = "pass";
    private static final int CONTACT_FIELD_ID = 3456;
    public static final String CONTACT_FIELD_VALUE = "value";

    private MobileEngageCoreCompletionHandler completionHandler;
    private MobileEngageInternal mobileEngage;
    private MobileEngageStatusListener statusListener;
    private FakeStatusListener mainThreadStatusListener;
    private Map<String, String> authHeader;
    private MobileEngageConfig baseConfig;
    private RequestManager manager;
    private RequestManager failingManager;
    private RequestManager succeedingManager;
    private Application application;
    private Context context;
    private CountDownLatch latch;
    private Handler coreSdkHandler;
    private Intent intent;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();
    private RequestContext requestContext;

    @Before
    public void init() throws Exception {
        authHeader = new HashMap<>();
        authHeader.put("Authorization", "Basic dXNlcjpwYXNz");
        context = InstrumentationRegistry.getTargetContext();
        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();

        coreSdkHandler = new CoreSdkHandlerProvider().provideHandler();

        intent = new Intent();
        Bundle payload = new Bundle();
        payload.putString("key1", "value1");
        payload.putString("u", "{\"sid\": \"+43c_lODSmXqCvdOz\"}");
        intent.putExtra("payload", payload);

        manager = mock(RequestManager.class);
        latch = new CountDownLatch(1);
        statusListener = mock(MobileEngageStatusListener.class);
        completionHandler = new MobileEngageCoreCompletionHandler(new ArrayList<AbstractResponseHandler>(), statusListener) {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {
                mainThreadStatusListener.onStatusLog(id, responseModel.getMessage());
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                Exception exception = new MobileEngageException(
                        responseModel.getStatusCode(),
                        responseModel.getMessage(),
                        responseModel.getBody());
                mainThreadStatusListener.onError(id, exception);
            }

            @Override
            public void onError(String id, Exception cause) {
                mainThreadStatusListener.onError(id, cause);
            }
        };
        succeedingManager = new FakeRequestManager(SUCCESS, latch, completionHandler);
        failingManager = new FakeRequestManager(FAILURE, latch, completionHandler);
        statusListener = mock(MobileEngageStatusListener.class);
        mainThreadStatusListener = new FakeStatusListener(latch);
        mobileEngageWith(mainThreadStatusListener, succeedingManager);
    }

    @After
    public void tearDown() {
        coreSdkHandler.getLooper().quit();
    }

    private void mobileEngageWith(MobileEngageStatusListener statusListener, RequestManager requestManager) {
        baseConfig = new MobileEngageConfig.Builder()
                .application(application)
                .credentials(APPLICATION_ID, APPLICATION_SECRET)
                .statusListener(statusListener)
                .disableDefaultChannel()
                .build();

        MeIdStorage meIdStorage = mock(MeIdStorage.class);
        when(meIdStorage.get()).thenReturn("meId");
        MeIdSignatureStorage meIdSignatureStorage = mock(MeIdSignatureStorage.class);
        when(meIdSignatureStorage.get()).thenReturn("meIdSignature");
        RequestIdProvider requestIdProvider = mock(RequestIdProvider.class);
        when(requestIdProvider.provideId()).thenReturn("REQUEST_ID");
        requestContext = new RequestContext(
                baseConfig,
                mock(DeviceInfo.class),
                new AppLoginStorage(context),
                meIdStorage,
                meIdSignatureStorage,
                mock(TimestampProvider.class),
                requestIdProvider);

        mobileEngage = new MobileEngageInternal(
                baseConfig,
                requestManager,
                coreSdkHandler,
                completionHandler,
                requestContext);
    }

    @Test
    public void testAppLogin_anonymus_statusListenerCalledWithSuccess() throws Exception {
        eventuallyAssertSuccess(mobileEngage.appLogin());
    }

    @Test
    public void testAppLogin_statusListenerCalledWithSuccess() throws Exception {
        eventuallyAssertSuccess(mobileEngage.appLogin(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE));
    }

    @Test
    public void testAppLogout_statusListenerCalledWithSuccess() throws Exception {
        eventuallyAssertSuccess(mobileEngage.appLogout());
    }

    @Test
    public void testTrackCustomEvent_statusListenerCalledWithSuccess() throws Exception {
        eventuallyAssertSuccess(mobileEngage.trackCustomEvent(EVENT_NAME, null));
    }

    @Test
    public void testTrackMessageOpen_intent_statusListenerCalledWithSuccess() throws Exception {
        eventuallyAssertSuccess(mobileEngage.trackMessageOpen(intent));
    }

    @Test
    public void testAppLogin_anonymous_statusListenerCalledWithFailure() throws Exception {
        mobileEngageWith(mainThreadStatusListener, failingManager);
        eventuallyAssertFailure(mobileEngage.appLogin());
    }

    @Test
    public void testAppLogin_statusListenerCalledWithFailure() throws Exception {
        mobileEngageWith(mainThreadStatusListener, failingManager);
        eventuallyAssertFailure(mobileEngage.appLogin(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE));
    }

    @Test
    public void testAppLogout_statusListenerCalledWithFailure() throws Exception {
        mobileEngageWith(mainThreadStatusListener, failingManager);
        eventuallyAssertFailure(mobileEngage.appLogout());
    }

    @Test
    public void testTrackCustomEvent_statusListenerCalledWithFailure() throws Exception {
        mobileEngageWith(mainThreadStatusListener, failingManager);
        eventuallyAssertFailure(mobileEngage.trackCustomEvent(EVENT_NAME, null));
    }

    @Test
    public void testTrackMessageOpen_intent_statusListenerCalledWithFailure() throws Exception {
        mobileEngageWith(mainThreadStatusListener, failingManager);
        eventuallyAssertFailure(mobileEngage.trackMessageOpen(intent));
    }

    @Test
    public void testTrackMessageOpen_intent_whenIntentIsEmpty() throws Exception {
        mobileEngageWith(mainThreadStatusListener, manager);
        eventuallyAssertFailure(mobileEngage.trackMessageOpen(new Intent()), IllegalArgumentException.class, "No messageId found!");
    }

    private void eventuallyAssertSuccess(String expectedId) throws Exception {
        latch.await();
        assertEquals(1, mainThreadStatusListener.onStatusLogCount);
        assertEquals(0, mainThreadStatusListener.onErrorCount);
        assertEquals(expectedId, mainThreadStatusListener.successId);
        assertEquals("OK", mainThreadStatusListener.successLog);
        assertNull(mainThreadStatusListener.errorId);
        assertNull(mainThreadStatusListener.errorCause);
    }

    private void eventuallyAssertFailure(String expectedId) throws Exception {
        eventuallyAssertFailure(expectedId, Exception.class, null);
    }

    private void eventuallyAssertFailure(String expectedId, Class type, String errorMessage) throws Exception {
        latch.await();
        assertEquals(0, mainThreadStatusListener.onStatusLogCount);
        assertEquals(1, mainThreadStatusListener.onErrorCount);
        assertEquals(expectedId, mainThreadStatusListener.errorId);
        assertEquals(type, mainThreadStatusListener.errorCause.getClass());
        assertEquals(errorMessage, mainThreadStatusListener.errorCause.getMessage());
        assertNull(mainThreadStatusListener.successId);
        assertNull(mainThreadStatusListener.successLog);
    }
}