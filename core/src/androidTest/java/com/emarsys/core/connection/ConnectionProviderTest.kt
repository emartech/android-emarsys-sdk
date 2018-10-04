package com.emarsys.core.connection

import com.emarsys.core.request.model.RequestModel
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.matchers.beTheSameInstanceAs
import io.kotlintest.should
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.net.URL

class ConnectionProviderTest {
    companion object {
        const val HTTP_PATH = "http://emarsys.com"
        const val HTTPS_PATH = "https://emarsys.com"
    }

    lateinit var provider: ConnectionProvider

    @Rule
    @JvmField
    var timeout = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        provider = ConnectionProvider()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testProvideConnection_requestModel_shouldNotBeNull() {
        provider.provideConnection(null)
    }

    @Test
    fun testProvideConnection_returnsCorrectConnection() {
        val url = URL(HTTPS_PATH)

        val requestModel = mock<RequestModel>(RequestModel::class.java)
        `when`<URL>(requestModel.url).thenReturn(url)

        val connection = provider.provideConnection(requestModel)

        connection.url should beTheSameInstanceAs(url)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testProvideConnection_shouldNotAcceptHttpRequestModel() {
        val url = URL(HTTP_PATH)

        val requestModel = mock<RequestModel>(RequestModel::class.java)
        `when`<URL>(requestModel.url).thenReturn(url)

        provider.provideConnection(requestModel)
    }
}