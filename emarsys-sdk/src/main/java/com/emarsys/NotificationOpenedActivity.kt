package com.emarsys

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.emarsys.config.ConfigLoader
import com.emarsys.mobileengage.di.isMobileEngageComponentSetup
import com.emarsys.mobileengage.notification.NotificationCommandFactory
import com.emarsys.mobileengage.service.NotificationActionUtils.handleAction

class NotificationOpenedActivity : Activity() {
    private val configLoader = ConfigLoader()

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
            if (isMobileEngageComponentSetup()) {
                handleAction(intent, NotificationCommandFactory(this))
            } else {
                Emarsys.setup(
                    configLoader.loadConfigFromSharedPref(
                        application,
                        "emarsys_setup_cache"
                    ).build()
                )
                handleAction(intent, NotificationCommandFactory(this))
            }
        }
        finish()
    }
}