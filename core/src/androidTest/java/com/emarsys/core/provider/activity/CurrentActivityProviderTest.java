package com.emarsys.core.provider.activity;

import android.app.Activity;

import com.emarsys.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;

public class CurrentActivityProviderTest {

    static {
        mock(Activity.class);
    }

    private CurrentActivityProvider provider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();


    @Before
    public void init() {
        provider = new CurrentActivityProvider();
    }

    @Test
    public void testGet_returnsNullByDefault() {
        Assert.assertEquals(null, provider.get());
    }

    @Test
    public void testGet_returnsSetValue() {
        Activity activity = mock(Activity.class);

        provider.set(activity);

        Assert.assertSame(activity, provider.get());
    }

    @Test
    public void testSet_overridesPreviousValue() {
        Activity activity1 = mock(Activity.class);
        Activity activity2 = mock(Activity.class);

        provider.set(activity1);
        provider.set(activity2);

        Assert.assertSame(activity2, provider.get());
    }

    @Test
    public void testSet_null_clearsPreviousValue() {
        provider.set(mock(Activity.class));
        provider.set(null);

        Assert.assertEquals(null, provider.get());
    }

}