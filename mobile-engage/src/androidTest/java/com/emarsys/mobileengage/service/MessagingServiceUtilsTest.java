package com.emarsys.mobileengage.service;

import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.provider.hardwareid.HardwareIdProvider;
import com.emarsys.core.resource.MetaDataReader;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.ReflectionTestUtils;
import com.emarsys.testUtil.TimeoutUtils;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static com.emarsys.testUtil.TestUrls.LARGE_IMAGE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MessagingServiceUtilsTest {

    static {
        mock(Context.class);
        mock(PackageManager.class);
        mock(NotificationManager.class);
    }

    private static final String TITLE = "title";
    private static final String DEFAULT_TITLE = "This is a default title";
    private static final String BODY = "body";
    private static final String CHANNEL_ID = "channelId";
    private static final String SDK_VERSION = "sdkVersion";

    private Context context;
    private DeviceInfo deviceInfo;
    private List<Notification> notificationCache;
    private MetaDataReader metaDataReader;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() throws Exception {
        context = InstrumentationRegistry.getTargetContext();

        deviceInfo = new DeviceInfo(context, mock(HardwareIdProvider.class), SDK_VERSION);

        Field cacheField = NotificationCache.class.getDeclaredField("internalCache");
        cacheField.setAccessible(true);
        notificationCache = (List) cacheField.get(null);
        notificationCache.clear();

        metaDataReader = mock(MetaDataReader.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleMessage_contextShouldNotBeNull() {
        MessagingServiceUtils.handleMessage(null, createEMSRemoteMessage(), deviceInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleMessage_remoteMessageShouldNotBeNull() {
        MessagingServiceUtils.handleMessage(context, null, deviceInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleMessage_deviceInfoShouldNotBeNull() {
        MessagingServiceUtils.handleMessage(context, createEMSRemoteMessage(), null);
    }

    @Test
    public void testHandleMessage_shouldReturnFalse_ifMessageIsNotHandled() {
        assertFalse(MessagingServiceUtils.handleMessage(context, createRemoteMessage(), deviceInfo));
    }

    @Test
    public void testHandleMessage_shouldReturnTrue_ifMessageIsHandled() {
        assertTrue(MessagingServiceUtils.handleMessage(context, createEMSRemoteMessage(), deviceInfo));
    }

    @Test
    public void testIsMobileEngageMessage_shouldBeFalse_withEmptyData() {
        Map<String, String> remoteMessageData = new HashMap<>();
        assertFalse(MessagingServiceUtils.isMobileEngageMessage(remoteMessageData));
    }

    @Test
    public void testIsMobileEngageMessage_shouldBeTrue_withDataWhichContainsTheCorrectKey() {
        Map<String, String> remoteMessageData = new HashMap<>();
        remoteMessageData.put("ems_msg", "value");
        assertTrue(MessagingServiceUtils.isMobileEngageMessage(remoteMessageData));
    }

    @Test
    public void testIsMobileEngageMessage_shouldBeFalse_withDataWithout_ems_msg() {
        Map<String, String> remoteMessageData = new HashMap<>();
        remoteMessageData.put("key1", "value1");
        remoteMessageData.put("key2", "value2");
        assertFalse(MessagingServiceUtils.isMobileEngageMessage(remoteMessageData));
    }

    @Test
    public void createNotification_shouldNotBeNull() {
        assertNotNull(MessagingServiceUtils.createNotification(
                0,
                context,
                new HashMap<String, String>(),
                deviceInfo,
                metaDataReader));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void createNotification_withBigTextStyle_withTitleAndBody() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);

        android.app.Notification result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader);

        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_TEXT));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT));

        assertNull(result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void createNotification_withBigTextStyle_withTitle_withoutBody() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);

        android.app.Notification result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader);

        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));

        assertNull(result.extras.getString(NotificationCompat.EXTRA_TEXT));
        assertNull(result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT));
        assertNull(result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void createNotification_withBigTextStyle_withoutTitle_withBody() {
        Map<String, String> input = new HashMap<>();
        input.put("body", BODY);

        android.app.Notification result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader);

        String expectedTitle = expectedBasedOnApiLevel(getApplicationName(), "");

        assertEquals(expectedTitle, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(expectedTitle, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_TEXT));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT));

        assertNull(result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void createNotification_withBigTextStyle_withoutTitle_withBody_withDefaultTitle() {
        Map<String, String> input = new HashMap<>();
        input.put("body", BODY);
        input.put("u", "{\"test_field\":\"\",\"ems_default_title\":\"" + DEFAULT_TITLE + "\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}");

        android.app.Notification result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader);

        String expectedTitle = expectedBasedOnApiLevel(DEFAULT_TITLE, "");

        assertEquals(expectedTitle, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(expectedTitle, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_TEXT));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT));

        assertNull(result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void testCreateNotification_withBigPictureStyle_whenImageIsAvailable() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);
        input.put("image_url", LARGE_IMAGE);

        android.app.Notification result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader);

        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT));

        assertNotNull(result.extras.get(NotificationCompat.EXTRA_PICTURE));
        assertNotNull(result.extras.get(NotificationCompat.EXTRA_LARGE_ICON));

        assertNull(result.extras.get(NotificationCompat.EXTRA_LARGE_ICON_BIG));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void testCreateNotification_withBigTextStyle_whenImageCannotBeLoaded() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);
        input.put("image_url", "https://fa.il/img.jpg");

        android.app.Notification result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader);

        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_TEXT));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT));

        assertNull(result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void testCreateNotification_setsNotificationColor() {
        int colorResourceId = android.R.color.darker_gray;
        int expectedColor = ContextCompat.getColor(context, colorResourceId);
        when(metaDataReader.getInt(any(Context.class), any(String.class))).thenReturn(colorResourceId);

        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);

        android.app.Notification result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader);
        assertEquals(expectedColor, result.color);
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void testCreateNotification_doesNotSet_notificationColor_when() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);

        android.app.Notification result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader);
        assertEquals(android.app.Notification.COLOR_DEFAULT, result.color);
    }

    @Test
    @SdkSuppress(minSdkVersion = O)
    public void testCreateNotification_withChannelId() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);
        input.put("channel_id", CHANNEL_ID);

        android.app.Notification result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader);

        assertEquals(CHANNEL_ID, result.getChannelId());
    }

    @Test
    @SdkSuppress(minSdkVersion = O)
    public void testCreateNotification_withoutChannelId() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);

        android.app.Notification result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader);

        assertNull(result.getChannelId());
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateNotification_setsActionsIfAvailable() throws JSONException {
        JSONObject ems = new JSONObject()
                .put("actions", new JSONArray()
                        .put(new JSONObject()
                                .put("id", "uniqueActionId1")
                                .put("title", "title1")
                                .put("type", "MEAppEvent")
                                .put("name", "event1")
                        )
                        .put(new JSONObject()
                                .put("id", "uniqueActionId2")
                                .put("title", "title2")
                                .put("type", "MEAppEvent")
                                .put("name", "event2")
                                .put("payload", new JSONObject()
                                        .put("payloadKey", "payloadValue"))
                        ));

        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);
        input.put("ems", ems.toString());

        android.app.Notification result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader);

        assertNotNull(result.actions);
        assertEquals(2, result.actions.length);
        assertEquals("title1", result.actions[0].title);
        assertEquals("title2", result.actions[1].title);
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateNotification_action_withoutActions() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);

        android.app.Notification result = MessagingServiceUtils.createNotification(
                0,
                context,
                input,
                deviceInfo,
                metaDataReader);
        assertNull(result.actions);
    }

    @Test
    public void testGetTitle_withTitleSet() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);

        assertEquals(TITLE, MessagingServiceUtils.getTitle(input, context));
    }

    @Test
    public void testGetTitle_shouldReturnAppName_whenTitleNotSet() {
        Map<String, String> input = new HashMap<>();
        input.put("key1", "value1");
        input.put("key2", "value2");

        String expectedBefore23 = getApplicationName();

        String expectedFrom23 = "";

        String expected = expectedBasedOnApiLevel(expectedBefore23, expectedFrom23);

        assertEquals(expected, MessagingServiceUtils.getTitle(input, context));
    }

    @Test
    public void testGetTitle_shouldReturnAppName_whenTitleIsEmpty() {
        Map<String, String> input = new HashMap<>();
        input.put("key1", "value1");
        input.put("key2", "value2");
        input.put("title", "");

        String expectedBefore23 = getApplicationName();

        String expectedFrom23 = "";

        String expected = expectedBasedOnApiLevel(expectedBefore23, expectedFrom23);

        assertEquals(expected, MessagingServiceUtils.getTitle(input, context));
    }

    @Test
    public void testGetTitle_shouldReturnDefaultTitle_whenDefaultTitleSet() {
        Map<String, String> input = new HashMap<>();
        input.put("key1", "value1");
        input.put("key2", "value2");
        input.put("u", "{\"test_field\":\"\",\"ems_default_title\":\"" + DEFAULT_TITLE + "\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}");

        String expectedFrom23 = "";

        String expected = expectedBasedOnApiLevel(DEFAULT_TITLE, expectedFrom23);

        assertEquals(expected, MessagingServiceUtils.getTitle(input, context));
    }

    @Test
    public void testGetTitle_defaultTitleShouldNotOverrideTitle() {
        Map<String, String> input = new HashMap<>();
        input.put("key1", "value1");
        input.put("key2", "value2");
        input.put("title", TITLE);
        input.put("u", "{\"test_field\":\"\",\"ems_default_title\":\"" + DEFAULT_TITLE + "\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}");

        assertEquals(TITLE, MessagingServiceUtils.getTitle(input, context));
    }

    @Test
    public void testGetInAppDescriptor_shouldReturnNull_forNullInput() {
        assertNull(MessagingServiceUtils.getInAppDescriptor(context, null));
    }

    @Test
    public void testGetInAppDescriptor_shouldReturnNull_whenThereIsNoEmsInPayload() {
        assertNull(MessagingServiceUtils.getInAppDescriptor(context, createNoEmsInPayload()));
    }


    @Test
    public void testGetInAppDescriptor_shouldReturnNull_whenThereIsNoInAppInPayload() {
        assertNull(MessagingServiceUtils.getInAppDescriptor(context, createNoInAppInPayload()));
    }


    @Test
    public void testGetInAppDescriptor_shouldReturnValidDescriptor_whenThereIsInAppInPayload() throws JSONException {
        JSONObject result = new JSONObject(MessagingServiceUtils.getInAppDescriptor(context, createInAppInPayload()));
        assertEquals("someId", result.getString("campaignId"));
        assertEquals("https://hu.wikipedia.org/wiki/Mont_Blanc", result.getString("url"));
        assertNotNull(result.getString("fileUrl"));
    }

    @Test
    public void testGetInAppDescriptor_shouldBeNull_whenCampaignIdIsMissing() throws JSONException {
        Map<String, String> payload = new HashMap<>();
        JSONObject ems = new JSONObject();
        JSONObject inapp = new JSONObject();
        inapp.put("url", "https://hu.wikipedia.org/wiki/Mont_Blanc");
        ems.put("inapp", inapp);
        payload.put("ems", ems.toString());

        assertNull(MessagingServiceUtils.getInAppDescriptor(context, payload));
    }

    @Test
    public void testGetInAppDescriptor_shouldBeNull_whenUrlIsMissing() throws JSONException {
        Map<String, String> payload = new HashMap<>();
        JSONObject ems = new JSONObject();
        JSONObject inapp = new JSONObject();
        inapp.put("campaignId", "someId");
        ems.put("inapp", inapp);
        payload.put("ems", ems.toString());

        assertNull(MessagingServiceUtils.getInAppDescriptor(context, payload));
    }

    @Test
    public void testGetInAppDescriptor_shouldReturnWithUrlAndCampaignId_whenFileUrlIsNull() throws JSONException {
        Map<String, String> payload = new HashMap<>();
        JSONObject ems = new JSONObject();
        JSONObject inapp = new JSONObject();
        inapp.put("campaignId", "someId");
        inapp.put("url", "https://thisIsNotARealUrl");
        ems.put("inapp", inapp);
        payload.put("ems", ems.toString());

        JSONObject result = new JSONObject(MessagingServiceUtils.getInAppDescriptor(context, payload));
        assertEquals("someId", result.getString("campaignId"));
        assertEquals("https://thisIsNotARealUrl", result.getString("url"));
        assertEquals(false, result.has("fileUrl"));
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreatePreloadedRemoteMessageData_shouldPutInAppDescriptorUnderEms_whenAvailableAndInAppIsTurnedOn() throws JSONException {
        String inAppDescriptor = "InAppDescriptor";
        Map<String, String> inAppPayload = createNoInAppInPayload();
        Map<String, String> result = MessagingServiceUtils.createPreloadedRemoteMessageData(inAppPayload, inAppDescriptor);

        assertEquals(inAppDescriptor, new JSONObject(result.get("ems")).getString("inapp"));
    }

    @Test
    public void testCreatePreloadedRemoteMessageData_shouldNotPutInAppDescriptorUnderEms_whenNotAvailable() throws JSONException {
        String inAppDescriptor = null;
        Map<String, String> inAppPayload = createNoInAppInPayload();
        Map<String, String> result = MessagingServiceUtils.createPreloadedRemoteMessageData(inAppPayload, inAppDescriptor);

        assertEquals(false, new JSONObject(result.get("ems")).has("inapp"));
    }

    @Test
    public void testCacheNotification_shouldCacheNotification() {
        Map<String, String> remoteData = new HashMap<>();
        remoteData.put("ems_msg", "true");
        remoteData.put("u", "{\"test_field\":\"\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}");
        remoteData.put("id", "21022.150123121212.43223434c3b9");
        remoteData.put("inbox", "true");
        remoteData.put("title", "hello there");
        remoteData.put("rootParam1", "param_param");

        Map<String, String> customData = new HashMap<>();
        customData.put("test_field", "");
        customData.put("image", "https://media.giphy.com/media/ktvFa67wmjDEI/giphy.gif");
        customData.put("deep_link", "lifestylelabels.com/mobile/product/3245678");
        customData.put("sid", "sid_here");

        long before = System.currentTimeMillis();
        MessagingServiceUtils.cacheNotification(remoteData);
        long after = System.currentTimeMillis();

        assertEquals(1, notificationCache.size());

        Notification result = notificationCache.get(0);

        assertEquals("21022.150123121212.43223434c3b9", result.getId());
        assertEquals("sid_here", result.getSid());
        assertEquals("hello there", result.getTitle());
        assertEquals(customData, result.getCustomData());
        Assert.assertTrue(before <= result.getReceivedAt());
        Assert.assertTrue(result.getReceivedAt() <= after);
    }

    private RemoteMessage createRemoteMessage() {
        Bundle bundle = new Bundle();
        bundle.putString("title", "title");
        bundle.putString("body", "body");
        return ReflectionTestUtils.instantiate(RemoteMessage.class, 0, bundle);
    }

    private RemoteMessage createEMSRemoteMessage() {
        Bundle bundle = new Bundle();
        bundle.putString("title", "title");
        bundle.putString("body", "body");
        bundle.putString("ems_msg", "value");
        return ReflectionTestUtils.instantiate(RemoteMessage.class, 0, bundle);
    }

    private Map<String, String> createNoEmsInPayload() {
        Map<String, String> payload = new HashMap<>();
        return payload;
    }

    private Map<String, String> createNoInAppInPayload() {
        Map<String, String> payload = new HashMap<>();
        payload.put("ems", "{}");
        return payload;
    }

    private Map<String, String> createInAppInPayload() throws JSONException {
        Map<String, String> payload = new HashMap<>();
        JSONObject ems = new JSONObject();
        JSONObject inapp = new JSONObject();
        inapp.put("campaignId", "someId");
        inapp.put("url", "https://hu.wikipedia.org/wiki/Mont_Blanc");
        ems.put("inapp", inapp);
        payload.put("ems", ems.toString());
        return payload;
    }

    private String expectedBasedOnApiLevel(String before23, String fromApi23) {
        if (Build.VERSION.SDK_INT < 23) {
            return before23;
        } else {
            return fromApi23;
        }
    }

    private String getApplicationName() {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

}