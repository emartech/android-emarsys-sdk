package com.emarsys.core;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;

import com.emarsys.testUtil.ApplicationTestUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Locale;
import java.util.TimeZone;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceInfoTest {
    DeviceInfo deviceInfo;
    TimeZone tz;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        tz = TimeZone.getTimeZone("Asia/Tokyo");
        TimeZone.setDefault(tz);
        deviceInfo = new DeviceInfo(InstrumentationRegistry.getTargetContext());
    }

    @After
    public void teardown() {
        TimeZone.setDefault(null);
    }

    @Test
    public void testConstructor_initializesFields() {
        assertNotNull(deviceInfo.getHwid());
        assertNotNull(deviceInfo.getPlatform());
        assertNotNull(deviceInfo.getLanguage());
        assertNotNull(deviceInfo.getTimezone());
        assertNotNull(deviceInfo.getManufacturer());
        assertNotNull(deviceInfo.getModel());
        assertNotNull(deviceInfo.getApplicationVersion());
        assertNotNull(deviceInfo.getOsVersion());
        assertNotNull(deviceInfo.getDisplayMetrics());
    }

    @Test
    public void testGetApplicationVersion_shouldBeDefault_whenVersionInPackageInfo_isNull() throws PackageManager.NameNotFoundException {
        String packageName = "packageName";
        Context mockContext = mock(Context.class);
        PackageInfo packageInfo = new PackageInfo();
        PackageManager packageManager = mock(PackageManager.class);
        packageInfo.versionName = null;

        when(mockContext.getContentResolver()).thenReturn(InstrumentationRegistry.getTargetContext().getContentResolver());
        when(mockContext.getPackageName()).thenReturn(packageName);
        when(mockContext.getPackageManager()).thenReturn(packageManager);
        when(packageManager.getPackageInfo(packageName, 0)).thenReturn(packageInfo);
        when(mockContext.getApplicationInfo()).thenReturn(mock(ApplicationInfo.class));

        DeviceInfo info = new DeviceInfo(mockContext);

        assertEquals(DeviceInfo.UNKNOWN_VERSION_NAME, info.getApplicationVersion());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_contextShouldNotBeNull() {
        new DeviceInfo(null);
    }

    @Test
    public void test_timezoneCorrectlyFormatted() {
        assertEquals("+0900", deviceInfo.getTimezone());
    }

    @Test
    public void test_timezoneCorrectlyFormatted_withArabicLocale() {
        Locale previous = Locale.getDefault();

        Locale locale = new Locale("my");
        Resources resources = InstrumentationRegistry.getTargetContext().getResources();
        Locale.setDefault(locale);
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        deviceInfo = new DeviceInfo(InstrumentationRegistry.getTargetContext());

        Locale.setDefault(previous);

        assertEquals("+0900", deviceInfo.getTimezone());
    }

    @Test
    public void test_getDisplayMetrics() {
        assertEquals(deviceInfo.getDisplayMetrics(), Resources.getSystem().getDisplayMetrics());
    }

    @Test
    public void testIsDebugMode_withDebugApplication() throws PackageManager.NameNotFoundException {
        Application mockDebugContext = ApplicationTestUtils.getApplicationDebug();
        when(mockDebugContext.getPackageManager().getPackageInfo(anyString(), anyInt())).thenReturn(new PackageInfo());

        DeviceInfo debugDeviceInfo = new DeviceInfo(mockDebugContext);
        assertTrue(debugDeviceInfo.isDebugMode());
    }

    @Test
    public void testIsDebugMode_withReleaseApplication() throws PackageManager.NameNotFoundException {
        Application mockReleaseContext = ApplicationTestUtils.getApplicationRelease();
        when(mockReleaseContext.getPackageManager().getPackageInfo(anyString(), anyInt())).thenReturn(new PackageInfo());

        DeviceInfo releaseDeviceInfo = new DeviceInfo(mockReleaseContext);
        assertFalse(releaseDeviceInfo.isDebugMode());
    }
}