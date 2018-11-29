package com.emarsys.core.resource;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.test.InstrumentationRegistry;

import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetaDataReaderTest {

    private MetaDataReader reader;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setUp() throws Exception {
        reader = new MetaDataReader();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIntOrNull_context_mustNotBeNull() throws Exception {
        reader.getInt(null, "key");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIntOrNull_key_mustNotBeNull() throws Exception {
        reader.getInt(InstrumentationRegistry.getContext(), null);
    }

    @Test
    public void testGetIntOrNull_returnsValue_ifExists() throws PackageManager.NameNotFoundException {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        Bundle bundle = new Bundle();
        bundle.putInt("something", 42);
        applicationInfo.metaData = bundle;

        Context context = mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
        when(context.getPackageManager().getApplicationInfo(nullable(String.class), anyInt())).thenReturn(applicationInfo);

        assertEquals(42, reader.getInt(context, "something"));
    }

    @Test
    public void getIntOrNull_shouldReturnNull_ifThereIsNoValue() throws PackageManager.NameNotFoundException {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.metaData = new Bundle();

        Context context = mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
        when(context.getPackageManager().getApplicationInfo(nullable(String.class), anyInt())).thenReturn(applicationInfo);

        assertEquals(0, reader.getInt(context, "something"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInt_context_mustNotBeNull() throws Exception {
        reader.getInt(null, "key", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInt_key_mustNotBeNull() throws Exception {
        reader.getInt(InstrumentationRegistry.getContext(), null, 0);
    }

    @Test
    public void testGetInt_returnsValue_ifExists() throws PackageManager.NameNotFoundException {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        Bundle bundle = new Bundle();
        bundle.putInt("something", 43);
        applicationInfo.metaData = bundle;

        Context context = mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
        when(context.getPackageManager().getApplicationInfo(nullable(String.class), anyInt())).thenReturn(applicationInfo);

        assertEquals(43, reader.getInt(context, "something", -1));
    }

    @Test
    public void getInt_shouldReturnDefaultValue_ifThereIsNoValue() throws PackageManager.NameNotFoundException {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.metaData = new Bundle();

        Context context = mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
        when(context.getPackageManager().getApplicationInfo(nullable(String.class), anyInt())).thenReturn(applicationInfo);

        assertEquals(0, reader.getInt(context, "something"));
        assertEquals(200, reader.getInt(context, "something", 200));
    }
}