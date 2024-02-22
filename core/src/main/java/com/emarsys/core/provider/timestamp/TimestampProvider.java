package com.emarsys.core.provider.timestamp;

import com.emarsys.core.Mockable;

@Mockable
public class TimestampProvider {
    public long provideTimestamp() {
        return System.currentTimeMillis();
    }
}
