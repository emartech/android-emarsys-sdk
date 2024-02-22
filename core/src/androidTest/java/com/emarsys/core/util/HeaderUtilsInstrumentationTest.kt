package com.emarsys.core.util;

import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;

public class HeaderUtilsInstrumentationTest {

    private final String username = "user";

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasicAuth_usernameShouldNotBeNull(){
        HeaderUtils.createBasicAuth(null);
    }

    @Test
    public void testCreateBasicAuth_shouldCreateCorrectBasicAuthString() throws Exception {
        String expected = "Basic dXNlcjo=";
        String result = HeaderUtils.createBasicAuth(username);
        assertEquals(expected, result);
    }

}