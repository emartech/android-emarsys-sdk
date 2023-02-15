package com.emarsys.mobileengage.iam.jsbridge

import android.content.ClipboardManager
import com.emarsys.core.Mockable
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.webview.Provider

@Mockable
class JSCommandFactoryProvider(
    val currentActivityProvider: CurrentActivityProvider,
    val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val inAppInternal: InAppInternal,
    private val buttonClickedRepository: Repository<ButtonClicked, SqlSpecification>,
    private val onCloseTriggered: OnCloseListener?,
    private val onAppEventTriggered: OnAppEventListener?,
    private val timestampProvider: TimestampProvider,
    private val clipboardManager: ClipboardManager): Provider<JSCommandFactory> {

    override fun provide(): JSCommandFactory {
        return JSCommandFactory(
            currentActivityProvider,
            concurrentHandlerHolder,
            inAppInternal,
            buttonClickedRepository,
            onCloseTriggered,
            onAppEventTriggered,
            timestampProvider,
            clipboardManager
        )
    }
}