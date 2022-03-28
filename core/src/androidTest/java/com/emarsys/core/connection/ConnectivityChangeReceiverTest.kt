package com.emarsys.core.connection

import android.content.Context
import android.os.Build
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.fake.FakeConnectionChangeListener
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.mock
import java.util.concurrent.CountDownLatch

class ConnectivityChangeReceiverTest {
    private lateinit var receiver: ConnectivityChangeReceiver
    private lateinit var mockListener: ConnectionChangeListener
    private lateinit var context: Context

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule
    lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()

        mockListener = mock()
    }

    @After
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