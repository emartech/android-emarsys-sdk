package com.emarsys.mobileengage.push

import com.google.firebase.iid.FirebaseInstanceId

class DefaultPushTokenProvider : PushTokenProvider {

    override fun providePushToken(): String? {
        return FirebaseInstanceId.getInstance().instanceId.result?.token
    }
}