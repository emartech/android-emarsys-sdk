package com.emarsys.mobileengage.notification.command

import android.content.Context
import com.emarsys.core.Mockable
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.event.CacheableEventHandler
import kotlinx.coroutines.launch
import org.json.JSONObject

@Mockable
class AppEventCommand(
    private val context: Context,
    private val cacheableEventHandler: CacheableEventHandler,
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    val name: String,
    val payload: JSONObject?
) : Runnable {

    val notificationEventHandler: EventHandler?
        get() = cacheableEventHandler

    override fun run() {
        val eventHandler = cacheableEventHandler
        concurrentHandlerHolder.uiScope.launch {
            eventHandler.handleEvent(context, name, payload)
        }
    }
}