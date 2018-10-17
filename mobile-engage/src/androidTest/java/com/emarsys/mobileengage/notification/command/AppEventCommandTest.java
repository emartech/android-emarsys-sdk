package com.emarsys.mobileengage.notification.command;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.di.DependencyInjection;
import com.emarsys.mobileengage.api.NotificationEventHandler;
import com.emarsys.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AppEventCommandTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    private Context applicationContext;
    private NotificationEventHandler notificationHandler;

    @Before
    public void setUp() {
        DependencyInjection.tearDown();

        applicationContext = InstrumentationRegistry.getTargetContext().getApplicationContext();

        notificationHandler = mock(NotificationEventHandler.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_shouldThrowException_whenThereIsNoContext() {
        new AppEventCommand(null, notificationHandler, "", mock(JSONObject.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_shouldThrowException_whenThereIsNoEventName() {
        new AppEventCommand(applicationContext, notificationHandler, null, mock(JSONObject.class));
    }

    @Test
    public void testRun_invokeHandleEventMethod_onNotificationEventHandler() throws JSONException {
        String name = "nameOfTheEvent";
        JSONObject payload = new JSONObject()
                .put("payloadKey", "payloadValue");
        new AppEventCommand(applicationContext, notificationHandler, name, payload).run();

        verify(notificationHandler).handleEvent(applicationContext, name, payload);
    }

    @Test
    public void testRun_invokeHandleEventMethod_onNotificationEventHandler_whenThereIsNoPayload() throws JSONException {
        String name = "nameOfTheEvent";
        new AppEventCommand(applicationContext, notificationHandler, name, null).run();

        verify(notificationHandler).handleEvent(applicationContext, name, null);
    }

    @Test
    public void testRun_shouldIgnoreHandler_ifNull() {
        try {
            new AppEventCommand(applicationContext, notificationHandler, "", null).run();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}