package com.emarsys.core.connection

import android.content.Context
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext

import androidx.test.filters.SdkSuppress
import com.emarsys.core.fake.FakeConnectionChangeListener
import android.os.Build
import android.os.Handler
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.handler.CoreSdkHandler
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.mock
import java.util.concurrent.CountDownLatch

class ConnectivityChangeReceiverTest {
    private lateinit var receiver: ConnectionWatchDog.ConnectivityChangeReceiver
    private lateinit var mockListener: ConnectionChangeListener
    private lateinit var context: Context

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule
    lateinit var sdkHandler: CoreSdkHandler

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        sdkHandler = CoreSdkHandlerProvider().provideHandler()

        mockListener = mock()
    }

    @After
    fun tearDown() {
        sdkHandler.looper.quit()
    }

    @Test
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.N_MR1)
    fun testOnReceive_listenerShouldCall_onCoreSDKThread() {
        val latch = CountDownLatch(1)
        val fakeListener = FakeConnectionChangeListener(latch)
        val expectedName = sdkHandler.looper.thread.name
        receiver = ConnectionWatchDog(context, sdkHandler).ConnectivityChangeReceiver(fakeListener)
        receiver.onReceive(context, mock())
        latch.await()

        fakeListener.onConnectionChangedCount shouldBe 1
        fakeListener.threadName shouldBe expectedName

    }
}