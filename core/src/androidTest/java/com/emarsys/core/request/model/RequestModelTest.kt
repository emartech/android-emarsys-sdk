package com.emarsys.core.request.model

import android.net.Uri
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito
import java.net.URL
import java.util.*

class RequestModelTest {
    private lateinit var url: String
    private lateinit var method: RequestMethod
    private lateinit var payload: Map<String, Any>
    private lateinit var headers: Map<String, String>
    private var timestamp: Long = 0
    private var ttl: Long = 0
    private lateinit var id: String
    private lateinit var timestampProvider: TimestampProvider
    private lateinit var uuidProvider: UUIDProvider

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun init() {
        url = "https://google.com"
        method = RequestMethod.PUT
        payload = createPayload()
        headers = createHeaders()
        timestamp = System.currentTimeMillis()
        ttl = 6000
        id = "uuid"
        timestampProvider = TimestampProvider()
        uuidProvider = UUIDProvider()
    }

    @Test
    @Throws(Exception::class)
    fun testBuilder_mandatoryArgumentsInitialized() {
        val result = RequestModel.Builder(timestampProvider, uuidProvider)
                .url(url)
                .build()
        Assert.assertEquals(URL(url), result.url)
    }

    @Test
    fun testBuilder_optionalArgumentsInitializedWithDefaultValue() {
        val result = RequestModel.Builder(timestampProvider, uuidProvider).url(url).build()
        Assert.assertEquals(HashMap<String, String>(), result.headers)
        Assert.assertEquals(null, result.payload)
        Assert.assertEquals(RequestMethod.POST, result.method)
        Assert.assertEquals(Long.MAX_VALUE, result.ttl)
    }

    @Test
    fun testBuilder_idAndTimestampInitialized() {
        val result = RequestModel.Builder(timestampProvider, uuidProvider).url(url).build()

        result.timestamp shouldNotBe null
        result.id shouldNotBe null

    }

    @Test
    fun testBuilder_withAllArguments() {
        val queryParams: MutableMap<String, String> = HashMap()
        queryParams["q1"] = "v1"
        val result = RequestModel.Builder(timestampProvider, uuidProvider)
                .url(url)
                .method(method)
                .payload(payload)
                .headers(headers)
                .queryParams(queryParams)
                .ttl(ttl)
                .build()
        val id = result.id
        val timestamp = result.timestamp
        val urlWithQueryParams = "$url?q1=v1"
        val expected = RequestModel(urlWithQueryParams, method, payload, headers, timestamp, ttl, id)

        result shouldBe expected
    }

    @Test
    fun testBuilder_timestampCorrectlySet() {
        val timestampProvider = Mockito.mock(TimestampProvider::class.java)
        val timestamp = 1L
        whenever(timestampProvider.provideTimestamp()).thenReturn(timestamp)
        val result = RequestModel.Builder(timestampProvider, uuidProvider)
                .url(url)
                .build()
        result.timestamp shouldBe timestamp
    }

    @Test
    fun testBuilder_requestIdCorrectlySet() {
        val uuidProvider = Mockito.mock(UUIDProvider::class.java)
        val requestId = "REQUEST_ID"
        Mockito.`when`(uuidProvider.provideId()).thenReturn(requestId)
        val result = RequestModel.Builder(timestampProvider, uuidProvider)
                .url(url)
                .build()
        Assert.assertEquals(requestId, result.id)
    }

    @Test
    fun testBuilder_queryParamIsCorrectlySet() {
        val queryParams: MutableMap<String, String> = HashMap()
        queryParams["key1"] = "value1"
        val result = RequestModel.Builder(timestampProvider, uuidProvider)
                .url("https://emarsys.com")
                .queryParams(queryParams).build()
        val uri = Uri.parse(result.url.toString())
        Assert.assertEquals("emarsys.com", uri.host)
        Assert.assertEquals("value1", uri.getQueryParameter("key1"))
        Assert.assertEquals(1, uri.queryParameterNames.size.toLong())
    }

    @Test
    fun testBuilder_multipleQueryParamsAreCorrectlySet() {
        val queryParams: MutableMap<String, String> = HashMap()
        queryParams["key1"] = "value1"
        queryParams["key2"] = "value2"
        val result = RequestModel.Builder(timestampProvider, uuidProvider)
                .url("https://emarsys.com")
                .queryParams(queryParams).build()
        val uri = Uri.parse(result.url.toString())
        Assert.assertEquals("emarsys.com", uri.host)
        Assert.assertEquals("value1", uri.getQueryParameter("key1"))
        Assert.assertEquals("value2", uri.getQueryParameter("key2"))
        Assert.assertEquals(2, uri.queryParameterNames.size.toLong())
    }

    @Test
    fun testBuilder_ignoresEmptyMap() {
        val result = RequestModel.Builder(timestampProvider, uuidProvider)
                .url("https://emarsys.com")
                .queryParams(HashMap()).build()
        Assert.assertEquals("https://emarsys.com", result.url.toString())
    }

    @Test
    fun testBuilder_from() {
        val expected = RequestModel(url, method, payload, headers, timestamp, ttl, id)
        val result = RequestModel.Builder(expected).build()
        Assert.assertEquals(expected, result)
    }

    private fun createPayload(): Map<String, Any> {
        val result: MutableMap<String, Any> = HashMap()
        result["key1"] = "value1"
        val value2: MutableMap<String, Any> = HashMap()
        value2["key3"] = "value3"
        value2["key4"] = 5
        result["key2"] = value2
        return result
    }

    private fun createHeaders(): Map<String, String> {
        val result: MutableMap<String, String> = HashMap()
        result["content"] = "application/x-www-form-urlencoded"
        result["accept"] = "application/json"
        return result
    }
}