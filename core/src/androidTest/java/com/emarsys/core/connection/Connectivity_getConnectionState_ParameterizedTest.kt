package com.emarsys.core.connection

import android.net.NetworkCapabilities
import android.os.Build
import androidx.test.filters.SdkSuppress
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.testUtil.ConnectionTestUtils.getConnectivityManagerMock
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.ReflectionTestUtils.setInstanceField
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Test

@SdkSuppress(minSdkVersion = Build.VERSION_CODES.P)
class Connectivity_getConnectionState_ParameterizedTest  {

    @Test
    fun testGetConnectionState_connectionWatchDog() = runBlocking {
        forAll(
            row(false, NetworkCapabilities.TRANSPORT_WIFI, ConnectionState.DISCONNECTED),
            row(false, NetworkCapabilities.TRANSPORT_CELLULAR, ConnectionState.DISCONNECTED),
            row(false, NetworkCapabilities.TRANSPORT_ETHERNET, ConnectionState.DISCONNECTED),
            row(false, NetworkCapabilities.TRANSPORT_VPN, ConnectionState.DISCONNECTED),

            row(true, NetworkCapabilities.TRANSPORT_WIFI, ConnectionState.CONNECTED),
            row(
                true,
                NetworkCapabilities.TRANSPORT_CELLULAR,
                ConnectionState.CONNECTED_MOBILE_DATA
            ),
            row(true, NetworkCapabilities.TRANSPORT_ETHERNET, ConnectionState.CONNECTED),
            row(true, NetworkCapabilities.TRANSPORT_VPN, ConnectionState.CONNECTED)
        ) { isConnected, connectionType, expectedConnectionState ->
            val connectionWatchDog = ConnectionWatchDog(
                getTargetContext(), ConcurrentHandlerHolderFactory.create()
            )
            setInstanceField(
                connectionWatchDog,
                "connectivityManager",
                getConnectivityManagerMock(isConnected, connectionType)
            )

            connectionWatchDog.connectionState shouldBe expectedConnectionState
        }
    }
}