package com.emarsys.core.connection

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.test.filters.SdkSuppress
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.testUtil.ConnectionTestUtils.getContextMock_withAppContext_withConnectivityManager
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import io.mockk.*
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@SdkSuppress(minSdkVersion = Build.VERSION_CODES.P)
class ConnectionWatchDogTest {
    private lateinit var context: Context
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

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
        Assert.assertNotNull(manager)
    }

    @Test
    fun testRegisterReceiver_shouldRegisterNetworkCallback() {
        val contextMock = getContextMock_withAppContext_withConnectivityManager(
            true,
            NetworkCapabilities.TRANSPORT_VPN
        )
        val connectionWatchDog = ConnectionWatchDog(contextMock, concurrentHandlerHolder)
        val connectionChangeListener: ConnectionChangeListener = mockk()

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
        val contextMock = getContextMock_withAppContext_withConnectivityManager(
            true,
            NetworkCapabilities.TRANSPORT_VPN
        )
        val connectionWatchDog = ConnectionWatchDog(contextMock, concurrentHandlerHolder)
        val connectionChangeListener: ConnectionChangeListener = mockk()
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
        val contextMock = getContextMock_withAppContext_withConnectivityManager(
            true,
            NetworkCapabilities.TRANSPORT_WIFI
        )
        val watchDog = ConnectionWatchDog(contextMock, concurrentHandlerHolder)
        Assert.assertTrue(watchDog.isConnected)
    }

    @Test
    fun testIsConnected_Offline() {
        val contextMock = getContextMock_withAppContext_withConnectivityManager(false, -1)
        val watchDog = ConnectionWatchDog(contextMock, concurrentHandlerHolder)
        Assert.assertFalse(watchDog.isConnected)
    }
}