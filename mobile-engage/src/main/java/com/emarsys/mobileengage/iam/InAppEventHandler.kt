package com.emarsys.mobileengage.iam

import com.emarsys.mobileengage.api.event.EventHandler

interface InAppEventHandler {
    fun pause()
    fun resume()
    val isPaused: Boolean
    var eventHandler: EventHandler?
}