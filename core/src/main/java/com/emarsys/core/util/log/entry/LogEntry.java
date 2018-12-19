package com.emarsys.core.util.log.entry;

import java.util.Map;

public interface LogEntry {
    String getTopic();

    Map<String, Object> getData();
}