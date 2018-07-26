package com.emarsys.core.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimestampUtils {

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);

    private TimestampUtils() {
    }

    public static String formatTimestampWithUTC(long timestamp) {
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(timestamp);
        return formatter.format(date);
    }

}