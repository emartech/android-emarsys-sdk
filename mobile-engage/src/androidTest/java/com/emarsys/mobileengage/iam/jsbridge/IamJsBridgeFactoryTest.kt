package com.emarsys.mobileengage.iam.jsbridge

import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import io.kotlintest.shouldBe
import org.junit.Test
import org.mockito.kotlin.mock

class IamJsBridgeFactoryTest {

    @Test
    fun createJsBridge_shouldReturnJSBridge() {
        val jsCommandFactory: JSCommandFactory = mock()
        val jsBridgeFactory = IamJsBridgeFactory(ConcurrentHandlerHolderFactory.create())

        val result = jsBridgeFactory.createJsBridge(jsCommandFactory)

        result::class shouldBe IamJsBridge::class
    }
}