package com.emarsys.mobileengage.notification.command;

import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CustomEventCommandTest {

    public static final String EVENT_NAME = "eventName";

    private MobileEngageInternal mockMobileEngageInternal;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setUp() {
        mockMobileEngageInternal = mock(MobileEngageInternal.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mobileEngageInternal_mustNotBeNull() {
        new CustomEventCommand(null, "", new HashMap<String, String>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_eventName_mustNotBeNull() {
        new CustomEventCommand(mockMobileEngageInternal, null, new HashMap<String, String>());
    }

    @Test
    public void testRun_withEventAttributes() {
        HashMap<String, String> eventAttributes = new HashMap<>();
        eventAttributes.put("key", "value");

        Runnable customEventCommand = new CustomEventCommand(mockMobileEngageInternal, EVENT_NAME, eventAttributes);
        customEventCommand.run();

        verify(mockMobileEngageInternal).trackCustomEvent(EVENT_NAME, eventAttributes, null);
    }

    @Test
    public void testRun_withoutEventAttributes() {
        Runnable customEventCommand = new CustomEventCommand(mockMobileEngageInternal, EVENT_NAME, null);
        customEventCommand.run();

        verify(mockMobileEngageInternal).trackCustomEvent(EVENT_NAME, null, null);
    }

}