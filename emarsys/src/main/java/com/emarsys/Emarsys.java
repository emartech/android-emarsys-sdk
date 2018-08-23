package com.emarsys;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.emarsys.core.util.Assert;

import java.util.Map;

class Emarsys {

    public static void trackCustomEvent(@NonNull String eventName, @Nullable Map<String, String> eventAttributes) {
        Assert.notNull(eventName, "EventName must not be null!");
    }
}