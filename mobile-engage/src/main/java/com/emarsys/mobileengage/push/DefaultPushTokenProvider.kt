package com.emarsys.mobileengage.push

import com.emarsys.core.storage.Storage

class DefaultPushTokenProvider(private val pushTokenStorage: Storage<String?>) : PushTokenProvider {

    override fun providePushToken(): String? {
        return pushTokenStorage.get()
    }
}