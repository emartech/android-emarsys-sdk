package com.emarsys.core

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import com.emarsys.core.api.notification.ChannelSettings
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.provider.clientid.ClientIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.util.AndroidVersionUtils
import com.emarsys.testUtil.ApplicationTestUtils.applicationDebug
import com.emarsys.testUtil.ApplicationTestUtils.applicationRelease
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*

class DeviceInfoTest {

    companion object {
        private const val CLIENT_ID = "hwid"
        private const val SDK_VERSION = "sdkVersion"
        private const val LANGUAGE = "en-US"
        private const val APP_VERSION = "2.0"
        private const val PACKAGE_NAME = "packageName"
    }

    private lateinit var deviceInfo: DeviceInfo
    private lateinit var tz: TimeZone
    private lateinit var context: Context
    private lateinit var mockClientIdProvider: ClientIdProvider
    private lateinit var mockLanguageProvider: LanguageProvider
    private lateinit var mockVersionProvider: VersionProvider
    private lateinit var mockNotificationManagerHelper: NotificationSettings
    private lateinit var mockContext: Context
    private lateinit var mockPackageManager: PackageManager


    @Before
    fun setup() {
        tz = TimeZone.getTimeZone("Asia/Tokyo")
        TimeZone.setDefault(tz)
        context = getTargetContext().applicationContext
        mockClientIdProvider = mockk(relaxed = true)
        every { mockClientIdProvider.provideClientId() } returns CLIENT_ID

        mockLanguageProvider = mockk(relaxed = true)
        every { mockLanguageProvider.provideLanguage(any()) } returns LANGUAGE

        mockVersionProvider = mockk(relaxed = true)
        every { mockVersionProvider.provideSdkVersion() } returns SDK_VERSION

        mockNotificationManagerHelper = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)
        mockPackageManager = mockk(relaxed = true)

        every { mockContext.contentResolver } returns getTargetContext().contentResolver
        every { mockContext.packageName } returns PACKAGE_NAME
        every { mockContext.packageManager } returns mockPackageManager

        deviceInfo = DeviceInfo(
            context, mockClientIdProvider, mockVersionProvider,
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
            clientId shouldNotBe null
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
        val packageInfo = PackageInfo()
        packageInfo.versionName = null

        every { mockContext.applicationInfo } returns mockk(relaxed = true)
        val info = DeviceInfo(
            mockContext, mockClientIdProvider, mockVersionProvider,
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
        config.setLocale(locale)
        context.createConfigurationContext(config)
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
            mockDebugContext, mockClientIdProvider,
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
            mockClientIdProvider,
            mockVersionProvider,
            mockLanguageProvider,
            mockNotificationManagerHelper,
            isAutomaticPushSendingEnabled = true,
            isGooglePlayAvailable = true
        )
        releaseDeviceInfo.isDebugMode.shouldBeFalse()
    }

    @Test
    fun testClientId_isAcquiredFromClientIdProvider() {
        verify { mockClientIdProvider.provideClientId() }
        CLIENT_ID shouldBe deviceInfo.clientId
    }

    @Test
    fun testGetLanguage_isAcquiredFromLanguageProvider() {
        val language = deviceInfo.language
        verify { mockLanguageProvider.provideLanguage(Locale.getDefault()) }
        LANGUAGE shouldBe language
    }

    @Test
    fun testGetDeviceInfoPayload_shouldEqualPayload() {
        val packageInfo = PackageInfo()
        packageInfo.versionName = APP_VERSION
        every { mockPackageManager.getPackageInfo(PACKAGE_NAME, 0) } returns packageInfo

        deviceInfo = DeviceInfo(
            mockContext, mockClientIdProvider, mockVersionProvider,
            mockLanguageProvider, mockNotificationManagerHelper,
            isAutomaticPushSendingEnabled = true, isGooglePlayAvailable = true
        )

        every { mockNotificationManagerHelper.channelSettings } returns listOf(
            ChannelSettings(
                channelId = "channelId"
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
        val expectedPayload = JSONObject(
            """{
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
                }"""
        ).toString()
        deviceInfo.deviceInfoPayload shouldBe expectedPayload
    }

    @Test
    fun testDeviceInfo_platformShouldBeHuawei() {
        val packageInfo = PackageInfo()
        packageInfo.versionName = APP_VERSION
        every { mockPackageManager.getPackageInfo(PACKAGE_NAME, 0) } returns packageInfo

        deviceInfo = DeviceInfo(
            mockContext, mockClientIdProvider, mockVersionProvider,
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