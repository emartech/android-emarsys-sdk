package com.emarsys.core.util;

import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;

public class HeaderUtilsInstrumentationTest {

    private final String username = "user";
    private final String password = "pass";

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasicAuth_usernameShouldNotBeNull(){
        HeaderUtils.createBasicAuth(null, password);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasicAuth_passwordShouldNotBeNull(){
        HeaderUtils.createBasicAuth(username, null);
    }

    @Test
    public void testCreateBasicAuth_shouldCreateCorrectBasicAuthString() throws Exception {
        String expected = "Basic dXNlcjpwYXNz";
        String result = HeaderUtils.createBasicAuth(username, password);
        assertEquals(expected, result);
    }

}