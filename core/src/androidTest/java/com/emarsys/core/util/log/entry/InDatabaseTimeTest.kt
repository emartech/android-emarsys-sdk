package com.emarsys.core.util.log.entry

import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

import java.net.URL

class InDatabaseTimeTest {
    companion object {
        const val ID = "id12345"
        const val REQUEST_MODEL_CREATED = 40L
        const val REQUEST_MODEL_SENT = 200L
        val URL = URL("https://emarsys.com")
    }

    private lateinit var inDbTime: InDatabaseTime
    private lateinit var requestModel: RequestModel

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun init() {
        requestModel = RequestModel(
                URL.toString(),
                RequestMethod.GET,
                null,
                mapOf(),
                REQUEST_MODEL_CREATED,
                Long.MAX_VALUE, ID)
        inDbTime = InDatabaseTime(requestModel, REQUEST_MODEL_SENT)
    }

    @Test
    fun testTopic() {
        inDbTime.topic shouldBe "log_in_database_time"
    }

    @Test
    fun testGetData() {
        val result = inDbTime.data
        val expected = mapOf(
                "request_id" to ID,
                "start" to REQUEST_MODEL_CREATED,
                "end" to REQUEST_MODEL_SENT,
                "duration" to REQUEST_MODEL_SENT - REQUEST_MODEL_CREATED,
                "url" to URL.toString()
        )
        result shouldBe expected
    }

}