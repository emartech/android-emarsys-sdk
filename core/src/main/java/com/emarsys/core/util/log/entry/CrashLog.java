package com.emarsys.core.util.log.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrashLog implements LogEntry {

    private final Map<String, Object> data;

    public CrashLog(Throwable throwable) {
        data = new HashMap<>();
        if (throwable != null) {
            data.put("exception", throwable.getClass().getName());
            data.put("reason", throwable.getMessage());
            data.put("stack_trace", getStackTrace(throwable));
        }
    }

    @Override
    public String getTopic() {
        return "log_crash";
    }

    @Override
    public Map<String, Object> getData() {
        return data;
    }

    private List<String> getStackTrace(Throwable throwable) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        int size = stackTrace.length;
        List<String> result = new ArrayList<>(size);

        for (StackTraceElement element : stackTrace) {
            result.add(element.toString());
        }

        return result;
    }

}
