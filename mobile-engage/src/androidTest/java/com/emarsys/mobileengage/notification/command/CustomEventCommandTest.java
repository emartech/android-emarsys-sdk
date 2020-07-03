package com.emarsys.mobileengage.notification.command;

import com.emarsys.mobileengage.event.EventServiceInternal;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CustomEventCommandTest {

    private static final String EVENT_NAME = "eventName";

    private EventServiceInternal mockEventServiceInternal;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setUp() {
        mockEventServiceInternal = mock(EventServiceInternal.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mockEventServiceInternal_mustNotBeNull() {
        new CustomEventCommand(null, "", new HashMap<String, String>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_eventName_mustNotBeNull() {
        new CustomEventCommand(mockEventServiceInternal, null, new HashMap<String, String>());
    }

    @Test
    public void testRun_withEventAttributes() {
        HashMap<String, String> eventAttributes = new HashMap<>();
        eventAttributes.put("key", "value");

        Runnable customEventCommand = new CustomEventCommand(mockEventServiceInternal, EVENT_NAME, eventAttributes);
        customEventCommand.run();

        verify(mockEventServiceInternal).trackCustomEventAsync(EVENT_NAME, eventAttributes, null);
    }

    @Test
    public void testRun_withoutEventAttributes() {
        Runnable customEventCommand = new CustomEventCommand(mockEventServiceInternal, EVENT_NAME, null);
        customEventCommand.run();

        verify(mockEventServiceInternal).trackCustomEventAsync(EVENT_NAME, null, null);
    }

}