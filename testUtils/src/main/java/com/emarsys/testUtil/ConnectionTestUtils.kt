package com.emarsys.testUtil

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import io.mockk.every
import io.mockk.mockk

object ConnectionTestUtils {

    @JvmStatic
    fun getConnectivityManagerMock(isConnected: Boolean, connectionType: Int): ConnectivityManager {
        val mockManager: ConnectivityManager = mockk(relaxed = true)
        val mockNetwork: Network = mockk(relaxed = true)
        val mockNetworkCapabilities: NetworkCapabilities = mockk(relaxed = true)

        every { mockManager.activeNetwork } returns mockNetwork
        every { mockManager.getNetworkCapabilities(mockNetwork) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(connectionType) } returns isConnected

        return mockManager
    }

    @JvmStatic
    fun getContextMock_withAppContext_withConnectivityManager(
        isConnected: Boolean,
        connectionType: Int
    ): Context {
        val contextMock: Context = mockk(relaxed = true)
        val applicationContextMock: Context = mockk(relaxed = true)
        val managerMock = getConnectivityManagerMock(isConnected, connectionType)

        every { contextMock.applicationContext} returns applicationContextMock
        every { applicationContextMock.getSystemService(Context.CONNECTIVITY_SERVICE)} returns managerMock
        return contextMock
    }

    @JvmStatic
    fun checkConnection(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.activeNetwork
            ?: throw RuntimeException("Device is not connected to the Internet!")

        val network = connectivityManager.getNetworkCapabilities(networkCapabilities)
            ?: throw RuntimeException("Device is not connected to the Internet!")

        if (!network.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            && !network.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            && !network.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        ) {
            throw RuntimeException("Device is not connected to the Internet!")
        }
    }
}
