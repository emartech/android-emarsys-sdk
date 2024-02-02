package com.emarsys.mobileengage.service

import android.R
import android.app.Notification
import android.content.Context
import android.content.res.Resources
import android.os.Build.VERSION_CODES
import android.util.DisplayMetrics
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.test.filters.SdkSuppress
import com.emarsys.core.api.notification.ChannelSettings
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.util.FileDownloader
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.notification.command.AppEventCommand
import com.emarsys.mobileengage.notification.command.SilentNotificationInformationCommand
import com.emarsys.mobileengage.push.SilentNotificationInformationListenerProvider
import com.emarsys.mobileengage.service.MessagingServiceUtils.styleNotification
import com.emarsys.mobileengage.service.mapper.RemoteMessageMapperFactory
import com.emarsys.mobileengage.service.mapper.RemoteMessageMapperV1
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.RetryUtils.retryRule
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.util.Locale

class MessagingServiceUtilsTest {
    private companion object {
        const val TITLE = "title"
        const val BODY = "body"
        const val CHANNEL_ID = "channelId"
        const val HARDWARE_ID = "hwid"
        const val SDK_VERSION = "sdkVersion"
        const val LANGUAGE = "en-US"
        const val IMAGE_URL = "https://emarsys.com/image"
        const val ICON_URL = "https://emarsys.com/icon_image"
        const val HTML_URL = "https://hu.wikipedia.org/wiki/Mont_Blanc"
        const val COLOR = R.color.darker_gray
        const val MULTICHANNEL_ID = "test multiChannel id"
        const val SID = "test sid"
        const val COLLAPSE_ID = "testCollapseId"
        val OPERATION = NotificationOperation.INIT.name

        val SMALL_NOTIFICATION_ICON =
            com.emarsys.mobileengage.R.drawable.default_small_notification_icon
        val EMPTY_NOTIFICATION_DATA: NotificationData = NotificationData(
            null,
            null,
            null,
            null,
            null,
            null,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null
        )
    }

    private lateinit var context: Context
    private lateinit var deviceInfo: DeviceInfo
    private lateinit var mockFileDownloader: FileDownloader
    private lateinit var mockRemoteMessageMapperFactory: RemoteMessageMapperFactory
    private lateinit var mockActionCommandFactory: ActionCommandFactory
    private lateinit var mockSilentNotificationInformationListenerProvider: SilentNotificationInformationListenerProvider
    private lateinit var mockRemoteMessageMapperV1: RemoteMessageMapperV1

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
            whenever(download(any(), any())).thenAnswer {
                if (it.arguments[0] == IMAGE_URL || it.arguments[0] == ICON_URL || it.arguments[0] == HTML_URL) {
                    val fileContent = getTargetContext().resources.openRawResource(
                        getTargetContext().resources.getIdentifier(
                            "test_image",
                            "raw", getTargetContext().packageName
                        )
                    )
                    val file =
                        File(getTargetContext().cacheDir.toURI().toURL().path + "/testFile.tmp")
                    file.copyInputStreamToFile(fileContent)
                    file.toURI().toURL().path
                } else {
                    null
                }
            }
        }
        mockRemoteMessageMapperFactory = mock()
        mockActionCommandFactory = mock()
        mockSilentNotificationInformationListenerProvider = mock()

        whenever(mockNotificationSettings.channelSettings).thenReturn(listOf(channelSettings))
        whenever(mockHardwareIdProvider.provideHardwareId()).thenReturn(HARDWARE_ID)
        whenever(
            mockLanguageProvider.provideLanguage(
                ArgumentMatchers.any(
                    Locale::
                    class.java
                )
            )
        ).thenReturn(LANGUAGE)
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
        mockRemoteMessageMapperV1 = mock()

        setupMobileEngageComponent(
            FakeMobileEngageDependencyContainer(silentNotificationInformationListenerProvider = mockSilentNotificationInformationListenerProvider)
        )
    }

    @After
    fun tearDown() {
        mobileEngage().concurrentHandlerHolder.coreLooper.quitSafely()
        tearDownMobileEngageComponent()
    }

    @Test
    fun testIsSilent_shouldReturnTrue_whenSilent() {
        val message = mapOf(
            "ems_msg" to "value",
            "ems" to JSONObject(
                mapOf(
                    "silent" to true
                )
            ).toString()
        )
        MessagingServiceUtils.isSilent(message) shouldBe true
    }

    @Test
    fun testIsSilent_shouldReturnFalse_whenNotSilent() {
        val message = mapOf(
            "ems_msg" to "value",
            "ems" to JSONObject(
                mapOf(
                    "silent" to false
                )
            ).toString()
        )
        MessagingServiceUtils.isSilent(message) shouldBe false
    }

    @Test
    fun testIsSilent_shouldReturnFalse_whenSilentIsNotDefined() {
        val message = mapOf(
            "ems_msg" to "value",
            "ems" to JSONObject().toString()
        )
        MessagingServiceUtils.isSilent(message) shouldBe false
    }

    @Test
    fun testIsSilent_shouldReturnFalse_whenV2_isSilentIsFalse() {
        val message = mapOf(
            "ems.silent" to "false",
        )
        MessagingServiceUtils.isSilent(message) shouldBe false
    }

    @Test
    fun testIsSilent_shouldReturnTru_whenV2_isSilentIsTrue() {
        val message = mapOf(
            "ems.silent" to "true",
        )
        MessagingServiceUtils.isSilent(message) shouldBe true
    }

    @Test
    fun testHandleMessage_shouldUse_V1_mapper_withOldNotificationStructure() {
        val notificationData = NotificationData(
            IMAGE_URL,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null,
        )

        val ems = JSONObject()
        val message = mapOf(
            "ems_msg" to "value",
            "ems" to ems.toString()
        )
        whenever(mockRemoteMessageMapperFactory.create(message)).thenReturn(
            mockRemoteMessageMapperV1
        )
        whenever(mockRemoteMessageMapperV1.map(message)).thenReturn(notificationData)

        MessagingServiceUtils.handleMessage(
            context,
            message,
            deviceInfo,
            mockFileDownloader,
            mockActionCommandFactory,
            mockRemoteMessageMapperFactory
        )

        verify(mockRemoteMessageMapperV1).map(message)
    }

    @Test
    fun testIsMobileEngageNotification_shouldBeFalse_withEmptyData() {
        val remoteMessageData: Map<String, String?> = HashMap()
        MessagingServiceUtils.isMobileEngageNotification(remoteMessageData) shouldBe false
    }

    @Test
    fun testIsMobileEngageNotification_shouldBeTrue_withDataWhichContainsTheV1CorrectKey() {
        val remoteMessageData: MutableMap<String, String?> = HashMap()
        remoteMessageData["ems_msg"] = "value"
        MessagingServiceUtils.isMobileEngageNotification(remoteMessageData) shouldBe true
    }

    @Test
    fun testIsMobileEngageNotification_shouldBeTrue_withDataWhichContainsTheV2Key() {
        val remoteMessageData: MutableMap<String, String?> = HashMap()
        remoteMessageData["ems.version"] = "testValue"
        MessagingServiceUtils.isMobileEngageNotification(remoteMessageData) shouldBe true
    }

    @Test
    fun testIsMobileEngageNotification_shouldBeFalse_withDataWithout_ems_msg() {
        val remoteMessageData: MutableMap<String, String?> = HashMap()
        remoteMessageData["key1"] = "value1"
        remoteMessageData["key2"] = "value2"
        MessagingServiceUtils.isMobileEngageNotification(remoteMessageData) shouldBe false
    }

    @Test
    fun createNotification_shouldNotBeNull() {
        val notificationData = NotificationData(
            IMAGE_URL,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null
        )

        MessagingServiceUtils.createNotification(
            context,
            deviceInfo,
            mockFileDownloader,
            notificationData
        ) shouldNotBe null
    }

    @Test
    fun createNotification_withBigTextStyle_withTitleAndBody() {
        val notificationData = NotificationData(
            null,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null,
        )

        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["channel_id"] = CHANNEL_ID

        val result = MessagingServiceUtils.createNotification(
            context,
            deviceInfo,
            mockFileDownloader,
            notificationData
        )
        result.extras.getString(NotificationCompat.EXTRA_TITLE) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe null
    }

    @Test
    fun createNotification_withBigTextStyle_withTitle_withoutBody() {
        val notificationData = NotificationData(
            IMAGE_URL,
            null,
            null,
            TITLE,
            null,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null
        )

        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["channel_id"] = CHANNEL_ID

        val result = MessagingServiceUtils.createNotification(
            context,
            deviceInfo,
            mockFileDownloader,
            notificationData
        )
        result.extras.getString(NotificationCompat.EXTRA_TITLE) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe null
        result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT) shouldBe null
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe null
    }

    @Test
    fun createNotification_withBigTextStyle_withoutTitle_withBody_withDefaultTitle() {
        val notificationData = NotificationData(
            null,
            null,
            null,
            null,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null
        )

        val result = MessagingServiceUtils.createNotification(
            context,
            deviceInfo,
            mockFileDownloader,
            notificationData
        )

        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe null
    }

    @Test
    fun testCreateNotification_withBigPictureStyle_whenImageIsAvailable() {
        val notificationData = NotificationData(
            IMAGE_URL,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null
        )

        val result = MessagingServiceUtils.createNotification(
            context,
            deviceInfo,
            mockFileDownloader,
            notificationData
        )

        result.extras.getString(NotificationCompat.EXTRA_TITLE) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe BODY
        result.extras[NotificationCompat.EXTRA_PICTURE] shouldNotBe null
        result.extras[NotificationCompat.EXTRA_LARGE_ICON] shouldNotBe null
        result.extras[NotificationCompat.EXTRA_LARGE_ICON_BIG] shouldBe null
    }

    @Test
    fun testCreateNotification_withBigTextStyle_whenImageIsNotAvailable() {
        val notificationData = NotificationData(
            null,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null
        )

        val result = MessagingServiceUtils.createNotification(
            context,
            deviceInfo,
            mockFileDownloader,
            notificationData
        )

        result.extras.getString(NotificationCompat.EXTRA_TITLE) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe null
    }

    @Test
    fun testCreateNotification_setsNotificationColor() {
        val colorResourceId = R.color.darker_gray
        val notificationData = NotificationData(
            IMAGE_URL,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = colorResourceId,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null
        )

        val expectedColor = ContextCompat.getColor(context, colorResourceId)

        val result = MessagingServiceUtils.createNotification(
            context,
            deviceInfo,
            mockFileDownloader,
            notificationData
        )

        result.color shouldBe expectedColor
    }

    @Test
    fun testCreateNotification_doesNotSet_notificationColor_whenCodeIsInvalid() {
        val invalidColor = 0
        val notificationData = NotificationData(
            IMAGE_URL,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = invalidColor,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null
        )

        val result = MessagingServiceUtils.createNotification(
            context,
            deviceInfo,
            mockFileDownloader,
            notificationData
        )

        result.color shouldBe Notification.COLOR_DEFAULT
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    fun testCreateNotification_withChannelId() {
        val notificationData = NotificationData(
            IMAGE_URL,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null
        )

        val result = MessagingServiceUtils.createNotification(
            context,
            deviceInfo,
            mockFileDownloader,
            notificationData
        )

        result.channelId shouldBe CHANNEL_ID
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    fun testCreateNotification_withoutChannelId() {
        val notificationData = NotificationData(
            IMAGE_URL,
            null,
            null,
            TITLE,
            BODY,
            null,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null
        )

        val notificationSettings: NotificationSettings = mock()
        val mockDeviceInfo: DeviceInfo = mock()
        val channelSettings = ChannelSettings(channelId = "notMatchingChannelId")
        whenever(notificationSettings.channelSettings).thenReturn(listOf(channelSettings))
        whenever(mockDeviceInfo.notificationSettings).thenReturn(notificationSettings)
        whenever(mockDeviceInfo.isDebugMode).thenReturn(false)
        whenever(mockDeviceInfo.displayMetrics).thenReturn(Resources.getSystem().displayMetrics)
        val result = MessagingServiceUtils.createNotification(
            context,
            mockDeviceInfo,
            mockFileDownloader,
            notificationData
        )

        result.channelId shouldBe null
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    fun testCreateNotification_withoutChannelId_inDebugMode() {
        val notificationData = NotificationData(
            IMAGE_URL,
            null,
            null,
            TITLE,
            BODY,
            null,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null
        )

        val notificationSettings: NotificationSettings = mock()
        val mockDeviceInfo: DeviceInfo = mock()
        val channelSettings = ChannelSettings(channelId = "notMatchingChannelId")
        whenever(notificationSettings.channelSettings).thenReturn(listOf(channelSettings))
        whenever(mockDeviceInfo.notificationSettings).thenReturn(notificationSettings)
        whenever(mockDeviceInfo.isDebugMode).thenReturn(true)
        whenever(mockDeviceInfo.displayMetrics).thenReturn(Resources.getSystem().displayMetrics)
        val result = MessagingServiceUtils.createNotification(
            context,
            mockDeviceInfo,
            mockFileDownloader,
            notificationData
        )
        result.channelId shouldBe "ems_debug"
    }

    @Test
    fun testCreateNotification_setsActionsIfAvailable() {
        val actions = JSONArray()
            .put(
                JSONObject()
                    .put("id", "uniqueActionId1")
                    .put("title", "title1")
                    .put("type", "MEAppEvent")
                    .put("name", "event1")
            )
            .put(
                JSONObject()
                    .put("id", "uniqueActionId2")
                    .put("title", "title2")
                    .put("type", "MEAppEvent")
                    .put("name", "event2")
                    .put(
                        "payload", JSONObject()
                            .put("payloadKey", "payloadValue")
                    )
            )

        val notificationData = NotificationData(
            IMAGE_URL,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = actions.toString(),
            inapp = null
        )

        val result = MessagingServiceUtils.createNotification(
            context,
            deviceInfo,
            mockFileDownloader,
            notificationData
        )
        result.actions shouldNotBe null
        result.actions.size shouldBe 2
        result.actions[0].title shouldBe "title1"
        result.actions[1].title shouldBe "title2"
    }

    @Test
    fun testCreateNotification_action_withoutActions() {
        val result = MessagingServiceUtils.createNotification(
            context,
            deviceInfo,
            mockFileDownloader,
            EMPTY_NOTIFICATION_DATA
        )
        result.actions shouldBe null
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    fun testCreateNotification_returnDebugMessage_whenThereIsChannelIdMismatch() {
        val notificationData = NotificationData(
            IMAGE_URL,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null
        )

        val notificationSettings: NotificationSettings = mock()
        val mockDeviceInfo: DeviceInfo = mock()
        val channelSettings = ChannelSettings(channelId = "notMatchingChannelId")
        whenever(notificationSettings.channelSettings).thenReturn(listOf(channelSettings))
        whenever(mockDeviceInfo.notificationSettings).thenReturn(notificationSettings)
        whenever(mockDeviceInfo.isDebugMode).thenReturn(true)
        whenever(mockDeviceInfo.displayMetrics).thenReturn(Resources.getSystem().displayMetrics)
        val result = MessagingServiceUtils.createNotification(
            context,
            mockDeviceInfo,
            mockFileDownloader,
            notificationData
        )
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe "DEBUG - channel_id mismatch: channelId not found!"
    }

    @Test
    fun testCreateNotification_returnOriginalTitle_evenIfThereIsChannelMismatch_but_weAreNotInDebugMode() {
        val notificationData = NotificationData(
            IMAGE_URL,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null
        )

        val notificationSettings: NotificationSettings = mock()
        val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics
        val mockDeviceInfo: DeviceInfo = mock()
        val channelSettings = ChannelSettings(channelId = "notMatchingChannelId")
        whenever(notificationSettings.channelSettings).thenReturn(listOf(channelSettings))
        whenever(mockDeviceInfo.notificationSettings).thenReturn(notificationSettings)
        whenever(mockDeviceInfo.isDebugMode).thenReturn(false)
        whenever(mockDeviceInfo.displayMetrics).thenReturn(displayMetrics)
        val result = MessagingServiceUtils.createNotification(
            context,
            mockDeviceInfo,
            mockFileDownloader,
            notificationData
        )
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe BODY
    }


    @Test
    fun testGetInAppDescriptor_shouldReturnNull_forNullInput() {
        MessagingServiceUtils.getInAppDescriptor(mockFileDownloader, null) shouldBe null
    }

    @Test
    fun testGetInAppDescriptor_shouldReturnNull_whenThereIsNoInapp() {
        MessagingServiceUtils.getInAppDescriptor(
            mockFileDownloader,
            null
        ) shouldBe null
    }

    @Test
    fun testGetInAppDescriptor_shouldReturnValidDescriptor_whenThereIsInAppInPayload() {
        val result = JSONObject(
            MessagingServiceUtils.getInAppDescriptor(
                mockFileDownloader,
                createInAppInPayload()
            )!!
        )

        result.getString("campaignId") shouldBe "someId"
        result.getString("url") shouldBe HTML_URL
        result.getString("fileUrl") shouldNotBe null
    }

    @Test
    fun testGetInAppDescriptor_shouldBeNull_whenCampaignIdIsMissing() {
        val inapp = JSONObject()
        inapp.put("url", HTML_URL)

        MessagingServiceUtils.getInAppDescriptor(mockFileDownloader, inapp.toString()) shouldBe null
    }

    @Test
    fun testGetInAppDescriptor_shouldBeNull_whenUrlIsMissing() {
        val inapp = JSONObject()
        inapp.put("campaignId", "someId")

        MessagingServiceUtils.getInAppDescriptor(mockFileDownloader, inapp.toString()) shouldBe null
    }

    @Test
    fun testGetInAppDescriptor_shouldReturnWithUrlAndCampaignId_whenFileUrlIsNull() {
        val inapp = JSONObject()
        inapp.put("campaign_id", "someId")
        inapp.put("url", "https://thisIsNotARealUrl")
        val result =
            JSONObject(
                MessagingServiceUtils.getInAppDescriptor(
                    mockFileDownloader,
                    inapp.toString()
                )!!
            )

        result.getString("campaignId") shouldBe "someId"
        result.getString("url") shouldBe "https://thisIsNotARealUrl"
        result.has("fileUrl") shouldBe false
    }

    @Test
    fun testCreatePreloadedRemoteMessageData_shouldPutInAppDescriptorInNotificationData_whenAvailableAndInAppIsTurnedOn() {
        val inAppDescriptor = "InAppDescriptor"
        val result =
            MessagingServiceUtils.createPreloadedRemoteMessageData(
                EMPTY_NOTIFICATION_DATA,
                inAppDescriptor
            )

        result.inapp shouldBe inAppDescriptor
    }

    @Test
    fun testCreatePreloadedRemoteMessageData_shouldNotPutInAppDescriptorInNotificationData_whenNotAvailable() {
        val inAppDescriptor: String? = null
        val result =
            MessagingServiceUtils.createPreloadedRemoteMessageData(
                EMPTY_NOTIFICATION_DATA,
                inAppDescriptor
            )

        result.inapp shouldBe null
    }

    @Test
    fun testCreateSilentPushCommands_shouldReturnEmptyList_whenNoActionIsDefined() {
        val notificationData = NotificationData(
            IMAGE_URL,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = null,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null
        )

        MessagingServiceUtils.createSilentPushCommands(
            mockActionCommandFactory,
            notificationData
        ) shouldBe emptyList()
    }

    @Test
    fun testCreateSilentPushCommands_shouldCreateSilentNotificationCommand_whenMessageIsSilentAndContainsCampaignId() {
        val notificationData = NotificationData(
            IMAGE_URL,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null
        )

        MessagingServiceUtils.createSilentPushCommands(
            mockActionCommandFactory,
            notificationData
        ).size shouldBe 1
        MessagingServiceUtils.createSilentPushCommands(
            mockActionCommandFactory,
            notificationData
        )[0] is SilentNotificationInformationCommand
    }

    @Test
    fun testCreateSilentPushCommands_shouldReturnListOfCommands_whenActionIsDefined() {
        val expectedCommand1 = AppEventCommand(context, mock(), mock(), "MEAppEvent", null)
        val expectedCommand2 = AppEventCommand(
            context,
            mock(),
            mock(),
            "MEAppEvent",
            JSONObject(mapOf("key" to "value"))
        )
        whenever(mockActionCommandFactory.createActionCommand(any())).thenReturn(expectedCommand1)
            .thenReturn(expectedCommand2)
        val actions = JSONArray(
            listOf(
                JSONObject(
                    mapOf(
                        "type" to "MEAppEvent",
                        "name" to "nameOfTheAppEvent"
                    )
                ),
                JSONObject(
                    mapOf(
                        "type" to "MEAppEvent",
                        "name" to "nameOfTheAppEvent",
                        "payload" to JSONObject(
                            mapOf(
                                "key" to "value"
                            )
                        )
                    )
                )
            )
        ).toString()

        val notificationData = NotificationData(
            IMAGE_URL,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = null,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = actions,
            inapp = null
        )

        MessagingServiceUtils.createSilentPushCommands(
            mockActionCommandFactory,
            notificationData
        ) shouldBe listOf(
            expectedCommand1,
            expectedCommand2
        )
    }

    @Test
    fun testCreateSilentPushCommands_shouldReturnListOfCommands_whenListenerIsRegistered() {
        val expectedCommand1 = AppEventCommand(context, mock(), mock(), "MEAppEvent", null)
        val expectedCommand2 = AppEventCommand(
            context,
            mock(),
            mock(),
            "MEAppEvent",
            JSONObject(mapOf("key" to "value"))
        )
        whenever(mockActionCommandFactory.createActionCommand(any())).thenReturn(expectedCommand1)
            .thenReturn(expectedCommand2)
        val actions = JSONArray(
            listOf(
                JSONObject(
                    mapOf(
                        "type" to "MEAppEvent",
                        "name" to "nameOfTheAppEvent"
                    )
                ),
                JSONObject(
                    mapOf(
                        "type" to "MEAppEvent",
                        "name" to "nameOfTheAppEvent",
                        "payload" to JSONObject(
                            mapOf(
                                "key" to "value"
                            )
                        )
                    )
                )
            )
        ).toString()
        val notificationData = NotificationData(
            IMAGE_URL,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = null,
            sid = SID,
            smallIconResourceId = SMALL_NOTIFICATION_ICON,
            colorResourceId = COLOR,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = actions,
            inapp = null
        )

        MessagingServiceUtils.createSilentPushCommands(
            mockActionCommandFactory,
            notificationData
        ) shouldBe listOf(
            expectedCommand1,
            expectedCommand2
        )
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
        val mockBuilder: NotificationCompat.Builder = mock {
            on { setLargeIcon(any()) } doReturn it
            on { setContentTitle(any()) } doReturn it
            on { setContentText(any()) } doReturn it
            on { setStyle(any()) } doReturn it
        }
        mockBuilder.styleNotification(
            NotificationData(
                IMAGE_URL,
                null,
                "THUMBNAIL",
                TITLE,
                BODY,
                CHANNEL_ID,
                campaignId = MULTICHANNEL_ID,
                sid = SID,
                smallIconResourceId = SMALL_NOTIFICATION_ICON,
                colorResourceId = COLOR,
                collapseId = COLLAPSE_ID,
                operation = OPERATION,
                actions = null,
                inapp = null
            ),
            mockFileDownloader,
            deviceInfo
        )

        verify(mockBuilder).setStyle(any<NotificationCompat.BigTextStyle>())
    }

    @Test
    fun testStyleNotification_whenStyleIsBigPicture() {
        "BIG_PICTURE" shouldCreateNotificationWithStyle NotificationCompat.BigPictureStyle::class.java
    }

    @Test
    fun testStyleNotification_whenStyleIsInvalid_imageIsNotSet_shouldBeBigTextStyle() {
        val mockBuilder: NotificationCompat.Builder = mock()

        mockBuilder.styleNotification(
            NotificationData(
                null,
                null,
                "INVALID_STYLE",
                TITLE,
                BODY,
                CHANNEL_ID,
                campaignId = MULTICHANNEL_ID,
                sid = SID,
                smallIconResourceId = SMALL_NOTIFICATION_ICON,
                colorResourceId = COLOR,
                collapseId = COLLAPSE_ID,
                operation = OPERATION,
                actions = null,
                inapp = null
            ),
            mockFileDownloader,
            deviceInfo
        )

        verify(mockBuilder).setStyle(any<NotificationCompat.BigTextStyle>())
    }

    @Test
    fun testStyleNotification_whenStyleIsInvalid_imageIsSet_shouldBeBigPictureStyle() {
        "INVALID_STYLE" shouldCreateNotificationWithStyle NotificationCompat.BigPictureStyle::class.java
    }

    private inline infix fun <reified T : NotificationCompat.Style> String.shouldCreateNotificationWithStyle(
        ignored: Class<T>
    ) {
        val mockBuilder: NotificationCompat.Builder = mock {
            on { setLargeIcon(org.mockito.kotlin.any()) } doReturn it
            on { setStyle(org.mockito.kotlin.any()) } doReturn it
        }

        mockBuilder.styleNotification(
            NotificationData(
                IMAGE_URL,
                ICON_URL,
                this,
                TITLE,
                BODY,
                CHANNEL_ID,
                campaignId = MULTICHANNEL_ID,
                sid = SID,
                smallIconResourceId = SMALL_NOTIFICATION_ICON,
                colorResourceId = COLOR,
                collapseId = COLLAPSE_ID,
                operation = OPERATION,
                actions = null,
                inapp = null
            ),
            mockFileDownloader,
            deviceInfo
        )

        verify(mockBuilder).setStyle(any<T>())
    }

    private fun createInAppInPayload(): String {
        val inapp = JSONObject()
        inapp.put("campaign_id", "someId")
        inapp.put("url", HTML_URL)

        return inapp.toString()
    }
}