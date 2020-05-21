package com.emarsys.inapp

import com.emarsys.core.Mockable
import com.emarsys.core.di.Container.getDependency
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.iam.InAppInternal

@Mockable
class InApp(private val loggingInstance: Boolean = false) : InAppApi {

    override fun pause() {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<InAppInternal>("defaultInstance"))
                .pause()
    }

    override fun resume() {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<InAppInternal>("defaultInstance"))
                .resume()
    }

    override fun isPaused(): Boolean {
        return (if (loggingInstance) getDependency("loggingInstance") else getDependency<InAppInternal>("defaultInstance"))
                .isPaused
    }

    override fun setEventHandler(eventHandler: EventHandler) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<InAppInternal>("defaultInstance"))
                .eventHandler = eventHandler
    }
}