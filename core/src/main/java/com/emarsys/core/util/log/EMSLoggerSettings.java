package com.emarsys.core.util.log;

import java.util.HashSet;
import java.util.Set;

public class EMSLoggerSettings {

    private static boolean allEnabled = false;
    private static Set<String> topics = new HashSet<>();

    private EMSLoggerSettings() {}

    public static void enableLogging(LogTopic... topics) {
        for (LogTopic topic : topics) {
            EMSLoggerSettings.topics.add(topic.getTag());
        }
    }

    public static void enableLoggingForAllTopics() {
        allEnabled = true;
    }

    static boolean isEnabled(LogTopic topic) {
        return allEnabled || topics.contains(topic.getTag());
    }
}
