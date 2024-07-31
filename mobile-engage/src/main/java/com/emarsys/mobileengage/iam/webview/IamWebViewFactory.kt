package com.emarsys.mobileengage.iam.webview

import android.content.Context
import com.emarsys.core.Mockable
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactoryProvider

@Mockable
class IamWebViewFactory(
    private val jsBridgeFactory: IamJsBridgeFactory,
    private val jsCommandFactoryProvider: JSCommandFactoryProvider,
    private val concurrentHandlerHolder: ConcurrentHandlerHolder
) {

    fun create(context: Context): IamWebView {
        return IamWebView(
            concurrentHandlerHolder, jsBridgeFactory,
            jsCommandFactoryProvider.provide(), context
        )
    }

}