package com.emarsys.mobileengage.service

import android.R
import android.app.Notification
import android.content.Context
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
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.resource.MetaDataReader
import com.emarsys.mobileengage.inbox.InboxParseUtils
import com.emarsys.mobileengage.inbox.model.NotificationCache
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.ReflectionTestUtils.instantiate
import com.emarsys.testUtil.RetryUtils.retryRule
import com.emarsys.testUtil.TestUrls.LARGE_IMAGE
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import com.google.firebase.messaging.RemoteMessage
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.*

class MessagingServiceUtilsTest {
    companion object {
        private const val TITLE = "title"
        private const val DEFAULT_TITLE = "This is a default title"
        private const val BODY = "body"
        private const val CHANNEL_ID = "channelId"
        private const val HARDWARE_ID = "hwid"
        private const val SDK_VERSION = "sdkVersion"
        private const val LANGUAGE = "en-US"
    }

    private lateinit var context: Context
    private lateinit var deviceInfo: DeviceInfo
    private lateinit var metaDataReader: MetaDataReader
    private lateinit var mockNotificationCache: NotificationCache
    private lateinit var mockTimestampProvider: TimestampProvider

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    var retry: TestRule = retryRule

    @Before
    fun init() {
        context = getTargetContext()
        val mockNotificationSettings = Mockito.mock(NotificationSettings::class.java)
        val mockHardwareIdProvider = Mockito.mock(HardwareIdProvider::class.java)
        val mockVersionProvider = Mockito.mock(VersionProvider::class.java)
        val mockLanguageProvider = Mockito.mock(LanguageProvider::class.java)
        val channelSettings = ChannelSettings(channelId = CHANNEL_ID)
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
        metaDataReader = Mockito.mock(MetaDataReader::class.java)
        mockNotificationCache = Mockito.mock(NotificationCache::class.java)
        mockTimestampProvider = Mockito.mock(TimestampProvider::class.java)
        whenever(mockTimestampProvider.provideTimestamp()).thenReturn(1L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testHandleMessage_contextShouldNotBeNull() {
        MessagingServiceUtils.handleMessage(null, createEMSRemoteMessage(), deviceInfo, mockNotificationCache, mockTimestampProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testHandleMessage_remoteMessageShouldNotBeNull() {
        MessagingServiceUtils.handleMessage(context, null, deviceInfo, mockNotificationCache, mockTimestampProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testHandleMessage_deviceInfoShouldNotBeNull() {
        MessagingServiceUtils.handleMessage(context, createEMSRemoteMessage(), null, mockNotificationCache, mockTimestampProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testHandleMessage_notificationCacheShouldNotBeNull() {
        MessagingServiceUtils.handleMessage(context, createRemoteMessage(), deviceInfo, null, mockTimestampProvider)
    }

    @Test
    fun testHandleMessage_shouldReturnFalse_ifMessageIsNotHandled() {
        MessagingServiceUtils.handleMessage(context, createRemoteMessage(), deviceInfo, mockNotificationCache, mockTimestampProvider) shouldBe false
    }

    @Test
    fun testHandleMessage_shouldReturnTrue_ifMessageIsHandled() {
        MessagingServiceUtils.handleMessage(context, createEMSRemoteMessage(), deviceInfo, mockNotificationCache, mockTimestampProvider) shouldBe true
    }

    @Test
    fun testIsMobileEngageMessage_shouldBeFalse_withEmptyData() {
        val remoteMessageData: Map<String, String> = HashMap()
        MessagingServiceUtils.isMobileEngageMessage(remoteMessageData) shouldBe false
    }

    @Test
    fun testIsMobileEngageMessage_shouldBeTrue_withDataWhichContainsTheCorrectKey() {
        val remoteMessageData: MutableMap<String, String> = HashMap()
        remoteMessageData["ems_msg"] = "value"
        MessagingServiceUtils.isMobileEngageMessage(remoteMessageData) shouldBe true
    }

    @Test
    fun testIsMobileEngageMessage_shouldBeFalse_withDataWithout_ems_msg() {
        val remoteMessageData: MutableMap<String, String> = HashMap()
        remoteMessageData["key1"] = "value1"
        remoteMessageData["key2"] = "value2"
        MessagingServiceUtils.isMobileEngageMessage(remoteMessageData) shouldBe false
    }

    @Test
    fun createNotification_shouldNotBeNull() {
        MessagingServiceUtils.createNotification(
                0,
                context,
                HashMap(),
                deviceInfo,
                metaDataReader) shouldNotBe null
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.LOLLIPOP)
    fun createNotification_withBigTextStyle_withTitleAndBody() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["channel_id"] = CHANNEL_ID
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader)
        result.extras.getString(NotificationCompat.EXTRA_TITLE) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe null
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.LOLLIPOP)
    fun createNotification_withBigTextStyle_withTitle_withoutBody() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["channel_id"] = CHANNEL_ID
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader)
        result.extras.getString(NotificationCompat.EXTRA_TITLE) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe null
        result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT) shouldBe null
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe null
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.LOLLIPOP)
    fun createNotification_withBigTextStyle_withoutTitle_withBody() {
        val input: MutableMap<String, String> = HashMap()
        input["body"] = BODY
        input["channel_id"] = CHANNEL_ID
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader)
        val expectedTitle = expectedBasedOnApiLevel(applicationName, "")

        result.extras.getString(NotificationCompat.EXTRA_TITLE) shouldBe expectedTitle
        result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG) shouldBe expectedTitle
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe null
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.LOLLIPOP)
    fun createNotification_withBigTextStyle_withoutTitle_withBody_withDefaultTitle() {
        val input: MutableMap<String, String> = HashMap()
        input["body"] = BODY
        input["u"] = "{\"test_field\":\"\",\"ems_default_title\":\"$DEFAULT_TITLE\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}"
        input["channel_id"] = CHANNEL_ID
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader)
        val expectedTitle = expectedBasedOnApiLevel(DEFAULT_TITLE, "")

        result.extras.getString(NotificationCompat.EXTRA_TITLE) shouldBe expectedTitle
        result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG) shouldBe expectedTitle
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe null
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.LOLLIPOP)
    fun testCreateNotification_withBigPictureStyle_whenImageIsAvailable() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["image_url"] = LARGE_IMAGE
        input["channel_id"] = CHANNEL_ID
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader)

        result.extras.getString(NotificationCompat.EXTRA_TITLE) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe BODY
        result.extras[NotificationCompat.EXTRA_PICTURE] shouldNotBe null
        result.extras[NotificationCompat.EXTRA_LARGE_ICON] shouldNotBe null
        result.extras[NotificationCompat.EXTRA_LARGE_ICON_BIG] shouldBe null
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.LOLLIPOP)
    fun testCreateNotification_withBigTextStyle_whenImageCannotBeLoaded() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["image_url"] = "https://fa.il/img.jpg"
        input["channel_id"] = CHANNEL_ID
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader)

        result.extras.getString(NotificationCompat.EXTRA_TITLE) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG) shouldBe TITLE
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT) shouldBe BODY
        result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) shouldBe null
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.LOLLIPOP)
    fun testCreateNotification_setsNotificationColor() {
        val colorResourceId = R.color.darker_gray
        val expectedColor = ContextCompat.getColor(context, colorResourceId)
        whenever(metaDataReader.getInt(ArgumentMatchers.any(Context::class.java), ArgumentMatchers.any(String::class.java))).thenReturn(colorResourceId)
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader)

        result.color shouldBe expectedColor
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.LOLLIPOP)
    fun testCreateNotification_doesNotSet_notificationColor_when() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader)

        result.color shouldBe Notification.COLOR_DEFAULT
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    fun testCreateNotification_withChannelId() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["channel_id"] = CHANNEL_ID
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader)

        result.channelId shouldBe CHANNEL_ID
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    fun testCreateNotification_withoutChannelId() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        val notificationSettings = Mockito.mock(NotificationSettings::class.java)
        val deviceInfo = Mockito.mock(DeviceInfo::class.java)
        val channelSettings = ChannelSettings(channelId = "notMatchingChannelId")
        whenever(notificationSettings.channelSettings).thenReturn(listOf(channelSettings))
        whenever(deviceInfo.notificationSettings).thenReturn(notificationSettings)
        whenever(deviceInfo.isDebugMode).thenReturn(false)
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader)

        result.channelId shouldBe null
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    fun testCreateNotification_withoutChannelId_inDebugMode() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        val notificationSettings = Mockito.mock(NotificationSettings::class.java)
        val deviceInfo = Mockito.mock(DeviceInfo::class.java)
        val channelSettings = ChannelSettings(channelId = "notMatchingChannelId")
        whenever(notificationSettings.channelSettings).thenReturn(listOf(channelSettings))
        whenever(deviceInfo.notificationSettings).thenReturn(notificationSettings)
        whenever(deviceInfo.isDebugMode).thenReturn(true)
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader)
        result.channelId shouldBe "ems_debug"
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.KITKAT)
    @Throws(JSONException::class)
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
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader)
        result.actions shouldNotBe null
        result.actions.size shouldBe 2
        result.actions[0].title shouldBe "title1"
        result.actions[1].title shouldBe "title2"
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.KITKAT)
    fun testCreateNotification_action_withoutActions() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader)
        result.actions shouldBe null
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    @Throws(Exception::class)
    fun testCreateNotification_returnDebugMessage_whenThereIsChannelIdMismatch() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["channel_id"] = CHANNEL_ID
        val notificationSettings = Mockito.mock(NotificationSettings::class.java)
        val deviceInfo = Mockito.mock(DeviceInfo::class.java)
        val channelSettings = ChannelSettings(channelId = "notMatchingChannelId")
        whenever(notificationSettings.channelSettings).thenReturn(listOf(channelSettings))
        whenever(deviceInfo.notificationSettings).thenReturn(notificationSettings)
        whenever(deviceInfo.isDebugMode).thenReturn(true)
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader)
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe "DEBUG - channel_id mismatch: channelId not found!"
    }

    @Test
    @Throws(Exception::class)
    fun testCreateNotification_returnOriginalTitle_evenIfThereIsChannelMismatch_but_weAreNotInDebugMode() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        input["body"] = BODY
        input["channel_id"] = CHANNEL_ID
        val notificationSettings = Mockito.mock(NotificationSettings::class.java)
        val channelSettings = ChannelSettings(channelId = "notMatchingChannelId")
        val deviceInfo = Mockito.mock(DeviceInfo::class.java)
        whenever(notificationSettings.channelSettings).thenReturn(listOf(channelSettings))
        whenever(deviceInfo.notificationSettings).thenReturn(notificationSettings)
        whenever(deviceInfo.isDebugMode).thenReturn(false)
        val result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader)
        result.extras.getString(NotificationCompat.EXTRA_TEXT) shouldBe BODY
    }

    @Test
    fun testGetTitle_withTitleSet() {
        val input: MutableMap<String, String> = HashMap()
        input["title"] = TITLE
        MessagingServiceUtils.getTitle(input, context) shouldBe TITLE
    }

    @Test
    fun testGetTitle_shouldReturnAppName_whenTitleNotSet() {
        val input: MutableMap<String, String> = HashMap()
        input["key1"] = "value1"
        input["key2"] = "value2"
        val expectedBefore23 = applicationName
        val expectedFrom23 = ""
        val expected = expectedBasedOnApiLevel(expectedBefore23, expectedFrom23)
        MessagingServiceUtils.getTitle(input, context) shouldBe expected
    }

    @Test
    fun testGetTitle_shouldReturnAppName_whenTitleIsEmpty() {
        val input: MutableMap<String, String> = HashMap()
        input["key1"] = "value1"
        input["key2"] = "value2"
        input["title"] = ""
        val expectedBefore23 = applicationName
        val expectedFrom23 = ""
        val expected = expectedBasedOnApiLevel(expectedBefore23, expectedFrom23)
        MessagingServiceUtils.getTitle(input, context) shouldBe expected
    }

    @Test
    fun testGetTitle_shouldReturnDefaultTitle_whenDefaultTitleSet() {
        val input: MutableMap<String, String> = HashMap()
        input["key1"] = "value1"
        input["key2"] = "value2"
        input["u"] = "{\"test_field\":\"\",\"ems_default_title\":\"$DEFAULT_TITLE\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}"
        val expectedFrom23 = ""
        val expected = expectedBasedOnApiLevel(DEFAULT_TITLE, expectedFrom23)

        MessagingServiceUtils.getTitle(input, context) shouldBe expected
    }

    @Test
    fun testGetTitle_defaultTitleShouldNotOverrideTitle() {
        val input: MutableMap<String, String> = HashMap()
        input["key1"] = "value1"
        input["key2"] = "value2"
        input["title"] = TITLE
        input["u"] = "{\"test_field\":\"\",\"ems_default_title\":\"$DEFAULT_TITLE\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}"

        MessagingServiceUtils.getTitle(input, context) shouldBe TITLE
    }

    @Test
    fun testGetInAppDescriptor_shouldReturnNull_forNullInput() {
        MessagingServiceUtils.getInAppDescriptor(context, null) shouldBe null
    }

    @Test
    fun testGetInAppDescriptor_shouldReturnNull_whenThereIsNoEmsInPayload() {
        MessagingServiceUtils.getInAppDescriptor(context, createNoEmsInPayload()) shouldBe null
    }

    @Test
    fun testGetInAppDescriptor_shouldReturnNull_whenThereIsNoInAppInPayload() {
        MessagingServiceUtils.getInAppDescriptor(context, createNoInAppInPayload()) shouldBe null
    }

    @Test
    @Throws(JSONException::class)
    fun testGetInAppDescriptor_shouldReturnValidDescriptor_whenThereIsInAppInPayload() {
        val result = JSONObject(MessagingServiceUtils.getInAppDescriptor(context, createInAppInPayload()))

        result.getString("campaignId") shouldBe "someId"
        result.getString("url") shouldBe "https://hu.wikipedia.org/wiki/Mont_Blanc"
        result.getString("fileUrl") shouldNotBe null
    }

    @Test
    @Throws(JSONException::class)
    fun testGetInAppDescriptor_shouldBeNull_whenCampaignIdIsMissing() {
        val payload: MutableMap<String, String> = HashMap()
        val ems = JSONObject()
        val inapp = JSONObject()
        inapp.put("url", "https://hu.wikipedia.org/wiki/Mont_Blanc")
        ems.put("inapp", inapp)
        payload["ems"] = ems.toString()
        MessagingServiceUtils.getInAppDescriptor(context, payload) shouldBe null
    }

    @Test
    @Throws(JSONException::class)
    fun testGetInAppDescriptor_shouldBeNull_whenUrlIsMissing() {
        val payload: MutableMap<String, String> = HashMap()
        val ems = JSONObject()
        val inapp = JSONObject()
        inapp.put("campaignId", "someId")
        ems.put("inapp", inapp)
        payload["ems"] = ems.toString()

        MessagingServiceUtils.getInAppDescriptor(context, payload) shouldBe null
    }

    @Test
    @Throws(JSONException::class)
    fun testGetInAppDescriptor_shouldReturnWithUrlAndCampaignId_whenFileUrlIsNull() {
        val payload: MutableMap<String, String> = HashMap()
        val ems = JSONObject()
        val inapp = JSONObject()
        inapp.put("campaign_id", "someId")
        inapp.put("url", "https://thisIsNotARealUrl")
        ems.put("inapp", inapp)
        payload["ems"] = ems.toString()
        val result = JSONObject(MessagingServiceUtils.getInAppDescriptor(context, payload))

        result.getString("campaignId") shouldBe "someId"
        result.getString("url") shouldBe "https://thisIsNotARealUrl"
        result.has("fileUrl") shouldBe false
    }

    @Test
    @SdkSuppress(minSdkVersion = VERSION_CODES.KITKAT)
    @Throws(JSONException::class)
    fun testCreatePreloadedRemoteMessageData_shouldPutInAppDescriptorUnderEms_whenAvailableAndInAppIsTurnedOn() {
        val inAppDescriptor = "InAppDescriptor"
        val inAppPayload = createNoInAppInPayload()
        val result = MessagingServiceUtils.createPreloadedRemoteMessageData(inAppPayload, inAppDescriptor)

        JSONObject(result["ems"]).getString("inapp") shouldBe inAppDescriptor
    }

    @Test
    @Throws(JSONException::class)
    fun testCreatePreloadedRemoteMessageData_shouldNotPutInAppDescriptorUnderEms_whenNotAvailable() {
        val inAppDescriptor: String? = null
        val inAppPayload = createNoInAppInPayload()
        val result = MessagingServiceUtils.createPreloadedRemoteMessageData(inAppPayload, inAppDescriptor)

        JSONObject(result["ems"]).has("inapp") shouldBe false
    }

    @Test
    fun testCacheNotification_shouldCacheNotification() {
        val remoteData: MutableMap<String, String> = HashMap()
        remoteData["ems_msg"] = "true"
        remoteData["u"] = "{\"test_field\":\"\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}"
        remoteData["id"] = "21022.150123121212.43223434c3b9"
        remoteData["inbox"] = "true"
        remoteData["title"] = "hello there"
        remoteData["rootParam1"] = "param_param"
        val customData: MutableMap<String, String> = HashMap()
        customData["test_field"] = ""
        customData["image"] = "https://media.giphy.com/media/ktvFa67wmjDEI/giphy.gif"
        customData["deep_link"] = "lifestylelabels.com/mobile/product/3245678"
        customData["sid"] = "sid_here"
        MessagingServiceUtils.cacheNotification(mockTimestampProvider, mockNotificationCache, remoteData)
        val notification = InboxParseUtils.parseNotificationFromPushMessage(mockTimestampProvider, false, remoteData)
        Mockito.verify(mockNotificationCache).cache(notification)
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
        return HashMap()
    }

    private fun createNoInAppInPayload(): Map<String, String> {
        val payload: MutableMap<String, String> = HashMap()
        payload["ems"] = "{}"
        return payload
    }

    @Throws(JSONException::class)
    private fun createInAppInPayload(): Map<String, String> {
        val payload: MutableMap<String, String> = HashMap()
        val ems = JSONObject()
        val inapp = JSONObject()
        inapp.put("campaign_id", "someId")
        inapp.put("url", "https://hu.wikipedia.org/wiki/Mont_Blanc")
        ems.put("inapp", inapp)
        payload["ems"] = ems.toString()
        return payload
    }

    private fun expectedBasedOnApiLevel(before23: String, fromApi23: String): String {
        return if (Build.VERSION.SDK_INT < 23) {
            before23
        } else {
            fromApi23
        }
    }

    private val applicationName: String
        private get() {
            val applicationInfo = context.applicationInfo
            val stringId = applicationInfo.labelRes
            return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(stringId)
        }
}