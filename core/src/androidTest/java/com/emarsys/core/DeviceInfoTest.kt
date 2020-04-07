package com.emarsys.core

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import com.emarsys.core.api.notification.ChannelSettings
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.testUtil.ApplicationTestUtils.applicationDebug
import com.emarsys.testUtil.ApplicationTestUtils.applicationRelease
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.*

class DeviceInfoTest {

    companion object {
        private const val HARDWARE_ID = "hwid"
        private const val SDK_VERSION = "sdkVersion"
        private const val LANGUAGE = "en-US"
    }

    private lateinit var deviceInfo: DeviceInfo
    private lateinit var tz: TimeZone
    private lateinit var context: Context
    private lateinit var mockHardwareIdProvider: HardwareIdProvider
    private lateinit var mockLanguageProvider: LanguageProvider
    private lateinit var mockVersionProvider: VersionProvider
    private lateinit var mockNotificationManagerHelper: NotificationSettings

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setup() {
        tz = TimeZone.getTimeZone("Asia/Tokyo")
        TimeZone.setDefault(tz)
        context = getTargetContext().applicationContext
        mockHardwareIdProvider = Mockito.mock(HardwareIdProvider::class.java)
        mockLanguageProvider = Mockito.mock(LanguageProvider::class.java)
        mockVersionProvider = Mockito.mock(VersionProvider::class.java)
        mockNotificationManagerHelper = Mockito.mock(NotificationSettings::class.java)
        whenever(mockHardwareIdProvider.provideHardwareId()).thenReturn(HARDWARE_ID)
        whenever(mockLanguageProvider.provideLanguage(ArgumentMatchers.any(Locale::class.java))).thenReturn(LANGUAGE)
        whenever(mockVersionProvider.provideSdkVersion()).thenReturn(SDK_VERSION)
        deviceInfo = DeviceInfo(context, mockHardwareIdProvider, mockVersionProvider,
                mockLanguageProvider, mockNotificationManagerHelper, true)
    }

    @After
    fun teardown() {
        TimeZone.setDefault(null)
    }

    @Test
    fun testConstructor_initializesFields() {
        with(deviceInfo) {
            hwid shouldNotBe null
            platform shouldNotBe null
            language shouldNotBe null
            timezone shouldNotBe null
            manufacturer shouldNotBe null
            model shouldNotBe null
            applicationVersion shouldNotBe null
            osVersion shouldNotBe null
            displayMetrics shouldNotBe null
            sdkVersion shouldNotBe null
            notificationSettings shouldNotBe null
        }
    }

    @Test
    @Throws(PackageManager.NameNotFoundException::class)
    fun testGetApplicationVersion_shouldBeDefault_whenVersionInPackageInfo_isNull() {
        val packageName = "packageName"
        val mockContext = Mockito.mock(Context::class.java)
        val packageInfo = PackageInfo()
        val packageManager = Mockito.mock(PackageManager::class.java)
        packageInfo.versionName = null
        whenever(mockContext.contentResolver).thenReturn(getTargetContext().contentResolver)
        whenever(mockContext.packageName).thenReturn(packageName)
        whenever(mockContext.packageManager).thenReturn(packageManager)
        whenever(packageManager.getPackageInfo(packageName, 0)).thenReturn(packageInfo)
        whenever(mockContext.applicationInfo).thenReturn(Mockito.mock(ApplicationInfo::class.java))
        val info = DeviceInfo(mockContext, mockHardwareIdProvider, mockVersionProvider,
                mockLanguageProvider, mockNotificationManagerHelper, true)
        info.applicationVersion shouldBe DeviceInfo.UNKNOWN_VERSION_NAME
    }

    @Test
    fun testTimezoneCorrectlyFormatted() {
        "+0900" shouldBe deviceInfo.timezone
    }

    @Test
    fun testTimezoneCorrectlyFormatted_withArabicLocale() {
        val previous = Locale.getDefault()
        val locale = Locale("my")
        val resources = getTargetContext().resources
        Locale.setDefault(locale)
        val config = resources.configuration
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
        Locale.setDefault(previous)
        "+0900" shouldBe deviceInfo.timezone
    }

    @Test
    fun testGetDisplayMetrics() {
        deviceInfo.displayMetrics shouldBe Resources.getSystem().displayMetrics
    }

    @Test
    fun testIsDebugMode_withDebugApplication() {
        val mockDebugContext = applicationDebug
        val debugDeviceInfo = DeviceInfo(mockDebugContext, mockHardwareIdProvider,
                mockVersionProvider, mockLanguageProvider, mockNotificationManagerHelper, true)
        debugDeviceInfo.isDebugMode.shouldBeTrue()
    }

    @Test
    fun testIsDebugMode_withReleaseApplication() {
        val mockReleaseContext = applicationRelease
        val releaseDeviceInfo = DeviceInfo(mockReleaseContext, mockHardwareIdProvider, mockVersionProvider, mockLanguageProvider, mockNotificationManagerHelper, true)
        releaseDeviceInfo.isDebugMode.shouldBeFalse()
    }

    @Test
    fun testHardwareId_isAcquiredFromHardwareIdProvider() {
        Mockito.verify(mockHardwareIdProvider).provideHardwareId()
        HARDWARE_ID shouldBe deviceInfo.hwid
    }

    @Test
    fun testGetLanguage_isAcquiredFromLanguageProvider() {
        val language = deviceInfo.language
        Mockito.verify(mockLanguageProvider).provideLanguage(Locale.getDefault())
        LANGUAGE shouldBe language
    }

    @Test
    fun testGetDeviceInfoPayload_shouldEqualPayload() {
        whenever(mockNotificationManagerHelper.channelSettings).thenReturn(listOf(ChannelSettings(channelId = "channelId")))

        val expectedPayload = JSONObject("""{
                  "notificationSettings": {
                    "channelSettings": [
                     {
                     "channelId":"channelId",
                     "importance":-1000,
                     "isCanBypassDnd":false,
                     "isCanShowBadge":false,
                     "isShouldVibrate":false}
                    ],
                    "importance": 0,
                    "areNotificationsEnabled": false
                  },
                  "hwid": "hwid",
                  "platform": "android",
                  "language": "en-US",
                  "timezone": "+0900",
                  "manufacturer": "${Build.MANUFACTURER}",
                  "model": "${Build.MODEL}",
                  "osVersion": "${Build.VERSION.RELEASE}",
                  "displayMetrics": "${Resources.getSystem().displayMetrics.widthPixels}x${Resources.getSystem().displayMetrics.heightPixels}",
                  "sdkVersion": "sdkVersion"
                }""").toString()
        deviceInfo.deviceInfoPayload shouldBe expectedPayload
    }

    @Test
    fun testGetNotificationSettings() {
        deviceInfo.notificationSettings shouldBe mockNotificationManagerHelper
    }

}