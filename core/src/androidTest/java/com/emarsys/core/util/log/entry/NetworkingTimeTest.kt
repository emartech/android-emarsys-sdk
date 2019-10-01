package com.emarsys.core.util.log.entry

import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class NetworkingTimeTest{

    companion object {
        const val ID = "id12345"
        const val STATUS_CODE = 200
        const val REQUEST_STARTED = 100L
        const val REQUEST_ENDED = 400L
        val URL = java.net.URL("https://emarsys.com")
    }

    private lateinit var networkingTime: NetworkingTime
    private lateinit var responseModel: ResponseModel

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun init() {
        val requestModel = RequestModel(
                URL.toString(),
                RequestMethod.GET,
                null,
                mapOf(),
                20L,
                Long.MAX_VALUE,
                ID
        )
        responseModel = ResponseModel(
                STATUS_CODE,
                "OK",
                mapOf(),
                mapOf(),
                "",
                REQUEST_ENDED,
                requestModel
        )
        networkingTime = NetworkingTime(responseModel, REQUEST_STARTED)
    }

    @Test
    fun testTopic() {
        networkingTime.topic shouldBe "log_networking_time"
    }

    @Test
    fun testData() {
        val actual = networkingTime.data
        val expected = mapOf(
                "start" to REQUEST_STARTED,
                "end" to REQUEST_ENDED,
                "duration" to REQUEST_ENDED - REQUEST_STARTED,
                "url" to URL.toString(),
                "request_id" to ID,
                "status_code" to STATUS_CODE
        )

        actual shouldBe expected
    }
}