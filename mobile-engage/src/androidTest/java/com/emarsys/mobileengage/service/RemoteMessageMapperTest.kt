package com.emarsys.mobileengage.service

import android.content.Context
import android.os.Build
import androidx.test.filters.SdkSuppress
import com.emarsys.core.api.notification.ChannelSettings
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.resource.MetaDataReader
import com.emarsys.core.util.FileDownloader
import com.emarsys.mobileengage.R
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.inbox.model.NotificationCache
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.RetryUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.copyInputStreamToFile
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers
import java.io.File
import java.util.*

class RemoteMessageMapperTest {
    private companion object {
        const val TITLE = "title"
        const val DEFAULT_TITLE = "emarsys-mobile-engage-android-test"
        const val BODY = "body"
        const val CHANNEL_ID = "channelId"
        const val HARDWARE_ID = "hwid"
        const val SDK_VERSION = "sdkVersion"
        const val LANGUAGE = "en-US"
        const val IMAGE_URL = "https://emarsys.com/image"
        const val HTML_URL = "https://hu.wikipedia.org/wiki/Mont_Blanc"
        const val SMALL_RESOURCE_ID = 123
        const val COLOR_RESOURCE_ID = 456

        const val METADATA_SMALL_NOTIFICATION_ICON_KEY = "com.emarsys.mobileengage.small_notification_icon"
        const val METADATA_NOTIFICATION_COLOR = "com.emarsys.mobileengage.notification_color"
        val DEFAULT_SMALL_NOTIFICATION_ICON = R.drawable.default_small_notification_icon
    }

    private lateinit var context: Context
    private lateinit var deviceInfo: DeviceInfo
    private lateinit var mockMetaDataReader: MetaDataReader
    private lateinit var mockNotificationCache: NotificationCache
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockFileDownloader: FileDownloader
    private lateinit var mockActionCommandFactory: ActionCommandFactory
    private lateinit var remoteMessageMapper: RemoteMessageMapper

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    var retry: TestRule = RetryUtils.retryRule

    @Before
    fun init() {
        context = InstrumentationRegistry.getTargetContext()
        val mockNotificationSettings: NotificationSettings = mock()
        val mockHardwareIdProvider: HardwareIdProvider = mock()
        val mockVersionProvider: VersionProvider = mock()
        val mockLanguageProvider: LanguageProvider = mock()
        val channelSettings = ChannelSettings(channelId = CHANNEL_ID)
        mockFileDownloader = mock<FileDownloader>().apply {
            whenever(download(any())).thenAnswer {
                if (it.arguments[0] == IMAGE_URL || it.arguments[0] == HTML_URL) {
                    val fileContent = InstrumentationRegistry.getTargetContext().resources.openRawResource(
                            InstrumentationRegistry.getTargetContext().resources.getIdentifier("test_image",
                                    "raw", InstrumentationRegistry.getTargetContext().packageName))
                    val file = File(InstrumentationRegistry.getTargetContext().cacheDir.toURI().toURL().path + "/testFile.tmp")
                    file.copyInputStreamToFile(fileContent)
                    file.toURI().toURL().path
                } else {
                    null
                }
            }
        }
        mockActionCommandFactory = mock()
        whenever(mockNotificationSettings.channelSettings).thenReturn(listOf(channelSettings))
        whenever(mockHardwareIdProvider.provideHardwareId()).thenReturn(HARDWARE_ID)
        whenever(mockLanguageProvider.provideLanguage(ArgumentMatchers.any(Locale::class.java))).thenReturn(LANGUAGE)
        whenever(mockVersionProvider.provideSdkVersion()).thenReturn(SDK_VERSION)
        deviceInfo = DeviceInfo(context,
                mockHardwareIdProvider,
                mockVersionProvider,
                mockLanguageProvider,
                mockNotificationSettings,
                true)
        mockMetaDataReader = mock()
        mockNotificationCache = mock()
        mockTimestampProvider = mock()
        whenever(mockTimestampProvider.provideTimestamp()).thenReturn(1L)

        DependencyInjection.setup(FakeMobileEngageDependencyContainer())

        remoteMessageMapper = RemoteMessageMapper(mockMetaDataReader, context, mockFileDownloader, deviceInfo)
    }

    @After
    fun tearDown() {
        DependencyInjection.getContainer<DependencyContainer>().getCoreSdkHandler().looper.quit()
        DependencyInjection.tearDown()
    }

    @Test
    fun testMap() {
        whenever(mockMetaDataReader.getInt(context, METADATA_SMALL_NOTIFICATION_ICON_KEY, DEFAULT_SMALL_NOTIFICATION_ICON))
                .thenReturn(SMALL_RESOURCE_ID)
        whenever(mockMetaDataReader.getInt(context, METADATA_NOTIFICATION_COLOR))
                .thenReturn(COLOR_RESOURCE_ID)

        val ems = JSONObject()
        ems.put("style", "THUMBNAIL")
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["channel_id"] = CHANNEL_ID
        input["ems"] = ems.toString()

        val notificationData = remoteMessageMapper.map(input)

        notificationData.body shouldBe  BODY
        notificationData.title shouldBe  TITLE
        notificationData.channelId shouldBe  CHANNEL_ID
        notificationData.smallIconResourceId shouldBe  SMALL_RESOURCE_ID
        notificationData.colorResourceId shouldBe  COLOR_RESOURCE_ID
        notificationData.style shouldBe "THUMBNAIL"
    }

    @Test
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.LOLLIPOP_MR1)
    fun testMap_whenTitleIsMissing_andVersionBelowMarshmallow() {
        val input: MutableMap<String, String> = HashMap()
        input["body"] = BODY
        input["channel_id"] = CHANNEL_ID

        val notificationData = remoteMessageMapper.map(input)

        notificationData.body shouldBe  BODY
        notificationData.style shouldBe  ""
        notificationData.title shouldBe  DEFAULT_TITLE
        notificationData.channelId shouldBe  CHANNEL_ID
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.M)
    fun testMap_whenTitleIsMissing_andVersionAboveMarshmallow() {
        val input: MutableMap<String, String> = HashMap()
        input["body"] = BODY
        input["channel_id"] = CHANNEL_ID

        val notificationData = remoteMessageMapper.map(input)

        notificationData.body shouldBe  BODY
        notificationData.title shouldBe  null
        notificationData.channelId shouldBe  CHANNEL_ID
    }

    @Test
    fun testMap_whenMapIsEmpty() {
        val input: MutableMap<String, String> = HashMap()

        val notificationData = remoteMessageMapper.map(input)

        notificationData shouldNotBe eq(null)
    }

    @Test
    fun testMap_whenImageIsAvailable() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["image_url"] = IMAGE_URL
        input["channel_id"] = CHANNEL_ID

        val notificationData = remoteMessageMapper.map(input)

        notificationData.image shouldNotBe null
    }

    @Test
    fun testMap_whenImageIsNotAvailable() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["image_url"] = "https://fa.il/img.jpg"
        input["channel_id"] = CHANNEL_ID

        val notificationData = remoteMessageMapper.map(input)

        notificationData.image shouldBe  null
    }

    @Test
    fun testGetTitle_withTitleSet() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        remoteMessageMapper.getTitle(input, context) shouldBe TITLE
    }

    @Test
    fun testGetTitle_shouldReturnAppName_whenTitleNotSet() {
        val input: MutableMap<String, String> = HashMap()
        input["key1"] = "value1"
        input["key2"] = "value2"
        val expectedBefore23 = applicationName
        val expectedFrom23 = null
        val expected = expectedBasedOnApiLevel(expectedBefore23, expectedFrom23)
        remoteMessageMapper.getTitle(input, context) shouldBe expected
    }

    @Test
    fun testGetTitle_shouldReturnAppName_whenTitleIsEmpty() {
        val input: MutableMap<String, String> = HashMap()
        input["key1"] = "value1"
        input["key2"] = "value2"
        input["title"] = ""
        val expectedBefore23 = applicationName
        val expectedFrom23 = null
        val expected = expectedBasedOnApiLevel(expectedBefore23, expectedFrom23)
        remoteMessageMapper.getTitle(input, context) shouldBe expected
    }

    @Test
    fun testGetTitle_defaultTitleShouldNotOverrideTitle() {
        val input: MutableMap<String, String> = HashMap()
        input["key1"] = "value1"
        input["key2"] = "value2"
        input["title"] = TITLE
        input["u"] = "{\"test_field\":\"\",\"ems_default_title\":\"${DEFAULT_TITLE}\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}"

        remoteMessageMapper.getTitle(input, context) shouldBe TITLE
    }


    private fun expectedBasedOnApiLevel(before23: String?, fromApi23: String?): String? {
        return if (Build.VERSION.SDK_INT < 23) {
            before23
        } else {
            fromApi23
        }
    }

    private val applicationName: String
        get() {
            val applicationInfo = context.applicationInfo
            val stringId = applicationInfo.labelRes
            return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(stringId)
        }
}