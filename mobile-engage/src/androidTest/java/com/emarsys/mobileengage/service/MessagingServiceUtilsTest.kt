package com.emarsys.mobileengage.service

import android.R
import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.inbox.InboxParseUtils
import com.emarsys.mobileengage.inbox.model.NotificationCache
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.notification.command.AppEventCommand
import com.emarsys.mobileengage.notification.command.SilentNotificationInformationCommand
import com.emarsys.mobileengage.push.SilentNotificationInformationListenerProvider
import com.emarsys.mobileengage.service.MessagingServiceUtils.styleNotification
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.ReflectionTestUtils.instantiate
import com.emarsys.testUtil.RetryUtils.retryRule
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.copyInputStreamToFile
import com.google.firebase.messaging.RemoteMessage
import com.nhaarman.mockitokotlin2.*
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
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class MessagingServiceUtilsTest {
    private companion object {
        const val TITLE = "title"
        const val BODY = "body"
        const val CHANNEL_ID = "channelId"
        const val HARDWARE_ID = "hwid"
        const val SDK_VERSION = "sdkVersion"
        const val LANGUAGE = "en-US"
        const val IMAGE_URL = "https://emarsys.com/image"
        const val HTML_URL = "https://hu.wikipedia.org/wiki/Mont_Blanc"

        val IMAGE: Bitmap = Bitmap.createBitmap(51, 51, Bitmap.Config.ARGB_8888)
        val SMALL_NOTIFICATION_ICON = com.emarsys.mobileengage.R.drawable.default_small_notification_icon
        val COLOR = com.emarsys.mobileengage.R.color.common_google_signin_btn_text_light
        val EMPTY_NOTIFICATION_DATA: NotificationData = NotificationData(null, null, null, null, null, null, SMALL_NOTIFICATION_ICON, COLOR)
    }

    private lateinit var context: Context
    private lateinit var deviceInfo: DeviceInfo
    private lateinit var mockNotificationCache: NotificationCache
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockFileDownloader: FileDownloader
    private lateinit var mockActionCommandFactory: ActionCommandFactory
    private lateinit var mockSilentNotificationInformationListenerProvider: SilentNotificationInformationListenerProvider
    private lateinit var mockRemoteMessageMapper: RemoteMessageMapper

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    var retry: TestRule = retryRule

    @Before
    fun init() {
        context = getTargetContext()
        val mockNotificationSettings: NotificationSettings = mock()
        val mockHardwareIdProvider: HardwareIdProvider = mock()
        val mockVersionProvider: VersionProvider = mock()
        val mockLanguageProvider: LanguageProvider = mock()
        val channelSettings = ChannelSettings(channelId = CHANNEL_ID)
        mockFileDownloader = mock<FileDownloader>().apply {
            whenever(download(any())).thenAnswer {
                if (it.arguments[0] == IMAGE_URL || it.arguments[0] == HTML_URL) {
                    val fileContent = getTargetContext().resources.openRawResource(
                            getTargetContext().resources.getIdentifier("test_image",
                                    "raw", getTargetContext().packageName))
                    val file = File(getTargetContext().cacheDir.toURI().toURL().path + "/testFile.tmp")
                    file.copyInputStreamToFile(fileContent)
                    file.toURI().toURL().path
                } else {
                    null
                }
            }
        }
        mockActionCommandFactory = mock()
        mockSilentNotificationInformationListenerProvider = mock()

        whenever(mockNotificationSettings.channelSettings).thenReturn(listOf(channelSettings))
        whenever(mockHardwareIdProvider.provideHardwareId()).thenReturn(HARDWARE_ID)
        whenever(mockLanguageProvider.provideLanguage(ArgumentMatchers.any(Locale::
        class.java))).thenReturn(LANGUAGE)
        whenever(mockVersionProvider.provideSdkVersion()).thenReturn(SDK_VERSION)
        deviceInfo = DeviceInfo(context,
                mockHardwareIdProvider,
                mockVersionProvider,
                mockLanguageProvider,
                mockNotificationSettings,
                true)
        mockNotificationCache = mock()
        mockTimestampProvider = mock()
        whenever(mockTimestampProvider.provideTimestamp()).thenReturn(1L)
        mockRemoteMessageMapper = mock()

        DependencyInjection.setup(FakeMobileEngageDependencyContainer(
                silentNotificationInformationListenerProvider = mockSilentNotificationInformationListenerProvider))
    }

    @After
    fun tearDown() {
        DependencyInjection.getContainer<DependencyContainer>().getCoreSdkHandler().looper.quit()
        DependencyInjection.tearDown()
    }

    @Test
    fun testHandleMessage_shouldReturnFalse_ifMessageIsNotHandled() {
        whenever(mockRemoteMessageMapper.map(any())).thenReturn(EMPTY_NOTIFICATION_DATA)
        MessagingServiceUtils.handleMessage(context, createRemoteMessage(), deviceInfo, mockNotificationCache, mockTimestampProvider, mockFileDownloader, mockActionCommandFactory, mockRemoteMessageMapper) shouldBe false
    }

    @Test
    fun testHandleMessage_shouldReturnTrue_ifMessageIsHandled() {
        whenever(mockRemoteMessageMapper.map(any())).thenReturn(EMPTY_NOTIFICATION_DATA)
        MessagingServiceUtils.handleMessage(context, createEMSRemoteMessage(), deviceInfo, mockNotificationCache, mockTimestampProvider, mockFileDownloader, mockActionCommandFactory, mockRemoteMessageMapper) shouldBe true
    }

    @Test
    fun testHandleMessage_shouldReturnTrue_whenSilent() {
        val message = mapOf(
                "ems_msg" to "value",
                "ems" to JSONObject(mapOf(
                        "silent" to true
                )).toString()
        )
        MessagingServiceUtils.isSilent(message) shouldBe true
    }

    @Test
    fun testHandleMessage_shouldReturnFalse_whenNotSilent() {
        val message = mapOf(
                "ems_msg" to "value",
                "ems" to JSONObject(mapOf(
                        "silent" to false
                )).toString()
        )
        MessagingServiceUtils.isSilent(message) shouldBe false
    }

    @Test
    fun testHandleMessage_shouldReturnFalse_whenSilentIsNotDefined() {
        val message = mapOf(
                "ems_msg" to "value",
                "ems" to JSONObject().toString()
        )
        MessagingServiceUtils.isSilent(message) shouldBe false
    }

    @Test
    fun testIsMobileEngageMessage_shouldBeFalse_withEmptyData() {
        val remoteMessageData: Map<String, String?> = HashMap()
        MessagingServiceUtils.isMobileEngageMessage(remoteMessageData) shouldBe false
    }

    @Test
    fun testIsMobileEngageMessage_shouldBeTrue_withDataWhichContainsTheCorrectKey() {
        val remoteMessageData: MutableMap<String, String?> = HashMap()
        remoteMessageData["ems_msg"] = "value"
        MessagingServiceUtils.isMobileEngageMessage(remoteMessageData) shouldBe true
    }

    @Test
    fun testIsMobileEngageMessage_shouldBeFalse_withDataWithout_ems_msg() {
        val remoteMessageData: MutableMap<String, String?> = HashMap()
        remoteMessageData["key1"] = "value1"
        remoteMessageData["key2"] = "value2"
        MessagingServiceUtils.isMobileEngageMessage(remoteMessageData) shouldBe false
    }

    @Test
    fun createNotification_shouldNotBeNull() {
        val notificationData = NotificationData(null, null, null, null, null, null, SMALL_NOTIFICATION_ICON, COLOR)

        val input: MutableMap<String, String> = HashMap()
        whenever(mockRemoteMessageMapper.map(input)).thenReturn(notificationData)
        MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                mockRemoteMessageMapper,
                mockFileDownloader) shouldNotBe null
    }

    @Test
    fun createNotification_withBigTextStyle_withTitleAndBody() {
        val notificationData = NotificationData(null, null, null, TITLE, BODY, CHANNEL_ID, SMALL_NOTIFICATION_ICON, COLOR)

        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["channel_id"] = CHANNEL_ID

        whenever(mockRemoteMessageMapper.map(input)).thenReturn(notificationData)
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                mockRemoteMessageMapper,
                mockFileDownloader)
        result.extras.getString(NotificationCompat.EXTRA_TITLE) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe null
    }

    @Test
    fun createNotification_withBigTextStyle_withTitle_withoutBody() {
        val notificationData = NotificationData(null, null, null, TITLE, null, CHANNEL_ID, SMALL_NOTIFICATION_ICON, COLOR)

        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["channel_id"] = CHANNEL_ID
        whenever(mockRemoteMessageMapper.map(input)).thenReturn(notificationData)

        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                mockRemoteMessageMapper,
                mockFileDownloader)
        result.extras.getString(NotificationCompat.EXTRA_TITLE) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe null
        result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT) shouldBe null
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe null
    }

    @Test
    fun createNotification_withBigTextStyle_withoutTitle_withBody() {
        val notificationData = NotificationData(null, null, null, null, BODY, CHANNEL_ID, SMALL_NOTIFICATION_ICON, COLOR)
        val input: MutableMap<String, String> = HashMap()
        input["body"] = BODY
        input["channel_id"] = CHANNEL_ID
        whenever(mockRemoteMessageMapper.map(input)).thenReturn(notificationData)

        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                mockRemoteMessageMapper,
                mockFileDownloader)
        val expectedTitle = expectedBasedOnApiLevel(applicationName, null)

        result.extras.getString(NotificationCompat.EXTRA_TITLE) shouldBe expectedTitle
        result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG) shouldBe expectedTitle
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe null
    }

    @Test
    fun createNotification_withBigTextStyle_withoutTitle_withBody_withDefaultTitle() {
        val notificationData = NotificationData(null, null, null, null, BODY, CHANNEL_ID, SMALL_NOTIFICATION_ICON, COLOR)
        val input: MutableMap<String, String> = HashMap()
        input["body"] = BODY
        input["u"] = "{\"test_field\":\"\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}"
        input["channel_id"] = CHANNEL_ID
        whenever(mockRemoteMessageMapper.map(input)).thenReturn(notificationData)

        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                mockRemoteMessageMapper,
                mockFileDownloader)

        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe null
    }

    @Test
    fun testCreateNotification_withBigPictureStyle_whenImageIsAvailable() {
        val notificationData = NotificationData(IMAGE, null, null, TITLE, BODY, CHANNEL_ID, SMALL_NOTIFICATION_ICON, COLOR)
        whenever(mockRemoteMessageMapper.map(any())).thenReturn(notificationData)

        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                mapOf(),
                deviceInfo,
                mockRemoteMessageMapper,
                mockFileDownloader)

        result.extras.getString(NotificationCompat.EXTRA_TITLE) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe BODY
        result.extras[NotificationCompat.EXTRA_PICTURE] shouldNotBe null
        result.extras[NotificationCompat.EXTRA_LARGE_ICON] shouldNotBe null
        result.extras[NotificationCompat.EXTRA_LARGE_ICON_BIG] shouldBe null
    }

    @Test
    fun testCreateNotification_withBigTextStyle_whenImageIsNotAvailable() {
        val notificationData = NotificationData(null, null, null, TITLE, BODY, CHANNEL_ID, SMALL_NOTIFICATION_ICON, COLOR)
        whenever(mockRemoteMessageMapper.map(any())).thenReturn(notificationData)

        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                mapOf(),
                deviceInfo,
                mockRemoteMessageMapper,
                mockFileDownloader)

        result.extras.getString(NotificationCompat.EXTRA_TITLE) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe null
    }

    @Test
    fun testCreateNotification_setsNotificationColor() {
        val colorResourceId = R.color.darker_gray
        val mockMetaDataReader: MetaDataReader = mock {
            on { getInt(ArgumentMatchers.any(Context::class.java), ArgumentMatchers.any(String::class.java)) } doReturn colorResourceId
        }
        val remoteMessageMapper = RemoteMessageMapper(mockMetaDataReader, context, mock(), deviceInfo)

        val expectedColor = ContextCompat.getColor(context, colorResourceId)
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                remoteMessageMapper,
                mockFileDownloader)

        result.color shouldBe expectedColor
    }

    @Test
    fun testCreateNotification_doesNotSet_notificationColor_whenCodeIsInvalid() {
        val mockMetaDataReader: MetaDataReader = mock {
            on { getInt(ArgumentMatchers.any(Context::class.java), ArgumentMatchers.any(String::class.java)) } doReturn 0
        }
        val remoteMessageMapper = RemoteMessageMapper(mockMetaDataReader, context, mock(), deviceInfo)

        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                remoteMessageMapper,
                mockFileDownloader)

        result.color shouldBe Notification.COLOR_DEFAULT
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    fun testCreateNotification_withChannelId() {
        val notificationData = NotificationData(null, null, null, TITLE, BODY, CHANNEL_ID, SMALL_NOTIFICATION_ICON, COLOR)

        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["channel_id"] = CHANNEL_ID
        whenever(mockRemoteMessageMapper.map(input)).thenReturn(notificationData)

        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                mockRemoteMessageMapper,
                mockFileDownloader)

        result.channelId shouldBe CHANNEL_ID
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    fun testCreateNotification_withoutChannelId() {
        val notificationData = NotificationData(null, null, null, TITLE, BODY, null, SMALL_NOTIFICATION_ICON, COLOR)
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        val notificationSettings: NotificationSettings = mock()
        val deviceInfo: DeviceInfo = mock()
        val channelSettings = ChannelSettings(channelId = "notMatchingChannelId")
        whenever(notificationSettings.channelSettings).thenReturn(listOf(channelSettings))
        whenever(deviceInfo.notificationSettings).thenReturn(notificationSettings)
        whenever(deviceInfo.isDebugMode).thenReturn(false)
        whenever(mockRemoteMessageMapper.map(input)).thenReturn(notificationData)
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                mockRemoteMessageMapper,
                mockFileDownloader)

        result.channelId shouldBe null
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    fun testCreateNotification_withoutChannelId_inDebugMode() {
        val notificationData = NotificationData(null, null, null, TITLE, BODY, null, SMALL_NOTIFICATION_ICON, COLOR)
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        val notificationSettings: NotificationSettings = mock()
        val deviceInfo: DeviceInfo = mock()
        val channelSettings = ChannelSettings(channelId = "notMatchingChannelId")
        whenever(notificationSettings.channelSettings).thenReturn(listOf(channelSettings))
        whenever(deviceInfo.notificationSettings).thenReturn(notificationSettings)
        whenever(deviceInfo.isDebugMode).thenReturn(true)
        whenever(mockRemoteMessageMapper.map(input)).thenReturn(notificationData)
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                mockRemoteMessageMapper,
                mockFileDownloader)
        result.channelId shouldBe "ems_debug"
    }

    @Test
    fun testCreateNotification_setsActionsIfAvailable() {
        val ems = JSONObject()
                .put("actions", JSONArray()
                        .put(JSONObject()
                                .put("id", "uniqueActionId1")
                                .put("title", "title1")
                                .put("type", "MEAppEvent")
                                .put("name", "event1")
                        )
                        .put(JSONObject()
                                .put("id", "uniqueActionId2")
                                .put("title", "title2")
                                .put("type", "MEAppEvent")
                                .put("name", "event2")
                                .put("payload", JSONObject()
                                        .put("payloadKey", "payloadValue"))
                        ))
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["ems"] = ems.toString()
        whenever(mockRemoteMessageMapper.map(any())).thenReturn(EMPTY_NOTIFICATION_DATA)
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                mockRemoteMessageMapper,
                mockFileDownloader)
        result.actions shouldNotBe null
        result.actions.size shouldBe 2
        result.actions[0].title shouldBe "title1"
        result.actions[1].title shouldBe "title2"
    }

    @Test
    fun testCreateNotification_action_withoutActions() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        whenever(mockRemoteMessageMapper.map(any())).thenReturn(EMPTY_NOTIFICATION_DATA)
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                mockRemoteMessageMapper,
                mockFileDownloader)
        result.actions shouldBe null
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    fun testCreateNotification_returnDebugMessage_whenThereIsChannelIdMismatch() {
        val notificationData = NotificationData(null, null, null, TITLE, BODY, CHANNEL_ID, SMALL_NOTIFICATION_ICON, COLOR)
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["channel_id"] = CHANNEL_ID
        val notificationSettings: NotificationSettings = mock()
        val deviceInfo: DeviceInfo = mock()
        val channelSettings = ChannelSettings(channelId = "notMatchingChannelId")
        whenever(notificationSettings.channelSettings).thenReturn(listOf(channelSettings))
        whenever(deviceInfo.notificationSettings).thenReturn(notificationSettings)
        whenever(deviceInfo.isDebugMode).thenReturn(true)
        whenever(mockRemoteMessageMapper.map(any())).thenReturn(notificationData)
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                mockRemoteMessageMapper,
                mockFileDownloader)
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe "DEBUG - channel_id mismatch: channelId not found!"
    }

    @Test
    fun testCreateNotification_returnOriginalTitle_evenIfThereIsChannelMismatch_but_weAreNotInDebugMode() {
        val notificationData = NotificationData(null, null, null, TITLE, BODY, CHANNEL_ID, SMALL_NOTIFICATION_ICON, COLOR)
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["channel_id"] = CHANNEL_ID
        val notificationSettings: NotificationSettings = mock()
        val deviceInfo: DeviceInfo = mock()
        val channelSettings = ChannelSettings(channelId = "notMatchingChannelId")
        whenever(notificationSettings.channelSettings).thenReturn(listOf(channelSettings))
        whenever(deviceInfo.notificationSettings).thenReturn(notificationSettings)
        whenever(deviceInfo.isDebugMode).thenReturn(false)
        whenever(mockRemoteMessageMapper.map(any())).thenReturn(notificationData)
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                mockRemoteMessageMapper,
                mockFileDownloader)
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe BODY
    }


    @Test
    fun testGetInAppDescriptor_shouldReturnNull_forNullInput() {
        MessagingServiceUtils.getInAppDescriptor(mockFileDownloader, null) shouldBe null
    }

    @Test
    fun testGetInAppDescriptor_shouldReturnNull_whenThereIsNoEmsInPayload() {
        MessagingServiceUtils.getInAppDescriptor(mockFileDownloader, createNoEmsInPayload()) shouldBe null
    }

    @Test
    fun testGetInAppDescriptor_shouldReturnNull_whenThereIsNoInAppInPayload() {
        MessagingServiceUtils.getInAppDescriptor(mockFileDownloader, createNoInAppInPayload()) shouldBe null
    }

    @Test
    fun testGetInAppDescriptor_shouldReturnValidDescriptor_whenThereIsInAppInPayload() {
        val result = JSONObject(MessagingServiceUtils.getInAppDescriptor(mockFileDownloader, createInAppInPayload()))

        result.getString("campaignId") shouldBe "someId"
        result.getString("url") shouldBe HTML_URL
        result.getString("fileUrl") shouldNotBe null
    }

    @Test
    fun testGetInAppDescriptor_shouldBeNull_whenCampaignIdIsMissing() {
        val payload: MutableMap<String, String> = HashMap()
        val ems = JSONObject()
        val inapp = JSONObject()
        inapp.put("url", HTML_URL)
        ems.put("inapp", inapp)
        payload["ems"] = ems.toString()
        MessagingServiceUtils.getInAppDescriptor(mockFileDownloader, payload) shouldBe null
    }

    @Test
    fun testGetInAppDescriptor_shouldBeNull_whenUrlIsMissing() {
        val payload: MutableMap<String, String> = HashMap()
        val ems = JSONObject()
        val inapp = JSONObject()
        inapp.put("campaignId", "someId")
        ems.put("inapp", inapp)
        payload["ems"] = ems.toString()

        MessagingServiceUtils.getInAppDescriptor(mockFileDownloader, payload) shouldBe null
    }

    @Test
    fun testGetInAppDescriptor_shouldReturnWithUrlAndCampaignId_whenFileUrlIsNull() {
        val payload: MutableMap<String, String> = HashMap()
        val ems = JSONObject()
        val inapp = JSONObject()
        inapp.put("campaign_id", "someId")
        inapp.put("url", "https://thisIsNotARealUrl")
        ems.put("inapp", inapp)
        payload["ems"] = ems.toString()
        val result = JSONObject(MessagingServiceUtils.getInAppDescriptor(mockFileDownloader, payload))

        result.getString("campaignId") shouldBe "someId"
        result.getString("url") shouldBe "https://thisIsNotARealUrl"
        result.has("fileUrl") shouldBe false
    }

    @Test
    fun testCreatePreloadedRemoteMessageData_shouldPutInAppDescriptorUnderEms_whenAvailableAndInAppIsTurnedOn() {
        val inAppDescriptor = "InAppDescriptor"
        val inAppPayload = createNoInAppInPayload()
        val result = MessagingServiceUtils.createPreloadedRemoteMessageData(inAppPayload, inAppDescriptor)

        JSONObject(result["ems"]).getString("inapp") shouldBe inAppDescriptor
    }

    @Test
    fun testCreatePreloadedRemoteMessageData_shouldNotPutInAppDescriptorUnderEms_whenNotAvailable() {
        val inAppDescriptor: String? = null
        val inAppPayload = createNoInAppInPayload()
        val result = MessagingServiceUtils.createPreloadedRemoteMessageData(inAppPayload, inAppDescriptor)

        JSONObject(result["ems"]).has("inapp") shouldBe false
    }

    @Test
    fun testCacheNotification_shouldCacheNotification() {
        val remoteData: MutableMap<String, String?> = mutableMapOf(
                "ems_msg" to "true",
                "u" to """{"test_field":"","image":"https://media.giphy.com/media/ktvFa67wmjDEI/giphy.gif","deep_link":"lifestylelabels.com/mobile/product/3245678","sid":"sid_here"}""",
                "id" to "21022.150123121212.43223434c3b9",
                "inbox" to "true",
                "title" to "hello there",
                "rootParam1" to "param_param")
        MessagingServiceUtils.cacheNotification(mockTimestampProvider, mockNotificationCache, remoteData)
        val notification = InboxParseUtils.parseNotificationFromPushMessage(mockTimestampProvider, false, remoteData)
        verify(mockNotificationCache).cache(notification)
    }

    @Test
    fun testCreateSilentPushCommands_shouldReturnEmptyList_whenNoActionIsDefined() {
        val message = mapOf(
                "ems_msg" to "value",
                "ems" to JSONObject(mapOf(
                        "silent" to true
                )).toString()
        )

        MessagingServiceUtils.createSilentPushCommands(mockActionCommandFactory, message) shouldBe emptyList()
    }

    @Test
    fun testCreateSilentPushCommands_shouldCreateSilentNotificationCommand_whenMessageIsSilentAndContainsCampaignId() {
        val message = mapOf(
                "ems_msg" to "value",
                "ems" to JSONObject(mapOf(
                        "multichannelId" to "testCampaignId",
                        "silent" to true
                )).toString()
        )

        MessagingServiceUtils.createSilentPushCommands(mockActionCommandFactory, message).size shouldBe 1
        MessagingServiceUtils.createSilentPushCommands(mockActionCommandFactory, message)[0] is SilentNotificationInformationCommand
    }

    @Test
    fun testCreateSilentPushCommands_shouldReturnListOfCommands_whenActionIsDefined() {
        val expectedCommand1 = AppEventCommand(context, mock(), "MEAppEvent", null)
        val expectedCommand2 = AppEventCommand(context, mock(), "MEAppEvent", JSONObject(mapOf("key" to "value")))
        whenever(mockActionCommandFactory.createActionCommand(any())).thenReturn(expectedCommand1).thenReturn(expectedCommand2)
        val message = mapOf(
                "ems_msg" to "value",
                "ems" to JSONObject(mapOf(
                        "silent" to true,
                        "actions" to JSONArray(listOf(
                                JSONObject(mapOf(
                                        "type" to "MEAppEvent",
                                        "name" to "nameOfTheAppEvent"
                                )),
                                JSONObject(mapOf(
                                        "type" to "MEAppEvent",
                                        "name" to "nameOfTheAppEvent",
                                        "payload" to JSONObject(mapOf(
                                                "key" to "value"
                                        ))
                                ))
                        ))
                )).toString()
        )

        MessagingServiceUtils.createSilentPushCommands(mockActionCommandFactory, message) shouldBe listOf(
                expectedCommand1,
                expectedCommand2)
    }

    @Test
    fun testCreateSilentPushCommands_shouldReturnListOfCommands_whenListenerIsRegistered() {
        val expectedCommand1 = AppEventCommand(context, mock(), "MEAppEvent", null)
        val expectedCommand2 = AppEventCommand(context, mock(), "MEAppEvent", JSONObject(mapOf("key" to "value")))
        whenever(mockActionCommandFactory.createActionCommand(any())).thenReturn(expectedCommand1).thenReturn(expectedCommand2)
        val message = mapOf(
                "ems_msg" to "value",
                "ems" to JSONObject(mapOf(
                        "silent" to true,
                        "actions" to JSONArray(listOf(
                                JSONObject(mapOf(
                                        "type" to "MEAppEvent",
                                        "name" to "nameOfTheAppEvent"
                                )),
                                JSONObject(mapOf(
                                        "type" to "MEAppEvent",
                                        "name" to "nameOfTheAppEvent",
                                        "payload" to JSONObject(mapOf(
                                                "key" to "value"
                                        ))
                                ))
                        ))
                )).toString()
        )

        MessagingServiceUtils.createSilentPushCommands(mockActionCommandFactory, message) shouldBe listOf(
                expectedCommand1,
                expectedCommand2)
    }

    @Test
    fun testStyleNotification_whenStyleIsMessage() {
        "MESSAGE" shouldCreateNotificationWithStyle NotificationCompat.MessagingStyle::class.java
    }

    @Test
    fun testStyleNotification_whenStyleIsBigText() {
        "BIG_TEXT" shouldCreateNotificationWithStyle NotificationCompat.BigTextStyle::class.java
    }

    @Test
    fun testStyleNotification_whenStyleIsThumbnail() {
        val mockBuilder: NotificationCompat.Builder = mock() {
            on { setLargeIcon(any()) } doReturn it
            on { setContentTitle(any()) } doReturn it
            on { setContentText(any()) } doReturn it
        }
        val title = "testTitle"
        val body = "testBody"
        val image = Bitmap.createBitmap(51, 51, Bitmap.Config.ARGB_8888)
        val icon = Bitmap.createBitmap(51, 51, Bitmap.Config.ARGB_8888)
        mockBuilder.styleNotification(NotificationData(image, icon, "THUMBNAIL", title, body, null, 123, 456))

        verify(mockBuilder, times(0)).setStyle(any())
    }

    @Test
    fun testStyleNotification_whenStyleIsBigPicture() {
        "BIG_PICTURE" shouldCreateNotificationWithStyle NotificationCompat.BigPictureStyle::class.java
    }

    @Test
    fun testStyleNotification_whenStyleIsInvalid_imageIsNotSet_shouldBeBigTextStyle() {
        val mockBuilder: NotificationCompat.Builder = mock()

        val title = "testTitle"
        val body = "testBody"
        mockBuilder.styleNotification(NotificationData(null, null, "INVALID_STYLE", title, body, null, 222, 333))

        verify(mockBuilder).setStyle(any<NotificationCompat.BigTextStyle>())
    }

    @Test
    fun testStyleNotification_whenStyleIsInvalid_imageIsSet_shouldBeBigPictureStyle() {
        "INVALID_STYLE" shouldCreateNotificationWithStyle NotificationCompat.BigPictureStyle::class.java
    }

    private inline infix fun <reified T : NotificationCompat.Style> String.shouldCreateNotificationWithStyle(style: Class<T>) {
        val mockBuilder: NotificationCompat.Builder = mock {
            on { setLargeIcon(com.nhaarman.mockitokotlin2.any()) } doReturn it
            on { setStyle(com.nhaarman.mockitokotlin2.any()) } doReturn it
        }
        val title = "testTitle"
        val body = "testBody"
        val image = Bitmap.createBitmap(51, 51, Bitmap.Config.ARGB_8888)
        val icon = Bitmap.createBitmap(51, 51, Bitmap.Config.ARGB_8888)
        mockBuilder.styleNotification(NotificationData(image, icon, this, title, body, null, 222, 444))

        verify(mockBuilder).setStyle(any<T>())
    }

    private fun createRemoteMessage(): RemoteMessage {
        val bundle = Bundle()
        bundle.putString("title", "title")
        bundle.putString("body", "body")
        return instantiate(RemoteMessage::class.java, 0, bundle)
    }

    private fun createEMSRemoteMessage(): RemoteMessage {
        val bundle = Bundle()
        bundle.putString("title", "title")
        bundle.putString("body", "body")
        bundle.putString("ems_msg", "value")
        return instantiate(RemoteMessage::class.java, 0, bundle)
    }

    private fun createNoEmsInPayload(): Map<String, String> {
        return emptyMap()
    }

    private fun createNoInAppInPayload(): Map<String, String> {
        return mapOf(
                "ems" to "{}"
        )
    }

    private fun createInAppInPayload(): Map<String, String> {
        val payload: MutableMap<String, String> = HashMap()
        val ems = JSONObject()
        val inapp = JSONObject()
        inapp.put("campaign_id", "someId")
        inapp.put("url", HTML_URL)
        ems.put("inapp", inapp)
        payload["ems"] = ems.toString()
        return payload
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