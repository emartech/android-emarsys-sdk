package com.emarsys;

import android.support.test.runner.AndroidJUnit4;

import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.HashMap;

@RunWith(AndroidJUnit4.class)
public class EmarsysTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testTrackCustomEvent_eventNameMustNotBeNull() {
        Emarsys.trackCustomEvent(null, new HashMap<String, String>());
    }
}
