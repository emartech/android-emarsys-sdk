package com.emarsys.mobileengage.service

interface RemoteMessageMapper {
    fun map(remoteMessageData: Map<String, String?>): NotificationData
}