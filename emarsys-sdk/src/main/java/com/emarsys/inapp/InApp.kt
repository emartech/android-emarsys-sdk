package com.emarsys.inapp

import com.emarsys.core.Mockable
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.di.mobileEngage

@Mockable
class InApp(private val loggingInstance: Boolean = false) : InAppApi {

    override fun pause() {
        (if (loggingInstance) mobileEngage().loggingInAppInternal else mobileEngage().inAppInternal)
                .pause()
    }

    override fun resume() {
        (if (loggingInstance) mobileEngage().loggingInAppInternal else mobileEngage().inAppInternal)
                .resume()
    }

    override val isPaused: Boolean
        get() = (if (loggingInstance) mobileEngage().loggingInAppInternal else mobileEngage().inAppInternal)
                .isPaused

    override fun setEventHandler(eventHandler: EventHandler) {
        (if (loggingInstance) mobileEngage().loggingInAppInternal else mobileEngage().inAppInternal)
                .eventHandler = eventHandler
    }
}