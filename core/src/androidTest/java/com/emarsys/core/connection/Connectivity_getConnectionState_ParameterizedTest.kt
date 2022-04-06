package com.emarsys.core.connection

import android.net.NetworkCapabilities
import android.os.Build
import androidx.test.filters.SdkSuppress
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.testUtil.ConnectionTestUtils.getConnectivityManagerMock
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.ReflectionTestUtils.setInstanceField
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.tables.row
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@SdkSuppress(minSdkVersion = Build.VERSION_CODES.P)
class Connectivity_getConnectionState_ParameterizedTest {
    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Test
    fun testGetConnectionState_connectionWatchDog() {
        forall(
                row(false, NetworkCapabilities.TRANSPORT_WIFI, ConnectionState.DISCONNECTED),
                row(false, NetworkCapabilities.TRANSPORT_CELLULAR, ConnectionState.DISCONNECTED),
                row(false, NetworkCapabilities.TRANSPORT_ETHERNET, ConnectionState.DISCONNECTED),
                row(false, NetworkCapabilities.TRANSPORT_VPN, ConnectionState.DISCONNECTED),

                row(true, NetworkCapabilities.TRANSPORT_WIFI, ConnectionState.CONNECTED),
                row(true, NetworkCapabilities.TRANSPORT_CELLULAR, ConnectionState.CONNECTED_MOBILE_DATA),
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