package com.emarsys.core.connection

import android.content.Context
import android.os.Build
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.fake.FakeConnectionChangeListener
import com.emarsys.core.handler.ConcurrentHandlerHolder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.util.concurrent.CountDownLatch

class ConnectivityChangeReceiverTest {
    private lateinit var receiver: ConnectivityChangeReceiver
    private lateinit var mockListener: ConnectionChangeListener
    private lateinit var context: Context


    lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder

    @BeforeEach
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()

        mockListener = mock()
    }

    @AfterEach
    fun tearDown() {
        concurrentHandlerHolder.coreLooper.quit()
    }

    @Test
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.N_MR1)
    fun testOnReceive_listenerShouldCall_onCoreSDKThread() {
        val latch = CountDownLatch(1)
        val fakeListener = FakeConnectionChangeListener(latch)
        val expectedName = concurrentHandlerHolder.coreLooper.thread.name
        receiver = ConnectivityChangeReceiver(
            fakeListener,
            ConnectionWatchDog(context, concurrentHandlerHolder),
            concurrentHandlerHolder
        )
        receiver.onReceive(context, mock())
        latch.await()

        fakeListener.onConnectionChangedCount shouldBe 1
        fakeListener.threadName shouldBe expectedName

    }
}