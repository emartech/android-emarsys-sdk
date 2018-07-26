package com.emarsys.mobileengage.service;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.v4.app.NotificationCompat;

import com.emarsys.mobileengage.notification.NotificationCommandFactory;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Build.VERSION_CODES.KITKAT;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NotificationActionUtilsTest {

    private Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        this.context = InstrumentationRegistry.getTargetContext().getApplicationContext();
    }

    @Test
    public void testHandleAction_runsNotificationCommand() {
        NotificationCommandFactory factory = mock(NotificationCommandFactory.class);
        Intent intent = mock(Intent.class);
        Runnable command = mock(Runnable.class);

        when(factory.createNotificationCommand(intent)).thenReturn(command);

        NotificationActionUtils.handleAction(intent, factory);
        verify(command).run();
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_missingId() throws JSONException {
        JSONObject ems = new JSONObject().put("actions",
                new JSONArray().put(new JSONObject()
                        .put("title", "title")
                        .put("type", "MEAppEvent")
                ));

        Map<String, String> input = new HashMap<>();
        input.put("ems", ems.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_missingTitle() throws JSONException {
        JSONObject ems = new JSONObject().put("actions",
                new JSONArray().put(new JSONObject()
                        .put("id", "uniqueActionId")
                        .put("type", "MEAppEvent")
                ));

        Map<String, String> input = new HashMap<>();
        input.put("ems", ems.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_missingType() throws JSONException {
        JSONObject ems = new JSONObject().put("actions",
                new JSONArray().put(new JSONObject()
                        .put("id", "uniqueActionId")
                        .put("title", "Action button title")
                ));

        Map<String, String> input = new HashMap<>();
        input.put("ems", ems.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_appEvent_missingEventName() throws JSONException {
        JSONObject ems = new JSONObject().put("actions",
                new JSONArray().put(new JSONObject()
                        .put("id", "uniqueActionId")
                        .put("title", "Action button title")
                        .put("type", "MEAppEvent")
                ));

        Map<String, String> input = new HashMap<>();
        input.put("ems", ems.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_appEvent_withSingleAction() throws JSONException {
        JSONObject payload = new JSONObject()
                .put("actions", new JSONArray()
                        .put(new JSONObject()
                                .put("id", "uniqueActionId")
                                .put("title", "Action button title")
                                .put("type", "MEAppEvent")
                                .put("name", "Name of the event")
                                .put("payload", new JSONObject()
                                        .put("payloadKey", "payloadValue")))
                );

        Map<String, String> input = new HashMap<>();
        input.put("ems", payload.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input, 0);
        assertEquals(1, result.size());
        assertEquals("Action button title", result.get(0).title);
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_appEvent_withMultipleActions() throws JSONException {
        JSONObject payload = new JSONObject()
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
        input.put("ems", payload.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input, 0);
        assertEquals(2, result.size());

        assertEquals("title1", result.get(0).title);

        assertEquals("title2", result.get(1).title);
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_externalUrl_missingUrl() throws JSONException {
        JSONObject ems = new JSONObject().put("actions",
                new JSONArray().put(new JSONObject()
                        .put("id", "uniqueActionId")
                        .put("title", "Action button title")
                        .put("type", "OpenExternalUrl")
                ));

        Map<String, String> input = new HashMap<>();
        input.put("ems", ems.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_externalUrl_withSingleAction() throws JSONException {
        JSONObject payload = new JSONObject()
                .put("actions", new JSONArray()
                        .put(new JSONObject()
                                .put("id", "uniqueActionId")
                                .put("title", "Action button title")
                                .put("type", "OpenExternalUrl")
                                .put("url", "https://www.emarsys.com")
                        )
                );

        Map<String, String> input = new HashMap<>();
        input.put("ems", payload.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input, 0);
        assertEquals(1, result.size());
        assertEquals("Action button title", result.get(0).title);
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_externalUrl_withMultipleActions() throws JSONException {
        JSONObject payload = new JSONObject()
                .put("actions", new JSONArray()
                        .put(new JSONObject()
                                .put("id", "uniqueActionId")
                                .put("title", "Action button title")
                                .put("type", "OpenExternalUrl")
                                .put("url", "https://www.emarsys.com")
                        )
                        .put(new JSONObject()
                                .put("id", "uniqueActionId2")
                                .put("title", "Second button title")
                                .put("type", "OpenExternalUrl")
                                .put("url", "https://www.emarsys/faq.com")
                        )
                );

        Map<String, String> input = new HashMap<>();
        input.put("ems", payload.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input, 0);
        assertEquals(2, result.size());
        assertEquals("Action button title", result.get(0).title);
        assertEquals("Second button title", result.get(1).title);
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_customEvent_missingName() throws JSONException {
        JSONObject ems = new JSONObject().put("actions",
                new JSONArray().put(new JSONObject()
                        .put("id", "uniqueActionId")
                        .put("title", "Action button title")
                        .put("type", "MECustomEvent")
                ));

        Map<String, String> input = new HashMap<>();
        input.put("ems", ems.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_customEvent_withSingleAction() throws JSONException {
        JSONObject ems = new JSONObject().put("actions",
                new JSONArray().put(new JSONObject()
                        .put("id", "uniqueActionId")
                        .put("title", "Action button title")
                        .put("type", "MECustomEvent")
                        .put("name", "eventName")
                        .put("payload", new JSONObject()
                                .put("key1", "value1")
                                .put("key2", "value2"))
                ));

        Map<String, String> input = new HashMap<>();
        input.put("ems", ems.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input, 0);
        assertEquals(1, result.size());
        assertEquals("Action button title", result.get(0).title);
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_customEvent_withSingleAction_withoutPayload() throws JSONException {
        JSONObject ems = new JSONObject().put("actions",
                new JSONArray().put(new JSONObject()
                        .put("id", "uniqueActionId")
                        .put("title", "Action button title")
                        .put("type", "MECustomEvent")
                        .put("name", "eventName")
                ));

        Map<String, String> input = new HashMap<>();
        input.put("ems", ems.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input, 0);
        assertEquals(1, result.size());
        assertEquals("Action button title", result.get(0).title);
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_customEvent_withMultipleActions() throws JSONException {
        JSONObject ems = new JSONObject().put("actions",
                new JSONArray().put(new JSONObject()
                        .put("id", "uniqueActionId")
                        .put("title", "Action button title")
                        .put("type", "MECustomEvent")
                        .put("name", "eventName")
                        .put("payload", new JSONObject()
                                .put("key1", "value1")
                                .put("key2", "value2")))
                        .put(new JSONObject()
                                .put("id", "uniqueActionId2")
                                .put("title", "Another button title")
                                .put("type", "MECustomEvent")
                                .put("name", "eventName")
                        ));

        Map<String, String> input = new HashMap<>();
        input.put("ems", ems.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input, 0);
        assertEquals(2, result.size());
        assertEquals("Action button title", result.get(0).title);
        assertEquals("Another button title", result.get(1).title);
    }
}