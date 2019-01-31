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
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.net.HttpCookie
import java.util.*

class ResponseModelTest {

    companion object {
        private val timestamp: Long = 4200
    }

    private var statusCode: Int = Int.MIN_VALUE
    private lateinit var message: String
    private lateinit var headers: Map<String, String>
    private lateinit var listHeaders: Map<String, List<String>>
    private lateinit var cookies: Map<String, HttpCookie>
    private lateinit var body: String
    private lateinit var timestampProvider: TimestampProvider

    private lateinit var requestModel: RequestModel

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
        timestampProvider = mock(TimestampProvider::class.java)
        `when`(timestampProvider.provideTimestamp()).thenReturn(timestamp)
        requestModel = mock(RequestModel::class.java)
    }

    @Test(expected = IllegalArgumentException::class)
    fun test_statusCodeShouldNotBeBelow200() {
        ResponseModel(199, message, headers, cookies, body, timestamp, requestModel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun test_statusCodeShouldNotBeOver600() {
        ResponseModel(600, message, headers, cookies, body, timestamp, requestModel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun test_messageShouldNotBeNull() {
        ResponseModel(statusCode, null, headers, cookies, body, timestamp, requestModel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun test_headersShouldNotBeNull() {
        ResponseModel(statusCode, message, null, cookies, body, timestamp, requestModel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun test_cookiesShouldNotBeNull() {
        ResponseModel(statusCode, message, headers, null, body, timestamp, requestModel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun test_requestModelShouldNotBeNull() {
        ResponseModel(statusCode, message, headers, cookies, body, timestamp, null)
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
                requestModel)

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
                requestModel)

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
                requestModel)

        val result = responseModel.parsedBody
        assertNull(result)
    }

    @Test
    fun testBuilder_withAllArguments() {
        val expected = ResponseModel(statusCode, message, headers, cookies, body, timestamp, requestModel)
        val result = ResponseModel.Builder(timestampProvider)
                .statusCode(statusCode)
                .message(message)
                .headers(listHeaders)
                .body(body)
                .requestModel(requestModel)
                .build()
        assertEquals(expected, result)
    }

    @Test
    fun testBuilder_withMandatoryArguments() {
        val expected = ResponseModel(statusCode, message, mapOf(), mapOf(), body, timestamp, requestModel)
        val result = ResponseModel.Builder(timestampProvider)
                .statusCode(statusCode)
                .message(message)
                .body(body)
                .requestModel(requestModel)
                .build()
        assertEquals(expected, result)
    }

    @Test
    fun testBuilder_joinShouldReturnConcatenatedValue() {
        val expected = "alma, korte, szilva, malna"
        val inputList = Arrays.asList("alma", "korte", "szilva", "malna")
        val delimiter = ", "
        val actual = ResponseModel.Builder().join(delimiter, inputList)
        assertEquals(expected, actual)
    }

    @Test
    fun testBuilder_convertHeadersShouldReturnJoinedHeaderValues() {
        val expected = HashMap<String, String>()
        expected["one"] = "alma, szilva"
        expected["two"] = "korte, malna"

        val inputHeaders = HashMap<String, List<String>>()
        inputHeaders["one"] = listOf("alma", "szilva")
        inputHeaders["two"] = listOf("korte", "malna")

        val result = ResponseModel.Builder().convertHeaders(inputHeaders)
        assertEquals(expected, result)
    }

    @Test
    fun testBuilder_cookiesShouldBeEmpty_whenNoSetCookiesArePresent() {
        val headers = mapOf("content" to "application/x-www-form-urlencoded")
        val headersAsList = headers.wrapValuesInList()

        val expected = ResponseModel(statusCode, message, headers, mapOf(), null, timestamp, requestModel)

        val result = ResponseModel.Builder(timestampProvider)
                .statusCode(statusCode)
                .headers(headersAsList)
                .message(message)
                .body(null)
                .requestModel(requestModel)
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
        val headersAsList = mapOf(
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

        val expected = ResponseModel(statusCode, message, headers, cookies, null, timestamp, requestModel)

        val result = ResponseModel.Builder(timestampProvider)
                .statusCode(statusCode)
                .headers(headersAsList)
                .message(message)
                .body(null)
                .requestModel(requestModel)
                .build()

        assertEquals(expected, result)
    }

    private fun createHeaders(): Map<String, String> {
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