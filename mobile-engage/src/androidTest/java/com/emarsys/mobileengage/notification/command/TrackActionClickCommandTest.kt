package com.emarsys.mobileengage.notification.command;

import com.emarsys.mobileengage.event.EventServiceInternal;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TrackActionClickCommandTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_eventServiceInternal_mustNotBeNull() {
        new TrackActionClickCommand(null, "", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_buttonId_mustNotBeNull() {
        new TrackActionClickCommand(mock(EventServiceInternal.class), null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_sid_mustNotBeNull() {
        new TrackActionClickCommand(mock(EventServiceInternal.class), "", null);
    }

    @Test
    public void testRun_sendsInternalCustomEvent() {
        EventServiceInternal internalMock = mock(EventServiceInternal.class);
        String buttonId = "buttonId";
        String sid = "sid1234";

        new TrackActionClickCommand(internalMock, buttonId, sid).run();

        Map<String, String> payload = new HashMap<>();
        payload.put("button_id", buttonId);
        payload.put("origin", "button");
        payload.put("sid", sid);
        verify(internalMock).trackInternalCustomEventAsync("push:click", payload, null);
    }

}