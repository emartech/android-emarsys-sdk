package com.emarsys.mobileengage.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.emarsys.core.validate.JsonObjectValidator;
import com.emarsys.mobileengage.notification.NotificationCommandFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationActionUtils {

    public static void handleAction(Intent intent, NotificationCommandFactory commandFactory) {
        Runnable command = commandFactory.createNotificationCommand(intent);
        command.run();
    }

    public static List<NotificationCompat.Action> createActions(
            Context context,
            Map<String, String> remoteMessageData,
            int notificationId) {
        List<NotificationCompat.Action> result = new ArrayList<>();
        String emsPayload = remoteMessageData.get("ems");
        if (emsPayload != null) {
            try {
                JSONArray actions = new JSONObject(emsPayload).getJSONArray("actions");
                for (int i = 0; i < actions.length(); ++i) {
                    NotificationCompat.Action action = createAction(
                            actions.getJSONObject(i),
                            context,
                            remoteMessageData,
                            notificationId);
                    if (action != null) {
                        result.add(action);
                    }
                }
            } catch (JSONException ignored) {
            }
        }
        return result;
    }

    private static NotificationCompat.Action createAction(
            JSONObject action,
            Context context,
            Map<String, String> remoteMessageData,
            int notificationId) {
        NotificationCompat.Action result = null;

        try {
            String actionId = action.getString("id");

            List<String> validationErrors = validate(action);

            if (validationErrors.isEmpty()) {
                result = new NotificationCompat.Action.Builder(
                        0,
                        action.getString("title"),
                        IntentUtils.createTrackMessageOpenServicePendingIntent(context, remoteMessageData, notificationId, actionId)).build();
            }
        } catch (JSONException ignored) {
        }

        return result;
    }

    private static List<String> validate(JSONObject action) throws JSONException {
        String actionType = action.getString("type");
        JsonObjectValidator jsonObjectValidator = JsonObjectValidator.from(action);
        if ("MEAppEvent".equals(actionType)) {
            jsonObjectValidator.hasField("name");
        }
        if ("OpenExternalUrl".equals(actionType)) {
            jsonObjectValidator.hasField("url");
        }
        if ("MECustomEvent".equals(actionType)) {
            jsonObjectValidator.hasField("name");
        }
        return jsonObjectValidator.validate();
    }

}
