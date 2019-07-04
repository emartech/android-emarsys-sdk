package com.emarsys.mobileengage;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.util.SystemUtils;
import com.emarsys.core.util.log.Logger;
import com.emarsys.core.util.log.entry.MethodNotAllowed;

import java.util.HashMap;
import java.util.Map;

public class LoggingMobileEngageInternal implements MobileEngageInternal {

    private static final Class<LoggingMobileEngageInternal> KLASS = LoggingMobileEngageInternal.class;

    @Override
    public void setContact(String contactFieldValue, CompletionListener completionListener) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("contact_field_value", contactFieldValue);
        parameters.put("completion_listener", completionListener != null);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(KLASS, callerMethodName, parameters));
    }

    @Override
    public void clearContact(CompletionListener completionListener) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("completion_listener", completionListener);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(KLASS, callerMethodName, parameters));
    }
}
