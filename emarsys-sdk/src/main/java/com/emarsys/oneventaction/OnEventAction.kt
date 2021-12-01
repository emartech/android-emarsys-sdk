package com.emarsys.oneventaction


import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.event.CacheableEventHandler

class OnEventAction : OnEventActionApi {

    override fun setOnEventActionEventHandler(eventHandler: EventHandler) {
        val onEventActionCacheableEventHandler: CacheableEventHandler = mobileEngage().onEventActionCacheableEventHandler
        onEventActionCacheableEventHandler.setEventHandler(eventHandler)
    }
}