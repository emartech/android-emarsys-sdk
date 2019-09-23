package com.emarsys.mobileengage.push

interface PushTokenProvider {

    fun providePushToken(): String?
}