package com.emarsys.test.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

object ConnectionTestUtils {

    @JvmStatic
    fun getConnectivityManagerMock(isConnected: Boolean, connectionType: Int): ConnectivityManager {
        val managerMock = mock(ConnectivityManager::class.java)
        val networkInfoMock = mock(NetworkInfo::class.java)
        `when`(managerMock.activeNetworkInfo).thenReturn(networkInfoMock)
        `when`(networkInfoMock.isConnected).thenReturn(isConnected)
        `when`(networkInfoMock.type).thenReturn(connectionType)
        return managerMock
    }

    @JvmStatic
    fun getContextMock_withAppContext_withConnectivityManager(isConnected: Boolean, connectionType: Int): Context {
        val contextMock = mock(Context::class.java)
        val applicationContextMock = mock(Context::class.java)
        val managerMock = getConnectivityManagerMock(isConnected, connectionType)

        `when`(contextMock.applicationContext).thenReturn(applicationContextMock)
        `when`(applicationContextMock.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(managerMock)
        return contextMock
    }

    @JvmStatic
    fun getContextMock_withConnectivityManager(isConnected: Boolean, connectionType: Int): Context {
        val contextMock = mock(Context::class.java)
        val managerMock = getConnectivityManagerMock(isConnected, connectionType)

        `when`(contextMock.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(managerMock)
        return contextMock
    }

    @JvmStatic
    fun checkConnection(context: Context) {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = manager.activeNetworkInfo
        if (info == null || !info.isConnected) {
            throw RuntimeException("Device is not connected to the Internet!")
        }
    }
}
