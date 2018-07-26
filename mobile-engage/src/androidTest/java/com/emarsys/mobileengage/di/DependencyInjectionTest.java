package com.emarsys.mobileengage.di;

import com.emarsys.mobileengage.testUtil.ReflectionTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.Assert.fail;
import static org.mockito.Mockito.mock;

public class DependencyInjectionTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() throws Exception {
        ReflectionTestUtils.setStaticField(DependencyInjection.class, "container", null);
    }

    @After
    public void tearDown() throws Exception {
        ReflectionTestUtils.setStaticField(DependencyInjection.class, "container", null);
    }

    @Test
    public void testSetup_initializesContainer() {
        DependencyContainer mock = mock(DependencyContainer.class);
        DependencyInjection.setup(mock);

        DependencyContainer result = DependencyInjection.getContainer();

        Assert.assertSame(mock, result);
    }

    @Test
    public void testSetup_ignoresMultipleSetups() {
        DependencyContainer mock = mock(DependencyContainer.class);
        DependencyContainer mock2 = mock(DependencyContainer.class);
        DependencyInjection.setup(mock);
        DependencyInjection.setup(mock2);

        DependencyContainer result = DependencyInjection.getContainer();

        Assert.assertSame(mock, result);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetContainer_shouldThrowException_whenContainerIsNotInitialized() {
        DependencyInjection.getContainer();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetContainer_shouldThrowException_afterReset() {
        DependencyContainer mock = mock(DependencyContainer.class);
        DependencyInjection.setup(mock);

        try {
            DependencyInjection.getContainer();
        } catch (IllegalStateException ise) {
            fail();
        }

        DependencyInjection.tearDown();

        DependencyInjection.getContainer();
    }

}