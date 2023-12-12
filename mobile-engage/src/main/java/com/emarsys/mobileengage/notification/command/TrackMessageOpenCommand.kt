package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.push.PushInternal

class TrackMessageOpenCommand(
    private val pushInternal: PushInternal,
    private val sid: String?
) : Runnable {

    override fun run() {
        pushInternal.trackMessageOpen(sid, null)
    }
}