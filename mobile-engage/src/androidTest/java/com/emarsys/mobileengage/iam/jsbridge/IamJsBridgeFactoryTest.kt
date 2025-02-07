package com.emarsys.mobileengage.iam.jsbridge

import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.Test

class IamJsBridgeFactoryTest  {

    @Test
    fun createJsBridge_shouldReturnJSBridge() {
        val jsCommandFactory: JSCommandFactory = mockk(relaxed = true)
        val jsBridgeFactory = IamJsBridgeFactory(ConcurrentHandlerHolderFactory.create())

        jsBridgeFactory.createJsBridge(jsCommandFactory)::class.java shouldBe IamJsBridge::class.java
    }
}