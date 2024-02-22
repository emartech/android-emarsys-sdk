package com.emarsys.testUtil.rules

import android.app.Application
import com.emarsys.testUtil.ConnectionTestUtils
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class ConnectionExtension(private val application: Application) : BeforeEachCallback {
    override fun beforeEach(p0: ExtensionContext?) {
        ConnectionTestUtils.checkConnection(application)
    }


}