package com.emarsys.mobileengage;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;
import com.emarsys.mobileengage.util.MobileEngageIdlingResource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class MobileEngageUtilsTest {
    private MobileEngageConfig disabledConfig;
    private MobileEngageConfig enabledConfig;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        Application application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        MobileEngageConfig baseConfig = new MobileEngageConfig.Builder()
                .application(application)
                .credentials("applicationCode", "applicationPassword")
                .disableDefaultChannel()
                .build();

        enabledConfig = new MobileEngageConfig.Builder()
                .from(baseConfig)
                .enableIdlingResource(true)
                .build();

        disabledConfig = new MobileEngageConfig.Builder()
                .from(baseConfig)
                .enableIdlingResource(false)
                .build();
    }

    @Test
    public void testSetup_disabled() {
        MobileEngageUtils.idlingResource = new MobileEngageIdlingResource("dummy");
        MobileEngageUtils.idlingResourceEnabled = true;

        MobileEngageUtils.setup(disabledConfig);

        assertFalse(MobileEngageUtils.idlingResourceEnabled);
        assertNull(MobileEngageUtils.getIdlingResource());
    }

    @Test
    public void testSetup_enabled() {
        MobileEngageUtils.idlingResource = null;
        MobileEngageUtils.idlingResourceEnabled = false;

        MobileEngageUtils.setup(enabledConfig);

        assertTrue(MobileEngageUtils.idlingResourceEnabled);
        assertNotNull(MobileEngageUtils.getIdlingResource());
    }

    @Test
    public void testIncrementIdlingResource_enabled(){
        MobileEngageUtils.setup(enabledConfig);
        MobileEngageUtils.idlingResource = mock(MobileEngageIdlingResource.class);

        MobileEngageUtils.incrementIdlingResource();

        verify(MobileEngageUtils.idlingResource, times(1)).increment();

    }

    @Test
    public void testIncrementIdlingResource_disabled(){
        MobileEngageUtils.setup(disabledConfig);
        MobileEngageUtils.idlingResource = mock(MobileEngageIdlingResource.class);

        MobileEngageUtils.incrementIdlingResource();

        verifyZeroInteractions(MobileEngageUtils.idlingResource);
    }

    @Test
    public void testDecrementIdlingResource_enabled(){
        MobileEngageUtils.setup(enabledConfig);
        MobileEngageUtils.idlingResource = mock(MobileEngageIdlingResource.class);

        MobileEngageUtils.decrementIdlingResource();

        verify(MobileEngageUtils.idlingResource, times(1)).decrement();
    }

    @Test
    public void testDecrementIdlingResource_disabled(){
        MobileEngageUtils.setup(disabledConfig);
        MobileEngageUtils.idlingResource = mock(MobileEngageIdlingResource.class);

        MobileEngageUtils.decrementIdlingResource();

        verifyZeroInteractions(MobileEngageUtils.idlingResource);
    }
}