package com.emarsys.core;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.device.LanguageProvider;
import com.emarsys.core.provider.hardwareid.HardwareIdProvider;
import com.emarsys.core.provider.version.VersionProvider;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceInfoTest {

    private static final String HARDWARE_ID = "hwid";
    private static final String SDK_VERSION = "sdkVersion";
    private static final String LANGUAGE = "en-US";

    private DeviceInfo deviceInfo;
    private TimeZone tz;
    private Context context;
    private HardwareIdProvider hardwareIdProvider;
    private LanguageProvider languageProvider;
    private VersionProvider versionProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        tz = TimeZone.getTimeZone("Asia/Tokyo");
        TimeZone.setDefault(tz);

        context = InstrumentationRegistry.getTargetContext().getApplicationContext();

        hardwareIdProvider = mock(HardwareIdProvider.class);
        languageProvider = mock(LanguageProvider.class);
        versionProvider = mock(VersionProvider.class);

        when(hardwareIdProvider.provideHardwareId()).thenReturn(HARDWARE_ID);
        when(languageProvider.provideLanguage(any(Locale.class))).thenReturn(LANGUAGE);
        when(versionProvider.provideSdkVersion()).thenReturn(SDK_VERSION);
    }

    @After
    public void teardown() {
        TimeZone.setDefault(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_context_mustNotBeNull() {
        new DeviceInfo(null, hardwareIdProvider, versionProvider, languageProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_hardwareIdProvider_mustNotBeNull() {
        new DeviceInfo(context, null, versionProvider, languageProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_versionProvider_mustNotBeNull() {
        new DeviceInfo(context, hardwareIdProvider, null, languageProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_languageProvider_mustNotBeNull() {
        new DeviceInfo(context, hardwareIdProvider, versionProvider, null);
    }

    @Test
    public void testConstructor_initializesFields() {
        deviceInfo = new DeviceInfo(context, hardwareIdProvider, versionProvider, languageProvider);
        assertNotNull(deviceInfo.getHwid());
        assertNotNull(deviceInfo.getPlatform());
        assertNotNull(deviceInfo.getLanguage());
        assertNotNull(deviceInfo.getTimezone());
        assertNotNull(deviceInfo.getManufacturer());
        assertNotNull(deviceInfo.getModel());
        assertNotNull(deviceInfo.getApplicationVersion());
        assertNotNull(deviceInfo.getOsVersion());
        assertNotNull(deviceInfo.getDisplayMetrics());
        assertNotNull(deviceInfo.getSdkVersion());
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

        DeviceInfo info = new DeviceInfo(mockContext, hardwareIdProvider, versionProvider, languageProvider);

        assertEquals(DeviceInfo.UNKNOWN_VERSION_NAME, info.getApplicationVersion());
    }

    @Test
    public void testTimezoneCorrectlyFormatted() {
        deviceInfo = new DeviceInfo(context, hardwareIdProvider, versionProvider, languageProvider);
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

        deviceInfo = new DeviceInfo(context, hardwareIdProvider, versionProvider, languageProvider);

        Locale.setDefault(previous);

        assertEquals("+0900", deviceInfo.getTimezone());
    }

    @Test
    public void testGetDisplayMetrics() {
        deviceInfo = new DeviceInfo(context, hardwareIdProvider, versionProvider, languageProvider);
        assertEquals(deviceInfo.getDisplayMetrics(), Resources.getSystem().getDisplayMetrics());
    }

    @Test
    public void testIsDebugMode_withDebugApplication() {
        Application mockDebugContext = ApplicationTestUtils.getApplicationDebug();

        DeviceInfo debugDeviceInfo = new DeviceInfo(mockDebugContext, hardwareIdProvider, versionProvider, languageProvider);
        assertTrue(debugDeviceInfo.isDebugMode());
    }

    @Test
    public void testIsDebugMode_withReleaseApplication() {
        Application mockReleaseContext = ApplicationTestUtils.getApplicationRelease();

        DeviceInfo releaseDeviceInfo = new DeviceInfo(mockReleaseContext, hardwareIdProvider, versionProvider, languageProvider);
        assertFalse(releaseDeviceInfo.isDebugMode());
    }

    @Test
    public void testHardwareId_isAcquiredFromHardwareIdProvider() {
        deviceInfo = new DeviceInfo(context, hardwareIdProvider, versionProvider, languageProvider);

        verify(hardwareIdProvider).provideHardwareId();
        assertEquals(HARDWARE_ID, deviceInfo.getHwid());
    }

    @Test
    public void testGetLanguage_isAcquiredFromLanguageProvider() {
        deviceInfo = new DeviceInfo(context, hardwareIdProvider, versionProvider, languageProvider);

        String language = deviceInfo.getLanguage();

        verify(languageProvider).provideLanguage(Locale.getDefault());

        assertEquals(LANGUAGE, language);
    }

    @Test
    public void testGetHash_shouldEqualHashCode() {
        deviceInfo = new DeviceInfo(context, hardwareIdProvider, versionProvider, languageProvider);

        Integer expectedHash = deviceInfo.hashCode();

        assertEquals(expectedHash, deviceInfo.getHash());
    }
}