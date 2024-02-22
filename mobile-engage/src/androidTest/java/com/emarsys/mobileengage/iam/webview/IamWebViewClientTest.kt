package com.emarsys.mobileengage.iam.webview

import android.webkit.WebView
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.fake.FakeMessageLoadedListener
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch

class IamWebViewClientTest {
    private lateinit var latch: CountDownLatch
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder


    @BeforeEach
    fun setUp() {
        latch = CountDownLatch(1)
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testOnPageFinished_shouldCallListener() {
        val listener = FakeMessageLoadedListener(latch)
        val client = IamWebViewClient(listener, concurrentHandlerHolder)
        concurrentHandlerHolder.postOnMain {
            client.onPageFinished(
                WebView(getTargetContext().applicationContext),
                ""
            )
        }
        latch.await()

        listener.invocationCount.toLong() shouldBe 1
    }

    @Test
    @Throws(InterruptedException::class)
    fun testOnPageFinished_shouldCallListener_shouldCallOnMainThread() {
        val listener = FakeMessageLoadedListener(latch, FakeMessageLoadedListener.Mode.MAIN_THREAD)
        val client = IamWebViewClient(listener, concurrentHandlerHolder)
        concurrentHandlerHolder.postOnMain {
            val webView = WebView(getTargetContext().applicationContext)
            Thread { client.onPageFinished(webView, "") }.start()
        }
        latch.await()
        listener.invocationCount.toLong() shouldBe 1
    }
}