package com.emarsys.mobileengage.iam.jsbridge

import android.content.ClipboardManager
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock

class JSCommandFactoryProviderTest : AnnotationSpec() {

    @Test
    fun provide_shouldReturnJSCommandFactory() {
        val currentActivityProvider: CurrentActivityProvider = mock()
        val concurrentHandlerHolder: ConcurrentHandlerHolder = mock()
        val inAppInternal: InAppInternal = mock()
        val buttonClickedRepository: Repository<ButtonClicked, SqlSpecification> = mock()
        val onCloseTriggered: OnCloseListener = mock()
        val onAppEventTriggered: OnAppEventListener = mock()
        val timestampProvider: TimestampProvider = mock()
        val clipboardManager: ClipboardManager = mock()

        val provider = JSCommandFactoryProvider(
            currentActivityProvider,
            concurrentHandlerHolder,
            inAppInternal,
            buttonClickedRepository,
            onCloseTriggered,
            onAppEventTriggered,
            timestampProvider,
            clipboardManager
        )

        val result = provider.provide()

        result::class shouldBe JSCommandFactory::class
    }

}