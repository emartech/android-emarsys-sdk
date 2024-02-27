package com.emarsys.core.connection

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.test.filters.SdkSuppress
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.worker.DefaultWorker
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.ConnectionTestUtils.getContextMockWithAppContextWithConnectivityManager
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.ReflectionTestUtils
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify


@SdkSuppress(minSdkVersion = Build.VERSION_CODES.P)
class ConnectionWatchDogTest : AnnotationSpec() {
    private lateinit var context: Context
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder


    @Before
    fun setup() {
        context = getTargetContext().applicationContext
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
    }

    @Test
    fun testConstructor_connectivityManagerShouldBeSet() {
        val watchDog = ConnectionWatchDog(context, concurrentHandlerHolder)
        val manager: ConnectivityManager =
            ReflectionTestUtils.getInstanceField(watchDog, "connectivityManager")!!
        manager shouldNotBe null
    }

    @Test
    fun testRegisterReceiver_shouldRegisterNetworkCallback() {
        val contextMock = getContextMockWithAppContextWithConnectivityManager(
            true,
            NetworkCapabilities.TRANSPORT_VPN
        )
        val connectionWatchDog = ConnectionWatchDog(contextMock, concurrentHandlerHolder)
        val connectionChangeListener: DefaultWorker = mockk()

        connectionWatchDog.registerReceiver(connectionChangeListener)
        val manager =
            ReflectionTestUtils.getInstanceField<ConnectivityManager>(
                connectionWatchDog,
                "connectivityManager"
            )
        verify {
            manager!!.registerNetworkCallback(any(), any(), any())
        }
    }

    @Test
    fun testRegisterReceiver_registersCompletionListener_inOnCapabilitiesChanged() {
        val contextMock = getContextMockWithAppContextWithConnectivityManager(
            true,
            NetworkCapabilities.TRANSPORT_VPN
        )
        val connectionWatchDog = ConnectionWatchDog(contextMock, concurrentHandlerHolder)
        val connectionChangeListener: DefaultWorker = mockk()
        every { connectionChangeListener.onConnectionChanged(any(), any()) } just Runs
        val manager =
            contextMock.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork
        connectionWatchDog.registerReceiver(connectionChangeListener)
        connectionWatchDog.onCapabilitiesChanged(
            network!!,
            manager.getNetworkCapabilities(network)!!
        )
        verify {
            connectionChangeListener.onConnectionChanged(any(), any())
        }
    }

    @Test
    fun testIsConnected_Online() {
        val contextMock = getContextMockWithAppContextWithConnectivityManager(
            true,
            NetworkCapabilities.TRANSPORT_WIFI
        )
        val watchDog = ConnectionWatchDog(contextMock, concurrentHandlerHolder)

        watchDog.isConnected shouldBe true
    }

    @Test
    fun testIsConnected_Offline() {
        val contextMock = getContextMockWithAppContextWithConnectivityManager(false, -1)
        val watchDog = ConnectionWatchDog(contextMock, concurrentHandlerHolder)
        watchDog.isConnected shouldBe false
    }
}