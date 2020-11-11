package com.emarsys.di

import com.emarsys.config.ConfigApi
import com.emarsys.config.ConfigInternal
import com.emarsys.geofence.GeofenceApi
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.InboxApi
import com.emarsys.inbox.MessageInboxApi
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer
import com.emarsys.oneventaction.OnEventActionApi
import com.emarsys.predict.PredictApi
import com.emarsys.predict.di.PredictDependencyContainer
import com.emarsys.push.PushApi

interface EmarsysDependencyContainer : MobileEngageDependencyContainer, PredictDependencyContainer {
    fun getInbox(): InboxApi

    fun getLoggingInbox(): InboxApi

    fun getMessageInbox(): MessageInboxApi

    fun getLoggingMessageInbox(): MessageInboxApi

    fun getInApp(): InAppApi

    fun getLoggingInApp(): InAppApi

    fun getOnEventAction(): OnEventActionApi

    fun getLoggingOnEventAction(): OnEventActionApi

    fun getPush(): PushApi

    fun getLoggingPush(): PushApi

    fun getPredict(): PredictApi

    fun getLoggingPredict(): PredictApi

    fun getConfig(): ConfigApi

    fun getGeofence(): GeofenceApi

    fun getLoggingGeofence(): GeofenceApi

    fun getConfigInternal(): ConfigInternal
}