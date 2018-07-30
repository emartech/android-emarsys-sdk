package com.emarsys.core.provider.uuid;

import java.util.UUID;

public class UUIDProvider {

    public String provideId() {
        return UUID.randomUUID().toString();
    }
}