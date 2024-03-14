package com.emarsys.core.provider.uuid;

import com.emarsys.core.Mockable;

import java.util.UUID;

@Mockable
public class UUIDProvider {

    public String provideId() {
        return UUID.randomUUID().toString();
    }
}