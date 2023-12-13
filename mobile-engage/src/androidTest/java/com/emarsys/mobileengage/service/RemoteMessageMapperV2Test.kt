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
import com.emarsys.mobileengage.service.mapper.RemoteMessageMapperV2
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.RetryUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.copyInputStreamToFile
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.json.JSONArray
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


class RemoteMessageMapperV2Test {
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
        const val SID = "test sid"

        const val METADATA_SMALL_NOTIFICATION_ICON_KEY =
            "com.emarsys.mobileengage.small_notification_icon"
        const val METADATA_NOTIFICATION_COLOR = "com.emarsys.mobileengage.notification_color"
        val DEFAULT_SMALL_NOTIFICATION_ICON = R.drawable.default_small_notification_icon
    }

    private lateinit var context: Context
    private lateinit var deviceInfo: DeviceInfo
    private lateinit var mockMetaDataReader: MetaDataReader
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockFileDownloader: FileDownloader
    private lateinit var remoteMessageMapperV2: RemoteMessageMapperV2

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
                    val fileContent =
                        InstrumentationRegistry.getTargetContext().resources.openRawResource(
                            InstrumentationRegistry.getTargetContext().resources.getIdentifier(
                                "test_image",
                                "raw", InstrumentationRegistry.getTargetContext().packageName
                            )
                        )
                    val file = File(
                        InstrumentationRegistry.getTargetContext().cacheDir.toURI()
                            .toURL().path + "/testFile.tmp"
                    )
                    file.copyInputStreamToFile(fileContent)
                    file.toURI().toURL().path
                } else {
                    null
                }
            }
        }
        whenever(mockNotificationSettings.channelSettings).thenReturn(listOf(channelSettings))
        whenever(mockHardwareIdProvider.provideHardwareId()).thenReturn(HARDWARE_ID)
        whenever(mockLanguageProvider.provideLanguage(ArgumentMatchers.any(Locale::class.java))).thenReturn(
            LANGUAGE
        )
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

        remoteMessageMapperV2 = RemoteMessageMapperV2(
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
        whenever(
            mockMetaDataReader.getInt(
                context,
                METADATA_SMALL_NOTIFICATION_ICON_KEY,
                DEFAULT_SMALL_NOTIFICATION_ICON
            )
        )
            .thenReturn(SMALL_RESOURCE_ID)
        whenever(
            mockMetaDataReader.getInt(
                context,
                METADATA_NOTIFICATION_COLOR
            )
        )
            .thenReturn(COLOR_RESOURCE_ID)

        val input: MutableMap<String, String> = createRemoteMessage()
        input["notification.channel_id"] = CHANNEL_ID
        input["ems.style"] = "THUMBNAIL"

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.body shouldBe BODY
        notificationData.title shouldBe TITLE
        notificationData.channelId shouldBe CHANNEL_ID
        notificationData.smallIconResourceId shouldBe SMALL_RESOURCE_ID
        notificationData.colorResourceId shouldBe COLOR_RESOURCE_ID
        notificationData.style shouldBe "THUMBNAIL"
    }

    @Test
    fun testMap_whenTitleIsMissing() {
        val input: MutableMap<String, String> = createRemoteMessage(title = null)
        input["notification.channel_id"] = CHANNEL_ID

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.body shouldBe BODY
        notificationData.title shouldBe null
        notificationData.channelId shouldBe CHANNEL_ID
    }

    @Test
    fun testMap_whenMapIsEmpty() {
        val input: MutableMap<String, String> = HashMap()

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData shouldNotBe eq(null)
    }

    @Test
    fun testMap_whenImageIsAvailable() {
        val input: MutableMap<String, String> = createRemoteMessage()
        input["notification.image"] = IMAGE_URL

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.image shouldNotBe null
    }

    @Test
    fun testMap_whenImageIsNotAvailable() {
        val input: MutableMap<String, String> = createRemoteMessage()
        input["notification.image"] = "https://fa.il/img.jpg"

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.image shouldBe null
    }

    @Test
    fun testMap_whenNotificationMethodIsSet() {
        val notificationMethod =
            NotificationMethod("testNotificationId", NotificationOperation.UPDATE)

        val input: MutableMap<String, String> = createRemoteMessage()
        input["ems.notification_method.collapse_key"] = "testNotificationId"
        input["ems.notification_method.operation"] = "UPDATE"

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.notificationMethod shouldBe notificationMethod
    }

    @Test
    fun testMap_whenNotificationMethodIsMissing() {
        val input: MutableMap<String, String> = createRemoteMessage()

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.notificationMethod.operation shouldBe NotificationOperation.INIT
    }

    @Test
    fun testMap_whenNotificationMethodIsSet_withoutCollapseID_shouldReturnWithInitOperation() {
        val input: MutableMap<String, String> = createRemoteMessage()
        input["ems.notification_method.operation"] = "UPDATE"

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.notificationMethod.operation shouldBe NotificationOperation.INIT
    }

    @Test
    fun testMap_notificationData_shouldContain_campaignId() {
        val testCampaignId = "test campaign id"
        val input: MutableMap<String, String> = createRemoteMessage()
        input["ems.multichannel_id"] = testCampaignId

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.campaignId shouldBe testCampaignId
    }

    @Test
    fun testMap_notificationData_shouldContain_sid() {
        val input: MutableMap<String, String> = createRemoteMessage()

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.sid shouldBe SID
    }

    @Test
    fun testMap_notificationData_shouldContain_actions() {
        val testActions = JSONArray().put(
            JSONObject()
                .put("type", "MECustomEvent")
                .put("id", "Testing")
                .put("title", JSONObject()
                    .put("en", "Test title")
                )
                .put("name","test action name")
        )

        val expectedActions = """[{"type":"MECustomEvent","id":"Testing","title":{"en":"Test title"},"name":"test action name"}]"""
        val input: MutableMap<String, String> = createRemoteMessage()
        input["ems.actions"] = testActions.toString()

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.actions shouldBe expectedActions
    }

    @Test
    fun testMap_notificationData_shouldContain_null_ifDefaultActionTypeIsMissing() {
        val input: MutableMap<String, String> = createRemoteMessage()
        input["ems.tap_actions.default_action.name"] = "test name"
        input["ems.tap_actions.default_action.url"] = "test url"
        input["ems.tap_actions.default_action.payload"] = " test payload"

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.defaultAction shouldBe null
    }

    @Test
    fun testMap_notificationData_shouldContain_defaultAction() {
        val input: MutableMap<String, String> = createRemoteMessage()
        input["ems.tap_actions.default_action.name"] = "test name"
        input["ems.tap_actions.default_action.type"] = "MECustomEvent"
        input["ems.tap_actions.default_action.url"] = "test url"
        input["ems.tap_actions.default_action.payload"] = "test payload"

        val expectedDefaultAction = """{"name":"test name","type":"MECustomEvent","url":"test url","payload":"test payload"}"""

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.defaultAction shouldBe expectedDefaultAction
    }

    @Test
    fun testMap_notificationData_shouldContain_null_ifActionsAreMissing() {
        val input: MutableMap<String, String> = createRemoteMessage()

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.actions shouldBe null
    }

    @Test
    fun testMap_notificationData_shouldContain_inapp() {
        val testInapp = "test inapp"
        val input: MutableMap<String, String> = createRemoteMessage()
        val ems = JSONObject()
        ems.put("inapp", testInapp)
        input["ems.root_params"] = ems.toString()

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.inapp shouldBe testInapp
    }

    @Test
    fun testMap_notificationData_shouldContain_null_ifInappIsMissing() {
        val input: MutableMap<String, String> = createRemoteMessage()

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.inapp shouldBe null
    }


    private fun createRemoteMessage(
        title: String? = TITLE,
        body: String = BODY,
        sid: String = SID
    ): MutableMap<String, String> {
        val payload = mutableMapOf<String, String>()
        title?.let { payload["notification.title"] = it }
        payload["notification.body"] = body
        payload["ems.sid"] = sid

        return payload
    }
}