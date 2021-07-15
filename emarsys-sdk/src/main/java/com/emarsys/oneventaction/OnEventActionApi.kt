package com.emarsys.oneventaction

import com.emarsys.mobileengage.api.event.EventHandler

interface OnEventActionApi {
    fun setOnEventActionEventHandler(eventHandler: EventHandler)
}