package com.emarsys.mobileengage.iam.webview

import com.emarsys.core.Mockable
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactoryProvider

@Mockable
class IamWebViewProvider(
    private val jsBridgeFactory: IamJsBridgeFactory,
    private val jsCommandFactoryProvider: JSCommandFactoryProvider,
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val currentActivityProvider: CurrentActivityProvider
): Provider<IamWebView> {

    override fun provide(): IamWebView {
        return IamWebView(
            concurrentHandlerHolder, jsBridgeFactory,
            jsCommandFactoryProvider.provide(), currentActivityProvider
        )
    }

}