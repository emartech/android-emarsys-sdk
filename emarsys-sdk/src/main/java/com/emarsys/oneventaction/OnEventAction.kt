package com.emarsys.oneventaction


import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.event.EventHandlerProvider

class OnEventAction : OnEventActionApi {

    override fun setOnEventActionEventHandler(eventHandler: EventHandler) {
        val onEventActionEventHandlerProvider: EventHandlerProvider = mobileEngage().onEventActionEventHandlerProvider
        onEventActionEventHandlerProvider.eventHandler = eventHandler
    }
}