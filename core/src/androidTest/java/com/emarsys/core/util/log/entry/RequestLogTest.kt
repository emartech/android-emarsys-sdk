package com.emarsys.core.util.log.entry

import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Test
import java.net.URL

class RequestLogTest {
    private companion object {
        const val TOPIC = "log_request"
        const val IN_DATABASE_TIME_END = 3L
    }

    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRequestModel: RequestModel
    private lateinit var requestLog: RequestLog

    @Before
    fun setUp() {
        mockRequestModel = mock {
            on { url }.doReturn(URL("https://emarsys.com"))
            on { timestamp }.doReturn(1L)
            on { id }.doReturn("testId")
        }

        mockResponseModel = mock {
            on { requestModel }.doReturn(mockRequestModel)
            on { timestamp }.doReturn(6L)
            on { statusCode }.doReturn(200)
        }

        requestLog = RequestLog(mockResponseModel, IN_DATABASE_TIME_END)
    }

    @Test
    fun testTopic() {
        val topic = requestLog.topic

        topic shouldBe TOPIC
    }

    @Test
    fun testData() {
        val expected = mapOf(
                "request_id" to "testId",
                "url" to URL("https://emarsys.com"),
                "status_code" to 200,
                "in_db_start" to 1L,
                "in_db_end" to 3L,
                "in_db_duration" to 2L,
                "networking_start" to 3L,
                "networking_end" to 6L,
                "networking_duration" to 3L
        )

        val data = requestLog.data

        data shouldBe expected
    }
}