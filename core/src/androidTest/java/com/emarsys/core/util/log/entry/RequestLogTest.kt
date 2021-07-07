package com.emarsys.core.util.log.entry

import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.net.URL

class RequestLogTest {
    private companion object {
        const val TOPIC = "log_request"
        const val IN_DATABASE_TIME_END = 3L
    }

    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRequestModel: RequestModel
    private lateinit var requestLog: RequestLog

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

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
                "requestId" to "testId",
                "url" to URL("https://emarsys.com"),
                "statusCode" to 200,
                "inDbStart" to 1L,
                "inDbEnd" to 3L,
                "inDbDuration" to 2L,
                "networkingStart" to 3L,
                "networkingEnd" to 6L,
                "networkingDuration" to 3L
        )

        val data = requestLog.data

        data shouldBe expected
    }

    @Test
    fun testDataWithDebugInformation() {
        val expected = mapOf(
                "requestId" to "testId",
                "url" to URL("https://emarsys.com"),
                "statusCode" to 200,
                "inDbStart" to 1L,
                "inDbEnd" to 3L,
                "inDbDuration" to 2L,
                "networkingStart" to 3L,
                "networkingEnd" to 6L,
                "networkingDuration" to 3L,
                "header" to "{test=header}",
                "payload" to "{test=payload}"
        )

        whenever(mockRequestModel.payload).thenReturn(mapOf("test" to "payload"))
        whenever(mockRequestModel.headers).thenReturn(mapOf("test" to "header"))

        requestLog = RequestLog(mockResponseModel, IN_DATABASE_TIME_END, mockRequestModel)

        val data = requestLog.data

        data shouldBe expected
    }
}