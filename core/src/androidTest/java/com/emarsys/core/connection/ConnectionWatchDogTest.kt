package com.emarsys.core.connection

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.test.filters.SdkSuppress
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.handler.CoreSdkHandler
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
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers

class ConnectionWatchDogTest {
    private lateinit var context: Context
    private lateinit var mockHandler: CoreSdkHandler

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Before
    fun setup() {
        context = getTargetContext().applicationContext
        mockHandler = CoreSdkHandlerProvider().provideHandler()
    }

    @Test
    fun testConstructor_connectivityManagerShouldBeSet() {
        val watchDog = ConnectionWatchDog(context, mockHandler)
        val manager: ConnectivityManager =
            ReflectionTestUtils.getInstanceField(watchDog, "connectivityManager")!!
        Assert.assertNotNull(manager)
    }

    @Test
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.N_MR1)
    fun testRegisterReceiver_shouldCallRegisterReceiver() {
        val contextMock = getContextMock_withAppContext_withConnectivityManager(
            true,
            NetworkCapabilities.TRANSPORT_WIFI
        )
        val appContextMock = contextMock.applicationContext
        val connectionWatchDog = ConnectionWatchDog(contextMock, mockHandler)
        val connectionChangeListener: ConnectionChangeListener = mockk()

        connectionWatchDog.registerReceiver(connectionChangeListener)
        val captor = ArgumentCaptor.forClass(
            IntentFilter::class.java
        )

        verify {
            (appContextMock).registerReceiver(
                ArgumentMatchers.any(
                    ConnectionWatchDog.ConnectivityChangeReceiver::class.java
                ), captor.capture()
            )
        }
        Assert.assertTrue(captor.value.hasAction(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    @Test(expected = IllegalStateException::class)
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.N_MR1)
    fun testRegisterReceiver_shouldThrowException_whenReceiverRegistrationHasAlreadyCalled() {
        val contextMock = getContextMock_withAppContext_withConnectivityManager(
            true,
            NetworkCapabilities.TRANSPORT_VPN
        )
        val connectionWatchDog = ConnectionWatchDog(contextMock, mockHandler)
        val connectionChangeListener: ConnectionChangeListener = mockk()

        connectionWatchDog.registerReceiver(connectionChangeListener)
        connectionWatchDog.registerReceiver(connectionChangeListener)
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
    fun testRegisterReceiver_shouldRegisterNetworkCallback() {
        val contextMock = getContextMock_withAppContext_withConnectivityManager(
            true,
            NetworkCapabilities.TRANSPORT_VPN
        )
        val connectionWatchDog = ConnectionWatchDog(contextMock, mockHandler)
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
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
    fun testRegisterReceiver_registersCompletionListener_inOnCapabilitiesChanged() {
        val contextMock = getContextMock_withAppContext_withConnectivityManager(
            true,
            NetworkCapabilities.TRANSPORT_VPN
        )
        val connectionWatchDog = ConnectionWatchDog(contextMock, mockHandler)
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
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.N_MR1)
    fun testIsConnected_Online_belowO() {
        val contextMock = getContextMock_withAppContext_withConnectivityManager(
            true,
            ConnectivityManager.TYPE_MOBILE_DUN
        )
        val watchDog = ConnectionWatchDog(contextMock, mockHandler)
        Assert.assertTrue(watchDog.isConnected)
    }

    @Test
    fun testIsConnected_Online() {
        val contextMock = getContextMock_withAppContext_withConnectivityManager(
            true,
            NetworkCapabilities.TRANSPORT_WIFI
        )
        val watchDog = ConnectionWatchDog(contextMock, mockHandler)
        Assert.assertTrue(watchDog.isConnected)
    }

    @Test
    fun testIsConnected_Offline() {
        val contextMock = getContextMock_withAppContext_withConnectivityManager(false, -1)
        val watchDog = ConnectionWatchDog(contextMock, mockHandler)
        Assert.assertFalse(watchDog.isConnected)
    }
}