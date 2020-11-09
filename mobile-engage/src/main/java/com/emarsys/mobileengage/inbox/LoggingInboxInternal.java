package com.emarsys.mobileengage.inbox;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.core.util.SystemUtils;
import com.emarsys.core.util.log.Logger;
import com.emarsys.core.util.log.entry.MethodNotAllowed;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;

import java.util.HashMap;
import java.util.Map;

public class LoggingInboxInternal implements InboxInternal {

    private final Class klass;

    public LoggingInboxInternal(Class klass) {
        this.klass = klass;
    }

    @Override
    public void fetchNotifications(ResultListener<Try<NotificationInboxStatus>> resultListener) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("result_listener", resultListener != null);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.debug(new MethodNotAllowed(klass, callerMethodName, parameters), false);
    }

    @Override
    public void resetBadgeCount(CompletionListener completionListener) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("completion_listener", completionListener != null);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.debug(new MethodNotAllowed(klass, callerMethodName, parameters), false);
    }

    @Override
    public void trackNotificationOpen(Notification notification, CompletionListener completionListener) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("notification", notification.toString());
        parameters.put("completion_listener", completionListener != null);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.debug(new MethodNotAllowed(klass, callerMethodName, parameters), false);
    }
}
