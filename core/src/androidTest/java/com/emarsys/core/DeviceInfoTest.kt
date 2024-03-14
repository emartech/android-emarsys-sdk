package com.emarsys.core

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import com.emarsys.core.api.notification.ChannelSettings
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.util.AndroidVersionUtils
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.ApplicationTestUtils.applicationDebug
import com.emarsys.testUtil.ApplicationTestUtils.applicationRelease
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.json.JSONObject
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.*

class DeviceInfoTest : AnnotationSpec() {

    companion object {
        private const val HARDWARE_ID = "hwid"
        private const val SDK_VERSION = "sdkVersion"
        private const val LANGUAGE = "en-US"
        private const val APP_VERSION = "2.0"
    }

    private lateinit var deviceInfo: DeviceInfo
    private lateinit var tz: TimeZone
    private lateinit var context: Context
    private lateinit var mockHardwareIdProvider: HardwareIdProvider
    private lateinit var mockLanguageProvider: LanguageProvider
    private lateinit var mockVersionProvider: VersionProvider
    private lateinit var mockNotificationManagerHelper: NotificationSettings


    @Before
    fun setup() {
        tz = TimeZone.getTimeZone("Asia/Tokyo")
        TimeZone.setDefault(tz)
        context = getTargetContext().applicationContext
        mockHardwareIdProvider = mock {
            on { provideHardwareId() } doReturn HARDWARE_ID
        }
        mockLanguageProvider = mock {
            on { provideLanguage(any()) } doReturn LANGUAGE
        }
        mockVersionProvider = mock {
            on { provideSdkVersion() } doReturn SDK_VERSION
        }
        mockNotificationManagerHelper = mock()

        deviceInfo = DeviceInfo(
                context, mockHardwareIdProvider, mockVersionProvider,
                mockLanguageProvider, mockNotificationManagerHelper,
                isAutomaticPushSendingEnabled = true, isGooglePlayAvailable = true
        )
    }

    @After
    fun teardown() {
        TimeZone.setDefault(null)
    }

    @Test
    fun testConstructor_initializesFields() {
        with(deviceInfo) {
            hardwareId shouldNotBe null
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
        val info = DeviceInfo(
                mockContext, mockHardwareIdProvider, mockVersionProvider,
                mockLanguageProvider, mockNotificationManagerHelper,
                isAutomaticPushSendingEnabled = true, isGooglePlayAvailable = true
        )
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
        val debugDeviceInfo = DeviceInfo(
                mockDebugContext, mockHardwareIdProvider,
                mockVersionProvider, mockLanguageProvider, mockNotificationManagerHelper,
                isAutomaticPushSendingEnabled = true, isGooglePlayAvailable = true
        )
        debugDeviceInfo.isDebugMode.shouldBeTrue()
    }

    @Test
    fun testIsDebugMode_withReleaseApplication() {
        val mockReleaseContext = applicationRelease
        val releaseDeviceInfo = DeviceInfo(
                mockReleaseContext,
                mockHardwareIdProvider,
                mockVersionProvider,
                mockLanguageProvider,
                mockNotificationManagerHelper,
                isAutomaticPushSendingEnabled = true,
                isGooglePlayAvailable = true
        )
        releaseDeviceInfo.isDebugMode.shouldBeFalse()
    }

    @Test
    fun testHardwareId_isAcquiredFromHardwareIdProvider() {
        Mockito.verify(mockHardwareIdProvider).provideHardwareId()
        HARDWARE_ID shouldBe deviceInfo.hardwareId
    }

    @Test
    fun testGetLanguage_isAcquiredFromLanguageProvider() {
        val language = deviceInfo.language
        Mockito.verify(mockLanguageProvider).provideLanguage(Locale.getDefault())
        LANGUAGE shouldBe language
    }

    @Test
    fun testGetDeviceInfoPayload_shouldEqualPayload() {
        val packageName = "packageName"
        val mockContext = Mockito.mock(Context::class.java)
        val packageInfo = PackageInfo()
        val packageManager = Mockito.mock(PackageManager::class.java)
        packageInfo.versionName = APP_VERSION
        whenever(mockContext.contentResolver).thenReturn(getTargetContext().contentResolver)
        whenever(mockContext.packageName).thenReturn(packageName)
        whenever(mockContext.packageManager).thenReturn(packageManager)
        whenever(packageManager.getPackageInfo(packageName, 0)).thenReturn(packageInfo)
        whenever(mockContext.applicationInfo).thenReturn(Mockito.mock(ApplicationInfo::class.java))

        deviceInfo = DeviceInfo(
                mockContext, mockHardwareIdProvider, mockVersionProvider,
                mockLanguageProvider, mockNotificationManagerHelper,
                isAutomaticPushSendingEnabled = true, isGooglePlayAvailable = true
        )

        whenever(mockNotificationManagerHelper.channelSettings).thenReturn(
                listOf(
                        ChannelSettings(
                                channelId = "channelId"
                        )
                )
        )

        var channelSettings = """
        channelSettings: [
            {
                "channelId":"channelId",
                "importance":-1000,
                "isCanBypassDnd":false,
                "isCanShowBadge":false,
                "isShouldVibrate":false
            }
        ]"""

        if (!AndroidVersionUtils.isOreoOrAbove) {
            channelSettings = "channelSettings: [{}]"
        }
        val expectedPayload = JSONObject("""{
                  "notificationSettings": {
                    $channelSettings,
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
                  "sdkVersion": "sdkVersion",
                  "appVersion": "$APP_VERSION" 
                }""").toString()
        deviceInfo.deviceInfoPayload shouldBe expectedPayload
    }

    @Test
    fun testDeviceInfo_platformShouldBeHuawei() {
        val packageName = "packageName"
        val mockContext = Mockito.mock(Context::class.java)
        val packageInfo = PackageInfo()
        val packageManager = Mockito.mock(PackageManager::class.java)
        packageInfo.versionName = APP_VERSION
        whenever(mockContext.contentResolver).thenReturn(getTargetContext().contentResolver)
        whenever(mockContext.packageName).thenReturn(packageName)
        whenever(mockContext.packageManager).thenReturn(packageManager)
        whenever(packageManager.getPackageInfo(packageName, 0)).thenReturn(packageInfo)
        whenever(mockContext.applicationInfo).thenReturn(Mockito.mock(ApplicationInfo::class.java))

        deviceInfo = DeviceInfo(
                mockContext, mockHardwareIdProvider, mockVersionProvider,
                mockLanguageProvider, mockNotificationManagerHelper,
                isAutomaticPushSendingEnabled = true, isGooglePlayAvailable = false
        )

        deviceInfo.platform shouldBe "android-huawei"
    }

    @Test
    fun testGetNotificationSettings() {
        deviceInfo.notificationSettings shouldBe mockNotificationManagerHelper
    }

}