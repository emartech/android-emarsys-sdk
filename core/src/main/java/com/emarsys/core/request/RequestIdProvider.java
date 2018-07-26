package com.emarsys.core.request;

import java.util.UUID;

public class RequestIdProvider {

    public String provideId() {
        return UUID.randomUUID().toString();
    }
}