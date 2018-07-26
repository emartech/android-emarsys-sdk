package com.emarsys.mobileengage.inbox;

import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.inbox.model.NotificationInboxStatus;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InboxParseUtilsTest {

    public static final String NOTIFICATION_STRING_1 = "{" +
            "\"id\":\"id1\", " +
            "\"sid\":\"sid1\", " +
            "\"title\":\"title1\", " +
            "\"custom_data\": {" +
            "\"data1\":\"dataValue1\"," +
            "\"data2\":\"dataValue2\"" +
            "}," +
            "\"root_params\": {" +
            "\"param1\":\"paramValue1\"," +
            "\"param2\":\"paramValue2\"" +
            "}," +
            "\"expiration_time\": 300, " +
            "\"received_at\":10000000" +
            "}";

    public static final String NOTIFICATION_STRING_2 = "{" +
            "\"id\":\"id2\", " +
            "\"sid\":\"sid2\", " +
            "\"title\":\"title2\", " +
            "\"custom_data\": {" +
            "\"data3\":\"dataValue3\"," +
            "\"data4\":\"dataValue4\"" +
            "}," +
            "\"root_params\": {" +
            "\"param3\":\"paramValue3\"," +
            "\"param4\":\"paramValue4\"" +
            "}," +
            "\"expiration_time\": 200, " +
            "\"received_at\":30000000" +
            "}";
    public static final String NOTIFICATION_STRING_3 = "{" +
            "\"id\":\"id3\", " +
            "\"sid\":\"sid3\", " +
            "\"title\":\"title3\", " +
            "\"custom_data\": {" +
            "\"data5\":\"dataValue5\"," +
            "\"data6\":\"dataValue6\"" +
            "}," +
            "\"root_params\": {" +
            "\"param5\":\"paramValue5\"," +
            "\"param6\":\"paramValue6\"" +
            "}," +
            "\"expiration_time\": 100, " +
            "\"received_at\":25000000" +
            "}";
    public static final String NOTIFICATION_STRING_4 = "{" +
            "\"id\":\"id4\", " +
            "\"sid\":\"sid4\", " +
            "\"title\":\"title4\", " +
            "\"body\":\"body4\", " +
            "\"custom_data\": {" +
            "\"data5\":\"dataValue5\"," +
            "\"data6\":\"dataValue6\"" +
            "}," +
            "\"root_params\": {" +
            "\"param5\":\"paramValue5\"," +
            "\"param6\":\"paramValue6\"" +
            "}," +
            "\"expiration_time\": 100, " +
            "\"received_at\":25000000" +
            "}";
    private Notification notification1;

    private Notification notification2;
    private Notification notification3;
    private Notification notification4;
    private List<Notification> notifications;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() throws JSONException {
        Map<String, String> customData1 = new HashMap<>();
        customData1.put("data1", "dataValue1");
        customData1.put("data2", "dataValue2");

        JSONObject rootParams1 = new JSONObject();
        rootParams1.put("param1", "paramValue1");
        rootParams1.put("param2", "paramValue2");

        Map<String, String> customData2 = new HashMap<>();
        customData2.put("data3", "dataValue3");
        customData2.put("data4", "dataValue4");

        JSONObject rootParams2 = new JSONObject();
        rootParams2.put("param3", "paramValue3");
        rootParams2.put("param4", "paramValue4");


        Map<String, String> customData3 = new HashMap<>();
        customData3.put("data5", "dataValue5");
        customData3.put("data6", "dataValue6");

        JSONObject rootParams3 = new JSONObject();
        rootParams3.put("param5", "paramValue5");
        rootParams3.put("param6", "paramValue6");

        notification1 = new Notification("id1", "sid1", "title1", null, customData1, rootParams1, 300, 10000000);
        notification2 = new Notification("id2", "sid2", "title2", null, customData2, rootParams2, 200, 30000000);
        notification3 = new Notification("id3", "sid3", "title3", null, customData3, rootParams3, 100, 25000000);
        notification4 = new Notification("id4", "sid4", "title4", "body4", customData3, rootParams3, 100, 25000000);

        notifications = Arrays.asList(notification1, notification2, notification3);
    }

    @Test
    public void parseNotificationInboxStatus_withNull_returnsDefaultInboxStatus() {
        NotificationInboxStatus expected = new NotificationInboxStatus(new ArrayList<Notification>(), 0);
        NotificationInboxStatus result = InboxParseUtils.parseNotificationInboxStatus(null);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void parseNotificationInboxStatus_withEmptyString_returnsDefaultInboxStatus() {
        NotificationInboxStatus expected = new NotificationInboxStatus(new ArrayList<Notification>(), 0);
        NotificationInboxStatus result = InboxParseUtils.parseNotificationInboxStatus("");
        Assert.assertEquals(expected, result);
    }

    @Test
    public void parseNotificationInboxStatus_withCorrectString() {
        NotificationInboxStatus expected = new NotificationInboxStatus(notifications, 300);

        String json = "{\"notifications\": " +
                "[" + NOTIFICATION_STRING_1 + "," + NOTIFICATION_STRING_2 + "," + NOTIFICATION_STRING_3 + "]," +
                " \"badge_count\": 300}";

        NotificationInboxStatus result = InboxParseUtils.parseNotificationInboxStatus(json);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void parseNotificationInboxStatus_withMissingNotifications() {
        NotificationInboxStatus expected = new NotificationInboxStatus(null, 300);

        String json = "{\"badge_count\": 300}";

        NotificationInboxStatus result = InboxParseUtils.parseNotificationInboxStatus(json);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void parseNotificationInboxStatus_withMissingBadge() {
        NotificationInboxStatus expected = new NotificationInboxStatus(notifications, 0);

        String json = "{\"notifications\": " +
                "[" + NOTIFICATION_STRING_1 + "," + NOTIFICATION_STRING_2 + "," + NOTIFICATION_STRING_3 + "]}";

        NotificationInboxStatus result = InboxParseUtils.parseNotificationInboxStatus(json);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void parseBadgeCount_withNull_returnsZero() {
        Assert.assertEquals(0, InboxParseUtils.parseBadgeCount(null));
    }

    @Test
    public void parseBadgeCount_withEmptyString_returnsZero() {
        Assert.assertEquals(0, InboxParseUtils.parseBadgeCount(""));
    }

    @Test
    public void parseBadgeCount_withCorrectString_returnsCorrectValue() {
        String json = "{\"badge_count\": 301, \"badgeCountTest\": 404, \"badgeCountWrong\": 500}";

        Assert.assertEquals(301, InboxParseUtils.parseBadgeCount(json));
    }

    @Test
    public void parseNotificationList_withNull_returnsEmptyList() {
        List<Notification> expected = new ArrayList<>();
        List<Notification> result = InboxParseUtils.parseNotificationList(null);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void parseNotificationList_withEmptyString_returnsEmptyList() {
        List<Notification> expected = new ArrayList<>();
        List<Notification> result = InboxParseUtils.parseNotificationList("");
        Assert.assertEquals(expected, result);
    }

    @Test
    public void parseNotificationList_withIncorrectString_returnsEmptyList() {
        List<Notification> expected = new ArrayList<>();
        List<Notification> result = InboxParseUtils.parseNotificationList("{\"key\":\"value\"}");
        Assert.assertEquals(expected, result);
    }

    @Test
    public void parseNotificationList_withCorrectString_returnsList() throws JSONException {
        List<Notification> expected = notifications;

        String json = "[" + NOTIFICATION_STRING_1 + "," + NOTIFICATION_STRING_2 + "," + NOTIFICATION_STRING_3 + "]";

        List<Notification> result = InboxParseUtils.parseNotificationList(json);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void parseNotificationList_shouldBeResilient_toPartiallyIncorrectInput() throws JSONException {
        List<Notification> expected = new ArrayList<>();

        Map<String, String> customData = new HashMap<>();
        customData.put("data1", "dataValue1");
        customData.put("data2", "dataValue2");

        JSONObject rootParams = new JSONObject();
        rootParams.put("param1", "paramValue1");
        rootParams.put("param2", "paramValue2");

        expected.add(notification1);

        String incorrect1 = "{" +
                "\"key\":\"value\"" +
                "}";

        String incorrect2 = "{" +
                "\"name\":\"Joel\"," +
                "\"age\":\"34\"" +
                "}";

        String json = "[" + incorrect1 + "," + NOTIFICATION_STRING_1 + "," + incorrect2 + "]";

        List<Notification> result = InboxParseUtils.parseNotificationList(json);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void parseNotification_withNull_returnsNull() {
        Assert.assertNull(InboxParseUtils.parseNotification(null));
    }

    @Test
    public void parseNotification_withEmptyString_returnsNull() {
        Assert.assertNull(InboxParseUtils.parseNotification(""));
    }

    @Test
    public void parseNotification_withIncorrectString_returnsNull() {
        Assert.assertNull(InboxParseUtils.parseNotification("{\"key\":\"value\"}"));
    }

    @Test
    public void parseNotification_withCorrectString_returnsNotification() throws JSONException {
        Map<String, String> customData = new HashMap<>();
        customData.put("data1", "dataValue1");
        customData.put("data2", "dataValue2");

        JSONObject rootParams = new JSONObject();
        rootParams.put("param1", "paramValue1");
        rootParams.put("param2", "paramValue2");

        Notification expected = notification1;


        Notification result = InboxParseUtils.parseNotification(NOTIFICATION_STRING_1);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void parseNotification_withCorrectStringWithBody_returnsNotification() throws JSONException {
        Notification expected = notification4;

        Notification result = InboxParseUtils.parseNotification(NOTIFICATION_STRING_4);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void convertFlatJsonObject_withNull() {
        HashMap<Object, Object> expected = new HashMap<>();
        Map<String, String> result = InboxParseUtils.convertFlatJsonObject(null);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void convertFlatJsonObject_withEmptyJson() {
        HashMap<String, String> expected = new HashMap<>();
        Map<String, String> result = InboxParseUtils.convertFlatJsonObject(new JSONObject());
        Assert.assertEquals(expected, result);
    }

    @Test
    public void convertFlatJsonObject_withJson() throws JSONException {
        JSONObject json = new JSONObject().put("key1", "value1").put("key2", "value2").put("key3", "value3");

        HashMap<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");

        Map<String, String> result = InboxParseUtils.convertFlatJsonObject(json);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testParseNotificationFromPushMessage(){
        Map<String, String> remoteData = new HashMap<>();
        remoteData.put("inbox", "true");
        remoteData.put("ems_msg", "true");
        remoteData.put("u", "{\"test_field\":\"\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}");
        remoteData.put("id", "21022.150123121212.43223434c3b9");
        remoteData.put("title", "hello there");
        remoteData.put("rootParam1", "param_param");

        Map<String, String> customData = new HashMap<>();
        customData.put("test_field", "");
        customData.put("image", "https://media.giphy.com/media/ktvFa67wmjDEI/giphy.gif");
        customData.put("deep_link", "lifestylelabels.com/mobile/product/3245678");
        customData.put("sid", "sid_here");

        long before = System.currentTimeMillis();
        Notification result = InboxParseUtils.parseNotificationFromPushMessage(remoteData, false);
        long after = System.currentTimeMillis();

        Assert.assertEquals("21022.150123121212.43223434c3b9", result.getId());
        Assert.assertEquals("sid_here", result.getSid());
        Assert.assertEquals("hello there", result.getTitle());
        Assert.assertEquals(customData, result.getCustomData());
        Assert.assertTrue(before <= result.getReceivedAt());
        Assert.assertTrue(result.getReceivedAt() <= after);
    }

    @Test
    public void testParseNotificationFromPushMessageWithBody() {
        Map<String, String> remoteData = new HashMap<>();
        remoteData.put("inbox", "true");
        remoteData.put("ems_msg", "true");
        remoteData.put("u", "{\"test_field\":\"\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}");
        remoteData.put("id", "21022.150123121212.43223434c3b9");
        remoteData.put("title", "hello there");
        remoteData.put("body", "o<-<");
        remoteData.put("rootParam1", "param_param");

        Map<String, String> customData = new HashMap<>();
        customData.put("test_field", "");
        customData.put("image", "https://media.giphy.com/media/ktvFa67wmjDEI/giphy.gif");
        customData.put("deep_link", "lifestylelabels.com/mobile/product/3245678");
        customData.put("sid", "sid_here");

        long before = System.currentTimeMillis();
        Notification result = InboxParseUtils.parseNotificationFromPushMessage(remoteData, false);
        long after = System.currentTimeMillis();

        Assert.assertEquals("21022.150123121212.43223434c3b9", result.getId());
        Assert.assertEquals("sid_here", result.getSid());
        Assert.assertEquals("hello there", result.getTitle());
        Assert.assertEquals("o<-<", result.getBody());
        Assert.assertEquals(customData, result.getCustomData());
        Assert.assertTrue(before <= result.getReceivedAt());
        Assert.assertTrue(result.getReceivedAt() <= after);
    }

    @Test
    public void testParseNotificationFromPushMessage_withUserCentricInbox() {
        String messageId = "anotherId";
        Map<String, String> remoteData = new HashMap<>();
        remoteData.put("inbox", "true");
        remoteData.put("ems_msg", "true");
        remoteData.put("u", "{\"test_field\":\"\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}");
        remoteData.put("id", "21022.150123121212.43223434c3b9");
        remoteData.put("message_id", messageId);
        remoteData.put("title", "hello there");
        remoteData.put("body", "o<-<");
        remoteData.put("rootParam1", "param_param");

        Notification result = InboxParseUtils.parseNotificationFromPushMessage(remoteData, true);

        Assert.assertEquals(messageId, result.getId());
    }

    @Test
    public void testParseNotificationFromPushMessage_shouldNotParse_ifNotInboxMessage(){
        Map<String, String> remoteData = new HashMap<>();
        remoteData.put("inbox", "false");
        remoteData.put("ems_msg", "true");
        remoteData.put("u", "{\"test_field\":\"\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}");
        remoteData.put("id", "21022.150123121212.43223434c3b9");
        remoteData.put("title", "hello there");
        remoteData.put("rootParam1", "param_param");

        Assert.assertNull(InboxParseUtils.parseNotificationFromPushMessage(remoteData, false));
    }

    @Test
    public void testParseNotificationFromPushMessage_shouldNotParse_withMissingInboxField(){
        Map<String, String> remoteData = new HashMap<>();
        remoteData.put("ems_msg", "true");
        remoteData.put("u", "{\"test_field\":\"\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}");
        remoteData.put("id", "21022.150123121212.43223434c3b9");
        remoteData.put("title", "hello there");
        remoteData.put("rootParam1", "param_param");

        Assert.assertNull(InboxParseUtils.parseNotificationFromPushMessage(remoteData, false));
    }
}