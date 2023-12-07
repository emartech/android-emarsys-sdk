package com.emarsys.mobileengage.service

import android.content.Context
import com.emarsys.core.api.notification.ChannelSettings
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.resource.MetaDataReader
import com.emarsys.core.util.FileDownloader
import com.emarsys.mobileengage.R
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.RetryUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.copyInputStreamToFile
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import java.util.Locale

class RemoteMessageMapperV1Test {
    private companion object {
        const val TITLE = "title"
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
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockFileDownloader: FileDownloader
    private lateinit var mockActionCommandFactory: ActionCommandFactory
    private lateinit var remoteMessageMapperV1: RemoteMessageMapperV1

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
            whenever(download(any(), any())).thenAnswer {
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
        deviceInfo = DeviceInfo(
                context,
                mockHardwareIdProvider,
                mockVersionProvider,
                mockLanguageProvider,
                mockNotificationSettings,
                isAutomaticPushSendingEnabled = true,
                isGooglePlayAvailable = true
        )
        mockMetaDataReader = mock()
        mockTimestampProvider = mock()
        whenever(mockTimestampProvider.provideTimestamp()).thenReturn(1L)

        setupMobileEngageComponent(FakeMobileEngageDependencyContainer())

        val uuidProvider: UUIDProvider = mock {
            on { provideId() }.thenReturn("testUUID")
        }

        remoteMessageMapperV1 = RemoteMessageMapperV1(
            mockMetaDataReader,
            context,
            mockFileDownloader,
            deviceInfo,
            uuidProvider
        )
    }

    @After
    fun tearDown() {
        mobileEngage().concurrentHandlerHolder.coreLooper.quitSafely()
        tearDownMobileEngageComponent()
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

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.body shouldBe  BODY
        notificationData.title shouldBe  TITLE
        notificationData.channelId shouldBe  CHANNEL_ID
        notificationData.smallIconResourceId shouldBe  SMALL_RESOURCE_ID
        notificationData.colorResourceId shouldBe  COLOR_RESOURCE_ID
        notificationData.style shouldBe "THUMBNAIL"
    }

    @Test
    fun testMap_whenTitleIsMissing() {
        val input: MutableMap<String, String> = HashMap()
        input["body"] = BODY
        input["channel_id"] = CHANNEL_ID

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.body shouldBe  BODY
        notificationData.title shouldBe  null
        notificationData.channelId shouldBe  CHANNEL_ID
    }

    @Test
    fun testMap_whenMapIsEmpty() {
        val input: MutableMap<String, String> = HashMap()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData shouldNotBe eq(null)
    }

    @Test
    fun testMap_whenImageIsAvailable() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["image_url"] = IMAGE_URL
        input["channel_id"] = CHANNEL_ID

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.image shouldNotBe null
    }

    @Test
    fun testMap_whenImageIsNotAvailable() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["image_url"] = "https://fa.il/img.jpg"
        input["channel_id"] = CHANNEL_ID

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.image shouldBe  null
    }

    @Test
    fun testMap_whenNotificationMethodIsSet() {
        val notificationMethod = NotificationMethod("testNotificationId", NotificationOperation.UPDATE)

        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["image_url"] = IMAGE_URL
        input["channel_id"] = CHANNEL_ID
        val notificationMethodJson = JSONObject()
        notificationMethodJson.put("collapseId", "testNotificationId")
        notificationMethodJson.put("operation", "UPDATE")
        val ems = JSONObject()
        ems.put("notificationMethod", notificationMethodJson)
        input["ems"] = ems.toString()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.notificationMethod shouldBe notificationMethod
    }

    @Test
    fun testMap_whenNotificationMethodIsMissing() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["image_url"] = IMAGE_URL
        input["channel_id"] = CHANNEL_ID
        val ems = JSONObject()
        input["ems"] = ems.toString()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.notificationMethod.operation shouldBe NotificationOperation.INIT
    }

    @Test
    fun testMap_whenNotificationMethodIsSet_withoutCollapseID_shouldReturnWithInitOperation() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["image_url"] = IMAGE_URL
        input["channel_id"] = CHANNEL_ID
        val ems = JSONObject()
        val notificationMethodJson = JSONObject()
        notificationMethodJson.put("operation", "UPDATE")
        ems.put("notificationMethod", notificationMethodJson)
        input["ems"] = ems.toString()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.notificationMethod.operation shouldBe NotificationOperation.INIT
    }

    @Test
    fun testMap_whenNotificationMethodIsSet_withCollapseID_shouldReturnWithSetOperation() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["image_url"] = IMAGE_URL
        input["channel_id"] = CHANNEL_ID
        val ems = JSONObject()
        val notificationMethodJson = JSONObject()
        notificationMethodJson.put("collapseId", 123)
        notificationMethodJson.put("operation", "UPDATE")
        ems.put("notificationMethod", notificationMethodJson)
        input["ems"] = ems.toString()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.notificationMethod.operation shouldBe NotificationOperation.UPDATE
    }

    @Test
    fun testMap_whenNotificationMethodIsSet_withCollapseID_shouldReturnWithInitOperation_whenOperationIsMissing() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["image_url"] = IMAGE_URL
        input["channel_id"] = CHANNEL_ID
        val ems = JSONObject()
        val notificationMethodJson = JSONObject()
        notificationMethodJson.put("collapseId", 123)
        ems.put("notificationMethod", notificationMethodJson)
        input["ems"] = ems.toString()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.notificationMethod.operation shouldBe NotificationOperation.INIT
    }
}