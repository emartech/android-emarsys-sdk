package com.emarsys.mobileengage.notification;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.emarsys.core.util.Assert;
import com.emarsys.core.util.CollectionUtils;
import com.emarsys.core.util.JsonUtils;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.api.NotificationEventHandler;
import com.emarsys.mobileengage.notification.command.AppEventCommand;
import com.emarsys.mobileengage.notification.command.CompositeCommand;
import com.emarsys.mobileengage.notification.command.CustomEventCommand;
import com.emarsys.mobileengage.notification.command.DismissNotificationCommand;
import com.emarsys.mobileengage.notification.command.HideNotificationShadeCommand;
import com.emarsys.mobileengage.notification.command.LaunchApplicationCommand;
import com.emarsys.mobileengage.notification.command.OpenExternalUrlCommand;
import com.emarsys.mobileengage.notification.command.TrackActionClickCommand;
import com.emarsys.mobileengage.notification.command.TrackMessageOpenCommand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NotificationCommandFactory {

    private Context context;
    private MobileEngageInternal mobileEngageInternal;
    private NotificationEventHandler notificationEventHandler;

    public NotificationCommandFactory(
            Context context,
            MobileEngageInternal mobileEngageInternal,
            NotificationEventHandler notificationEventHandler) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(mobileEngageInternal, "MobileEngageInternal must not be null!");
        this.context = context;
        this.mobileEngageInternal = mobileEngageInternal;
        this.notificationEventHandler = notificationEventHandler;
    }

    public Runnable createNotificationCommand(Intent intent) {
        Runnable result = null;
        String actionId = intent.getAction();
        Bundle bundle = intent.getBundleExtra("payload");

        if (bundle != null) {
            String emsPayload = bundle.getString("ems");
            if (emsPayload != null) {
                Runnable hideNotificationShadeCommand = new HideNotificationShadeCommand(context);
                Runnable trackMessageOpenCommand = new TrackMessageOpenCommand(mobileEngageInternal, intent);
                Runnable dismissNotificationCommand = new DismissNotificationCommand(context, intent);
                if (actionId != null) {
                    try {
                        JSONArray actions = new JSONObject(emsPayload).getJSONArray("actions");
                        JSONObject action = findActionWithId(actions, actionId);

                        String sid = extractSid(bundle);
                        Runnable trackActionClickCommand = new TrackActionClickCommand(mobileEngageInternal, actionId, sid);

                        result = createCompositeCommand(action, Arrays.asList(dismissNotificationCommand, trackMessageOpenCommand, trackActionClickCommand, hideNotificationShadeCommand));

                    } catch (JSONException ignored) {
                    }
                } else {
                    try {
                        JSONObject action = new JSONObject(emsPayload).getJSONObject("default_action");

                        result = createCompositeCommand(action, Arrays.asList(dismissNotificationCommand, trackMessageOpenCommand, hideNotificationShadeCommand));
                    } catch (JSONException ignored) {
                    }
                }
            }
        }
        if (result == null) {
            result = new LaunchApplicationCommand(intent, context);
        }

        return result;
    }

    private Runnable createCompositeCommand(JSONObject action, List<Runnable> mandatoryActions) throws JSONException {
        Runnable result = null;
        String type = action.getString("type");

        if ("MEAppEvent".equals(type)) {
            result = new CompositeCommand(CollectionUtils.mergeLists(mandatoryActions, Collections.singletonList(createAppEventCommand(action))));
        }
        if ("OpenExternalUrl".equals(type)) {
            Runnable openExternalUrl = createOpenExternalUrlCommand(action);
            if (openExternalUrl != null) {
                result = new CompositeCommand(CollectionUtils.mergeLists(mandatoryActions, Collections.singletonList(openExternalUrl)));
            }
        }
        if ("MECustomEvent".equals(type)) {
            result = new CompositeCommand(CollectionUtils.mergeLists(mandatoryActions, Collections.singletonList(createCustomEventCommand(action))));
        }
        return result;
    }

    private String extractSid(Bundle bundle) {
        String sid = null;
        if (bundle.containsKey("u")) {
            try {
                sid = new JSONObject(bundle.getString("u")).getString("sid");
            } catch (JSONException ignore) {

            }
        }

        if (sid == null) {
            sid = "Missing sid";
        }
        return sid;
    }

    private JSONObject findActionWithId(JSONArray actions, String actionId) throws JSONException {
        for (int i = 0; i < actions.length(); ++i) {
            JSONObject action = actions.optJSONObject(i);
            if (action != null && actionId.equals(action.optString("id"))) {
                return action;
            }
        }
        throw new JSONException("Cannot find action with id: " + actionId);
    }

    private Runnable createAppEventCommand(JSONObject action) throws JSONException {
        return new AppEventCommand(
                context,
                notificationEventHandler,
                action.getString("name"),
                action.optJSONObject("payload"));
    }

    private Runnable createOpenExternalUrlCommand(JSONObject action) throws JSONException {
        Runnable result = null;

        Uri link = Uri.parse(action.getString("url"));
        Intent externalCommandIntent = new Intent(Intent.ACTION_VIEW, link);
        externalCommandIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (externalCommandIntent.resolveActivity(context.getPackageManager()) != null) {
            result = new OpenExternalUrlCommand(externalCommandIntent, context);
        }
        return result;
    }

    private Runnable createCustomEventCommand(JSONObject action) throws JSONException {
        String name = action.getString("name");
        JSONObject payload = action.optJSONObject("payload");
        Map<String, String> eventAttribute = null;
        if (payload != null) {
            eventAttribute = JsonUtils.toFlatMap(payload);
        }

        return new CustomEventCommand(mobileEngageInternal, name, eventAttribute);
    }

}
