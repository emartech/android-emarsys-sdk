package com.emarsys.mobileengage.util;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.*;

public class RequestUrlUtilsTest {
    public static final String VALID_CUSTOM_EVENT_V3 = "https://mobile-events.eservice.emarsys.net/v3/devices/12345/events";

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventUrl_V2_shouldNotAcceptNull() {
        RequestUrlUtils.createEventUrl_V2(null);
    }

    @Test
    public void testCreateEventUrl_V2_shouldReturnTheCorrectEventUrl() {
        String url = RequestUrlUtils.createEventUrl_V2("my-custom-event");
        String expected = "https://push.eservice.emarsys.net/api/mobileengage/v2/events/my-custom-event";
        assertEquals(expected, url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventUrl_V3_meIdShouldNotBeNull() {
        RequestUrlUtils.createEventUrl_V3(null);
    }

    @Test
    public void testCreateEventUrl_V3_shouldReturnTheCorrectEventUrl() {
        String url = RequestUrlUtils.createEventUrl_V3("meId");
        String expected = "https://mobile-events.eservice.emarsys.net/v3/devices/meId/events";
        assertEquals(expected, url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsCustomEvent_V3_mustNotBeNull() {
        RequestUrlUtils.isCustomEvent_V3((String) null);
    }

    @Test
    public void testIsCustomEvent_V3_returnsTrue_ifIndeedV3Event() {
        assertTrue(RequestUrlUtils.isCustomEvent_V3(VALID_CUSTOM_EVENT_V3));
    }

    @Test
    public void testIsCustomEvent_V3_matchesWholeString() {
        assertFalse(RequestUrlUtils.isCustomEvent_V3("prefix" + VALID_CUSTOM_EVENT_V3 + "suffix"));
    }

    @Test
    public void testIsCustomEvent_V3_returnsFalse_ifThereIsNoMatch() {
        assertFalse(RequestUrlUtils.isCustomEvent_V3("https://www.google.com"));
    }
}