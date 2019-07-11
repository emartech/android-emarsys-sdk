package com.emarsys.core.util.log.entry;

import android.util.Log;

import com.emarsys.core.util.Assert;

import java.util.HashMap;
import java.util.Map;

public class MethodNotAllowed implements LogEntry {

    private static final String TAG = "Emarsys SDK";
    private final Map<String, Object> data;

    public MethodNotAllowed(Class klass, String callerMethodName, Map<String, Object> parameters) {
        Assert.notNull(klass, "ClassName must not be null!");
        Assert.notNull(callerMethodName, "CallerMethodName must not be null!");

        data = new HashMap<>();
        data.put("class_name", klass.getSimpleName());
        data.put("method_name", callerMethodName);
        if (parameters != null) {
            data.put("parameters", parameters);
        }

        Log.i(TAG, String.format("Feature disabled, Class: %s method: %s not allowed. Please check your config.", klass.getSimpleName(), callerMethodName));
    }

    @Override
    public String getTopic() {
        return "log_method_not_allowed";
    }

    @Override
    public Map<String, Object> getData() {
        return data;
    }
}
