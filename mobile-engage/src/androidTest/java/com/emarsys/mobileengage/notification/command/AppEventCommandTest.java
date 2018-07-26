package com.emarsys.mobileengage.notification.command;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.di.DependencyInjection;
import com.emarsys.mobileengage.notification.NotificationEventHandler;
import com.emarsys.mobileengage.testUtil.ReflectionTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AppEventCommandTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    private MobileEngageConfig config;
    private Context applicationContext;
    private NotificationEventHandler notificationHandler;

    @Before
    public void setUp() {
        DependencyInjection.tearDown();

        applicationContext = InstrumentationRegistry.getTargetContext().getApplicationContext();

        notificationHandler = mock(NotificationEventHandler.class);
        config = new MobileEngageConfig.Builder()
                .application((Application) applicationContext)
                .credentials("EMSEC-B103E", "RM1ZSuX8mgRBhQIgOsf6m8bn/bMQLAIb")
                .setNotificationEventHandler(notificationHandler)
                .disableDefaultChannel()
                .build();
        MobileEngage.setup(config);
    }

    @After
    public void tearDown() throws Exception {
        DependencyInjection.tearDown();
        closeMobileEngage();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_shouldThrowException_whenThereIsNoContext() {
        new AppEventCommand(null, "", mock(JSONObject.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_shouldThrowException_whenThereIsNoEventName() {
        new AppEventCommand(applicationContext, null, mock(JSONObject.class));
    }

    @Test
    public void testRun_invokeHandleEventMethod_onNotificationEventHandler() throws JSONException {
        String name = "nameOfTheEvent";
        JSONObject payload = new JSONObject()
                .put("payloadKey", "payloadValue");
        new AppEventCommand(applicationContext, name, payload).run();

        verify(notificationHandler).handleEvent(applicationContext, name, payload);
    }

    @Test
    public void testRun_invokeHandleEventMethod_onNotificationEventHandler_whenThereIsNoPayload() throws JSONException {
        String name = "nameOfTheEvent";
        new AppEventCommand(applicationContext, name, null).run();

        verify(notificationHandler).handleEvent(applicationContext, name, null);
    }

    @Test
    public void testRun_shouldIgnoreHandler_ifNull() throws Exception {
        closeMobileEngage();

        config = new MobileEngageConfig.Builder()
                .application((Application) applicationContext)
                .credentials("EMSEC-B103E", "RM1ZSuX8mgRBhQIgOsf6m8bn/bMQLAIb")
                .disableDefaultChannel()
                .build();
        MobileEngage.setup(config);

        try {
            new AppEventCommand(applicationContext, "", null).run();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private void closeMobileEngage() throws Exception {
        Handler coreSdkHandler = ReflectionTestUtils.getStaticField(MobileEngage.class, "coreSdkHandler");
        coreSdkHandler.getLooper().quit();
    }
}