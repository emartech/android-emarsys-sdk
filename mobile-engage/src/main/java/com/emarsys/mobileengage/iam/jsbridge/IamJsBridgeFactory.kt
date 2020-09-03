package com.emarsys.mobileengage.iam.jsbridge

import android.os.Handler
import com.emarsys.core.Mockable
import com.emarsys.mobileengage.iam.model.InAppMessage

@Mockable
class IamJsBridgeFactory(private val uiHandler: Handler) {

    fun createJsBridge(jsCommandFactory: JSCommandFactory, inAppMessage: InAppMessage): IamJsBridge {
        return IamJsBridge(uiHandler, jsCommandFactory, inAppMessage)
    }
}