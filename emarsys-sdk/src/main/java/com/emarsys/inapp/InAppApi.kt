package com.emarsys.inapp

import com.emarsys.mobileengage.api.event.EventHandler

interface InAppApi {
    fun pause()
    fun resume()
    val isPaused: Boolean
    fun setEventHandler(eventHandler: EventHandler)
}