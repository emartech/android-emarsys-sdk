package com.emarsys.mobileengage.iam.jsbridge

import android.os.Handler
import com.emarsys.core.Mockable

@Mockable
class IamJsBridgeFactory(private val coreSdkHandler: Handler, private val uiHandler: Handler) {

    fun createJsBridge(): IamJsBridge {
        return IamJsBridge(coreSdkHandler, uiHandler)
    }
}