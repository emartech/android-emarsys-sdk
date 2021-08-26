package com.emarsys.mobileengage.service

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.emarsys.mobileengage.notification.NotificationCommandFactory
import com.emarsys.mobileengage.service.NotificationActionUtils.handleAction

class NotificationOpenedActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processIntent()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        processIntent()
    }

    private fun processIntent() {
        if (intent != null) {
            handleAction(intent, NotificationCommandFactory(this))
        }
        finish()
    }
}