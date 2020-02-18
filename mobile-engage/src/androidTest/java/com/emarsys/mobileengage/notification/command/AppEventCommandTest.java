package com.emarsys.mobileengage.notification.command;

import android.content.Context;

import com.emarsys.core.di.DependencyInjection;
import com.emarsys.mobileengage.api.event.EventHandler;
import com.emarsys.mobileengage.event.EventHandlerProvider;
import com.emarsys.testUtil.InstrumentationRegistry;
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
import static org.mockito.Mockito.when;

public class AppEventCommandTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    private Context applicationContext;
    private EventHandlerProvider eventHandlerProvider;
    private EventHandler mockEventHandler;

    @Before
    public void setUp() {
        DependencyInjection.tearDown();

        applicationContext = InstrumentationRegistry.getTargetContext().getApplicationContext();
        mockEventHandler = mock(EventHandler.class);

        eventHandlerProvider = mock(EventHandlerProvider.class);
        when(eventHandlerProvider.getEventHandler()).thenReturn(mockEventHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_shouldThrowException_whenThereIsNoContext() {
        new AppEventCommand(null, eventHandlerProvider, "", mock(JSONObject.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_shouldThrowException_whenThereIsNoEventName() {
        new AppEventCommand(applicationContext, eventHandlerProvider, null, mock(JSONObject.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_shouldThrowException_whenThereIsEventHandlerProvider() {
        new AppEventCommand(applicationContext, null, "", mock(JSONObject.class));
    }

    @Test
    public void testRun_invokeHandleEventMethod_onNotificationEventHandler() throws JSONException {
        String name = "nameOfTheEvent";
        JSONObject payload = new JSONObject()
                .put("payloadKey", "payloadValue");
        new AppEventCommand(applicationContext, eventHandlerProvider, name, payload).run();

        verify(mockEventHandler).handleEvent(applicationContext, name, payload);
    }

    @Test
    public void testRun_invokeHandleEventMethod_onNotificationEventHandler_whenThereIsNoPayload() throws JSONException {
        String name = "nameOfTheEvent";
        new AppEventCommand(applicationContext, eventHandlerProvider, name, null).run();

        verify(mockEventHandler).handleEvent(applicationContext, name, null);
    }

    @Test
    public void testRun_shouldIgnoreHandler_ifNull() {
        try {
            new AppEventCommand(applicationContext, eventHandlerProvider, "", null).run();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}