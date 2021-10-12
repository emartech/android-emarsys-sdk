package com.emarsys.core.response

import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.testUtil.TimeoutUtils
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.net.HttpCookie
import java.util.*

class ResponseModelTest {

    companion object {
        private const val timestamp: Long = 4200
    }

    private var statusCode: Int = Int.MIN_VALUE
    private lateinit var message: String
    private lateinit var headers: Map<String?, String>
    private lateinit var listHeaders: Map<String?, List<String>>
    private lateinit var cookies: Map<String, HttpCookie>
    private lateinit var body: String
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockRequestModel: RequestModel

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun init() {
        statusCode = 200
        message = "OK"
        headers = createHeaders()
        cookies = createCookies()
        listHeaders = headers.wrapValuesInList()
        body = "payload"
        mockTimestampProvider = mock {
            on { provideTimestamp() } doReturn timestamp
        }
        mockRequestModel = mock()
    }

    @Test
    @Throws(JSONException::class)
    fun testGetParsedBody_whenBodyIsAValidJson() {
        val responseModel = ResponseModel(
                200,
                "",
                mapOf(),
                mapOf(),
                "{ 'foo': 'bar', 'a': 1, 'nested': { 'b': 'c' }}",
                timestamp,
                mockRequestModel)

        val result = responseModel.parsedBody
        val expected = JSONObject()
                .put("foo", "bar")
                .put("a", 1)
                .put("nested", JSONObject().put("b", "c"))

        assertEquals(expected.toString(), result.toString())
    }

    @Test
    @Throws(JSONException::class)
    fun testGetParsedBody_whenBodyIsAnInvalidJson() {
        val responseModel = ResponseModel(
                200,
                "",
                mapOf(),
                mapOf(),
                "<html>Not valid json</html>",
                timestamp,
                mockRequestModel)

        val result = responseModel.parsedBody
        assertNull(result)
    }

    @Test
    @Throws(JSONException::class)
    fun testGetParsedBody_whenBodyIsNull() {
        val responseModel = ResponseModel(
                200,
                "",
                mapOf(),
                mapOf(),
                null,
                timestamp,
                mockRequestModel)

        val result = responseModel.parsedBody
        assertNull(result)
    }

    @Test
    fun testBuilder_withAllArguments() {
        val expected = ResponseModel(statusCode, message, headers, cookies, body, timestamp, mockRequestModel)
        val result = ResponseModel.Builder(mockTimestampProvider)
                .statusCode(statusCode)
                .message(message)
                .headers(listHeaders)
                .body(body)
                .requestModel(mockRequestModel)
                .build()
        assertEquals(expected, result)
    }

    @Test
    fun testBuilder_withMandatoryArguments() {
        val expected = ResponseModel(statusCode, message, mapOf(), mapOf(), body, timestamp, mockRequestModel)
        val result = ResponseModel.Builder(mockTimestampProvider)
                .statusCode(statusCode)
                .message(message)
                .body(body)
                .requestModel(mockRequestModel)
                .build()
        assertEquals(expected, result)
    }

    @Test
    fun testBuilder_joinShouldReturnConcatenatedValue() {
        val expected = "alma, korte, szilva, malna"
        val inputList = listOf("alma", "korte", "szilva", "malna")
        val delimiter = ", "
        val actual = inputList.joinToString(delimiter)
        assertEquals(expected, actual)
    }

    @Test
    fun testBuilder_convertHeadersShouldReturnJoinedHeaderValues() {
        val expected = HashMap<String, String>()
        expected["one"] = "alma, szilva"
        expected["two"] = "korte, malna"

        val inputHeaders = HashMap<String?, List<String>>()
        inputHeaders["one"] = listOf("alma", "szilva")
        inputHeaders["two"] = listOf("korte", "malna")

        val result = ResponseModel.Builder().convertHeaders(inputHeaders)
        assertEquals(expected, result)
    }

    @Test
    fun testBuilder_cookiesShouldBeEmpty_whenNoSetCookiesArePresent() {
        val headers = mapOf<String?, String>("content" to "application/x-www-form-urlencoded")
        val headersAsList = headers.wrapValuesInList()

        val expected = ResponseModel(statusCode, message, headers, mapOf(), null, timestamp, mockRequestModel)

        val result = ResponseModel.Builder(mockTimestampProvider)
                .statusCode(statusCode)
                .headers(headersAsList)
                .message(message)
                .body(null)
                .requestModel(mockRequestModel)
                .build()

        assertEquals(expected, result)
    }

    @Test
    fun testBuilder_cookiesAreSetWithHeaders_setCookieCaseInsensitive() {
        val headers = mapOf(
                null to "HTTP/1.1 200 OK",
                "content" to "application/x-www-form-urlencoded",
                "Set-Cookie" to "cdv=AAABBB;Path=/;Expires=Fri, 20-Sep-2019 14:30:24 GMT, s=ASDF1234",
                "set-cookie" to "UserID=JohnDoe; Max-Age=3600; Version=1"
        )
        val headersAsList = mapOf<String?, List<String>>(
                null to listOf("HTTP/1.1 200 OK"),
                "content" to listOf("application/x-www-form-urlencoded"),
                "Set-Cookie" to listOf(
                        "cdv=AAABBB;Path=/;Expires=Fri, 20-Sep-2019 14:30:24 GMT",
                        "s=ASDF1234"
                ),
                "set-cookie" to listOf("UserID=JohnDoe; Max-Age=3600; Version=1")
        )
        val cookies = mapOf(
                "cdv" to HttpCookie.parse("Set-Cookie: cdv=AAABBB;Path=/;Expires=Fri, 20-Sep-2019 14:30:24 GMT").first(),
                "s" to HttpCookie.parse("Set-Cookie: s=ASDF1234").first(),
                "UserID" to HttpCookie.parse("Set-Cookie: UserID=JohnDoe; Max-Age=3600; Version=1").first()
        )

        val expected = ResponseModel(statusCode, message, headers, cookies, null, timestamp, mockRequestModel)

        val result = ResponseModel.Builder(mockTimestampProvider)
                .statusCode(statusCode)
                .headers(headersAsList)
                .message(message)
                .body(null)
                .requestModel(mockRequestModel)
                .build()

        assertEquals(expected, result)
    }

    private fun createHeaders(): Map<String?, String> {
        return mapOf(
                "content" to "application/x-www-form-urlencoded",
                "set-cookie" to "UserID=JohnDoe; Max-Age=3600; Version=1"
        )
    }

    private fun <K, V> Map<K, V>.wrapValuesInList(): Map<K, List<V>> = this.mapValues { listOf(it.value) }

    private fun createCookies(): Map<String, HttpCookie> {
        return mapOf(
                "UserID" to HttpCookie("UserID", "JohnDoe")
        )
    }

}