package com.emarsys.core.util;

import android.util.Base64;

public class HeaderUtils {

    private HeaderUtils() {
    }

    public static String createBasicAuth(String username, String password) {
        Assert.notNull(username, "Username must not be null!");
        Assert.notNull(password, "Password must not be null!");
        String credentials = username + ":" + password;
        return "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
    }
}
