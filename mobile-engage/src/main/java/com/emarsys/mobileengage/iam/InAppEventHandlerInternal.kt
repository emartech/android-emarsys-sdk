package com.emarsys.mobileengage.iam

import com.emarsys.core.Mockable
import com.emarsys.mobileengage.api.event.EventHandler

@Mockable
class InAppEventHandlerInternal : InAppEventHandler {

    override var isPaused: Boolean = false
    override var eventHandler: EventHandler? = null

    override fun pause() {
        isPaused = true
    }

    override fun resume() {
        isPaused = false
    }
}
