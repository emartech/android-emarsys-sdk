package com.emarsys.mobileengage.iam.jsbridge

import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock

class IamJsBridgeFactoryTest : AnnotationSpec() {

    @Test
    fun createJsBridge_shouldReturnJSBridge() {
        val jsCommandFactory: JSCommandFactory = mock()
        val jsBridgeFactory = IamJsBridgeFactory(ConcurrentHandlerHolderFactory.create())

        val result = jsBridgeFactory.createJsBridge(jsCommandFactory)

        (result is IamJsBridge) shouldBe true
    }
}