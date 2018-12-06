package com.emarsys.core;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.emarsys.core.provider.hardwareid.HardwareIdProvider;
import com.emarsys.testUtil.ApplicationTestUtils;
import com.emarsys.testUtil.InstrumentationRegistry;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceInfoTest {

    private static final String HARDWARE_ID = "hwid";

    private DeviceInfo deviceInfo;
    private TimeZone tz;
    private Context context;
    private HardwareIdProvider hardwareIdProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        tz = TimeZone.getTimeZone("Asia/Tokyo");
        TimeZone.setDefault(tz);

        context = InstrumentationRegistry.getTargetContext().getApplicationContext();

        hardwareIdProvider = mock(HardwareIdProvider.class);
        when(hardwareIdProvider.provideHardwareId()).thenReturn(HARDWARE_ID);
    }

    @After
    public void teardown() {
        TimeZone.setDefault(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_context_mustNotBeNull() {
        new DeviceInfo(null, hardwareIdProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_hardwareIdProvider_mustNotBeNull() {
        new DeviceInfo(context, null);
    }

    @Test
    public void testConstructor_initializesFields() {
        deviceInfo = new DeviceInfo(context, hardwareIdProvider);
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

        DeviceInfo info = new DeviceInfo(mockContext, hardwareIdProvider);

        assertEquals(DeviceInfo.UNKNOWN_VERSION_NAME, info.getApplicationVersion());
    }

    @Test
    public void testTimezoneCorrectlyFormatted() {
        deviceInfo = new DeviceInfo(context, hardwareIdProvider);
        assertEquals("+0900", deviceInfo.getTimezone());
    }

    @Test
    public void testTimezoneCorrectlyFormatted_withArabicLocale() {
        Locale previous = Locale.getDefault();

        Locale locale = new Locale("my");
        Resources resources = InstrumentationRegistry.getTargetContext().getResources();
        Locale.setDefault(locale);
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        deviceInfo = new DeviceInfo(context, hardwareIdProvider);

        Locale.setDefault(previous);

        assertEquals("+0900", deviceInfo.getTimezone());
    }

    @Test
    public void testGetDisplayMetrics() {
        deviceInfo = new DeviceInfo(context, hardwareIdProvider);
        assertEquals(deviceInfo.getDisplayMetrics(), Resources.getSystem().getDisplayMetrics());
    }

    @Test
    public void testIsDebugMode_withDebugApplication() {
        Application mockDebugContext = ApplicationTestUtils.getApplicationDebug();

        DeviceInfo debugDeviceInfo = new DeviceInfo(mockDebugContext, hardwareIdProvider);
        assertTrue(debugDeviceInfo.isDebugMode());
    }

    @Test
    public void testIsDebugMode_withReleaseApplication() {
        Application mockReleaseContext = ApplicationTestUtils.getApplicationRelease();

        DeviceInfo releaseDeviceInfo = new DeviceInfo(mockReleaseContext, hardwareIdProvider);
        assertFalse(releaseDeviceInfo.isDebugMode());
    }

    @Test
    public void testHardwareId_isAcquiredFromHardwareIdProvider() {
        deviceInfo = new DeviceInfo(context, hardwareIdProvider);

        verify(hardwareIdProvider).provideHardwareId();
        assertEquals(HARDWARE_ID, deviceInfo.getHwid());
    }
}