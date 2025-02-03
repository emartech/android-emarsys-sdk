package com.emarsys.core.connection

import com.emarsys.core.request.model.RequestModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.types.beTheSameInstanceAs
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.net.URL

class ConnectionProviderTest  {
    companion object {
        const val HTTP_PATH = "http://emarsys.com"
        const val HTTPS_PATH = "https://emarsys.com"
    }

    private lateinit var provider: ConnectionProvider

    @Before
    fun setUp() {
        provider = ConnectionProvider()
    }

    @Test
    fun testProvideConnection_returnsCorrectConnection() {
        val url = URL(HTTPS_PATH)

        val requestModel = mock<RequestModel>(RequestModel::class.java)
        `when`<URL>(requestModel.url).thenReturn(url)

        val connection = provider.provideConnection(requestModel)

        connection.url should beTheSameInstanceAs(url)
    }

    @Test
    fun testProvideConnection_shouldNotAcceptHttpRequestModel() {
        val url = URL(HTTP_PATH)
        shouldThrow<IllegalArgumentException> {
            val requestModel = mock<RequestModel>(RequestModel::class.java)
            `when`<URL>(requestModel.url).thenReturn(url)

            provider.provideConnection(requestModel)
        }
    }
}