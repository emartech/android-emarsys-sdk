package com.emarsys.core.concurrency;


import com.emarsys.core.handler.CoreSdkHandler;
import com.emarsys.testUtil.TimeoutUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.*;

public class CoreHandlerProviderTest {

    CoreSdkHandlerProvider provider;
    CoreSdkHandler provided;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        provider = new CoreSdkHandlerProvider();
        provided = provider.provideHandler();
    }

    @After
    public void tearDown() {
        provided.getLooper().quit();
    }

    @Test
    public void testProvideHandler_shouldNotReturnNull() {
        assertNotNull(provided);
    }

    @Test
    public void testProvideHandler_shouldReturnCoreSdkHandler() {
        assertEquals(CoreSdkHandler.class, provided.getClass());
    }

    @Test
    public void testProvideHandler_shouldReturnCoreSdkHandlerWithCorrectName() {
        String expectedNamePrefix = "CoreSDKHandlerThread";

        String actualName = provided.getLooper().getThread().getName();

        assertTrue(actualName.startsWith(expectedNamePrefix));
    }
}