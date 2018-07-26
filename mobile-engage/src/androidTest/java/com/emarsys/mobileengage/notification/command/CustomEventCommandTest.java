package com.emarsys.mobileengage.notification.command;

import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CustomEventCommandTest {

    public static final String EVENT_NAME = "eventName";

    private MobileEngageInternal mock;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setUp() throws Exception {
        mock = mock(MobileEngageInternal.class);

        Field field = MobileEngage.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, mock);
    }

    @After
    public void tearDown() throws Exception {
        Field field = MobileEngage.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_eventName_mustNotBeNull() {
        new CustomEventCommand(null, new HashMap<String, String>());
    }

    @Test
    public void testRun_withEventAttributes() throws Exception {
        HashMap<String, String> eventAttributes = new HashMap<>();
        eventAttributes.put("key", "value");

        Runnable customEventCommand = new CustomEventCommand(EVENT_NAME, eventAttributes);
        customEventCommand.run();

        verify(mock).trackCustomEvent(EVENT_NAME, eventAttributes);
    }

    @Test
    public void testRun_withoutEventAttributes() throws Exception {
        Runnable customEventCommand = new CustomEventCommand(EVENT_NAME, null);
        customEventCommand.run();

        verify(mock).trackCustomEvent(EVENT_NAME, null);
    }

}