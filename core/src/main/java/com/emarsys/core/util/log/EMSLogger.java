package com.emarsys.core.util.log;

import android.util.Log;

public class EMSLogger {

    private EMSLogger() {
    }

    public static void log(LogTopic topic, String message) {
        if (EMSLoggerSettings.isEnabled(topic)) {
            log(topic.getTag(), message);
        }
    }

    public static void log(LogTopic topic, String message, Object... args) {
        if (EMSLoggerSettings.isEnabled(topic)) {
            log(topic.getTag(), String.format(message, args));
        }
    }

    private static void log(String tag, String rawMessage) {
        Log.i(tag,  createMessage(rawMessage));
    }

    private static String createMessage(String rawMessage) {
        StackTraceElement stackTraceElement = getStackTraceElement();
        return String.format("[%s] - %s::%s:%s\n%s",
                Thread.currentThread().getName(),
                stackTraceElement.getClassName(),
                stackTraceElement.getMethodName(),
                stackTraceElement.getLineNumber(),
                rawMessage);
    }

    private static StackTraceElement getStackTraceElement() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int i = 0;
        while (!stackTrace[i].getClassName().equals(EMSLogger.class.getName())) {
            i++;
        }
        while (stackTrace[i].getClassName().equals(EMSLogger.class.getName())) {
            i++;
        }
        return stackTrace[i];
    }

}