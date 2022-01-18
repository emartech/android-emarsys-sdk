package com.emarsys.core.concurrency

import android.os.Handler
import android.os.Looper
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class CoreHandlerProviderTest {
    private lateinit var holderFactory: ConcurrentHandlerHolderFactory
    private lateinit var provided: ConcurrentHandlerHolder
    private lateinit var uiHandler: Handler

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Before
    fun setUp() {
        uiHandler = Handler(Looper.getMainLooper())
        holderFactory = ConcurrentHandlerHolderFactory(uiHandler)
        provided = holderFactory.create()
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
    fun testProvideHandler_shouldReturnConcurrentHandlerHolder() {
        provided.javaClass shouldBe ConcurrentHandlerHolder::class.java
    }

    @Test
    fun testProvideHandler_shouldReturnConcurrentHandlerHolderWithCorrectName() {
        val expectedNamePrefix = "CoreSDKHandlerThread"
        val actualName = provided.looper.thread.name
        actualName.startsWith(expectedNamePrefix) shouldBe true
    }
}