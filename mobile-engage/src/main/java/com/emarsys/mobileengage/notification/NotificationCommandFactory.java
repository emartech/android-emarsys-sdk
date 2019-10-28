package com.emarsys.mobileengage.notification;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.emarsys.core.util.Assert;
import com.emarsys.core.util.JsonUtils;
import com.emarsys.mobileengage.api.NotificationEventHandler;
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer;
import com.emarsys.mobileengage.event.EventServiceInternal;
import com.emarsys.mobileengage.notification.command.AppEventCommand;
import com.emarsys.mobileengage.notification.command.CompositeCommand;
import com.emarsys.mobileengage.notification.command.CustomEventCommand;
import com.emarsys.mobileengage.notification.command.DismissNotificationCommand;
import com.emarsys.mobileengage.notification.command.HideNotificationShadeCommand;
import com.emarsys.mobileengage.notification.command.LaunchApplicationCommand;
import com.emarsys.mobileengage.notification.command.OpenExternalUrlCommand;
import com.emarsys.mobileengage.notification.command.PreloadedInappHandlerCommand;
import com.emarsys.mobileengage.notification.command.TrackActionClickCommand;
import com.emarsys.mobileengage.notification.command.TrackMessageOpenCommand;
import com.emarsys.mobileengage.push.PushInternal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationCommandFactory {

    private final Context context;
    private final MobileEngageDependencyContainer dependencyContainer;
    private final EventServiceInternal eventServiceInternal;
    private final PushInternal pushInternal;
    private final NotificationEventHandler notificationEventHandler;

    public NotificationCommandFactory(
            Context context,
            MobileEngageDependencyContainer dependencyContainer) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(dependencyContainer, "DependencyContainer must not be null!");

        this.context = context;
        this.dependencyContainer = dependencyContainer;
        this.eventServiceInternal = dependencyContainer.getEventServiceInternal();
        this.notificationEventHandler = dependencyContainer.getNotificationEventHandler();
        this.pushInternal = dependencyContainer.getPushInternal();

        Assert.notNull(eventServiceInternal, "EventServiceInternal from dependency container must not be null!");
        Assert.notNull(pushInternal, "PushInternal from dependency container must not be null!");
    }

    public Runnable createNotificationCommand(Intent intent) {
        String actionId = intent.getAction();
        Bundle bundle = intent.getBundleExtra("payload");
        JSONObject action = getAction(bundle, actionId);
        List<Runnable> commands = createMandatoryCommands(intent);

        Runnable inappCommand = handleInapp(intent, bundle);
        if (inappCommand != null) {
            commands.add(inappCommand);
        }

        Runnable trackingCommand = handleTracking(intent, actionId, bundle, action);
        if (trackingCommand != null) {
            commands.add(trackingCommand);
        }

        Runnable actionCommand = handleAction(action);
        if (actionCommand != null) {
            commands.add(actionCommand);
        }

        if (action == null || !action.optString("type").equals("Dismiss")) {
            commands.add(new LaunchApplicationCommand(intent, context));
        }

        return new CompositeCommand(commands);
    }

    private List<Runnable> createMandatoryCommands(Intent intent) {
        List<Runnable> commands =  new ArrayList<>();
        commands.add(new HideNotificationShadeCommand(context));
        commands.add(new DismissNotificationCommand(context, intent));

        return commands;
    }

    private Runnable handleAction(JSONObject action) {
        Runnable result = null;
        if (action != null) {
            Runnable actionCommand = getCommand(action);
            if (actionCommand != null) {
                result = actionCommand;
            }
        }
        return result;
    }

    private Runnable handleTracking(Intent intent, String actionId, Bundle bundle, JSONObject action) {
        Runnable result;
        if (action != null && actionId != null) {
            result = new TrackActionClickCommand(eventServiceInternal, actionId, extractSid(bundle));
        } else {
            result = new TrackMessageOpenCommand(pushInternal, intent);
        }
        return result;
    }

    private Runnable handleInapp(Intent intent, Bundle bundle) {
        Runnable result = null;
        if (hasInapp(bundle)) {
            result = new PreloadedInappHandlerCommand(intent, dependencyContainer);
        }
        return result;
    }

    private boolean hasInapp(Bundle payload) {
        boolean result = false;
        try {
            if (payload != null) {
                String ems = payload.getString("ems");
                if (ems != null) {
                    JSONObject emsJson = new JSONObject(ems);
                    new JSONObject(emsJson.getString("inapp"));
                    result = true;
                }
            }
        } catch (JSONException ignored) {
        }
        return result;
    }

    private Runnable getCommand(JSONObject action) {
        Runnable result = null;
        if (action != null) {
            String type;
            try {
                type = action.getString("type");
                if ("MEAppEvent".equals(type)) {
                    result = createAppEventCommand(action);
                }
                if ("OpenExternalUrl".equals(type)) {
                    Runnable openExternalUrl = createOpenExternalUrlCommand(action);
                    if (openExternalUrl != null) {
                        result = openExternalUrl;
                    }
                }
                if ("MECustomEvent".equals(type)) {
                    result = createCustomEventCommand(action);
                }

            } catch (JSONException ignored) {
            }
        }
        return result;
    }

    private JSONObject getAction(Bundle bundle, String actionId) {
        JSONObject result = null;
        if (bundle != null) {
            String emsPayload = bundle.getString("ems");
            if (emsPayload != null) {
                try {
                    if (actionId != null) {
                        JSONArray actions = new JSONObject(emsPayload).getJSONArray("actions");
                        result = findActionWithId(actions, actionId);
                    } else {
                        result = new JSONObject(emsPayload).getJSONObject("default_action");
                    }
                } catch (JSONException ignored) {
                }
            }
        }
        return result;
    }

    private String extractSid(Bundle bundle) {
        String sid = null;
        if (bundle != null && bundle.containsKey("u")) {
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

        return new CustomEventCommand(eventServiceInternal, name, eventAttribute);
    }

}
