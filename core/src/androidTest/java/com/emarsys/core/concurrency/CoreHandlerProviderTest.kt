package com.emarsys.core.concurrency

import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import com.emarsys.core.handler.CoreSdkHandler
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.*
import org.junit.rules.TestRule

class CoreHandlerProviderTest {
    private lateinit var provider: CoreSdkHandlerProvider
    private lateinit var provided: CoreSdkHandler

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule
    @Before
    fun setUp() {
        provider = CoreSdkHandlerProvider()
        provided = provider.provideHandler()
    }

    @After
    fun tearDown() {
        provided.looper.quit()
    }

    @Test
    fun testProvideHandler_shouldNotReturnNull() {
        provided shouldNotBe null
    }

    @Test
    fun testProvideHandler_shouldReturnCoreSdkHandler() {
        provided.javaClass shouldBe CoreSdkHandler::class.java
    }

    @Test
    fun testProvideHandler_shouldReturnCoreSdkHandlerWithCorrectName() {
        val expectedNamePrefix = "CoreSDKHandlerThread"
        val actualName = provided.looper.thread.name
        actualName.startsWith(expectedNamePrefix) shouldBe true
    }
}