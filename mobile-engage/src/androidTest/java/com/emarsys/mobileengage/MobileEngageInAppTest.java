package com.emarsys.mobileengage;

import android.support.test.runner.AndroidJUnit4;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MobileEngageInAppTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @After
    public void tearDown() {
        MobileEngage.InApp.setPaused(false);
    }

    @Test
    public void testSetter_paused() {
        MobileEngage.InApp.setPaused(true);

        Assert.assertTrue(MobileEngage.InApp.isPaused());
    }

    @Test
    public void testSetter_resumed() {
        MobileEngage.InApp.setPaused(false);

        Assert.assertFalse(MobileEngage.InApp.isPaused());
    }

}