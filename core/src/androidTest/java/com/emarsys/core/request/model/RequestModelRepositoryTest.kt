package com.emarsys.core.request.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_HEADERS
import com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_METHOD
import com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_PAYLOAD
import com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_REQUEST_ID
import com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_TIMESTAMP
import com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_TTL
import com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_URL
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.repository.specification.Everything
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.util.serialization.SerializationUtils
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.DatabaseTestUtils.deleteCoreDatabase
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.shouldBe
import org.json.JSONException
import org.junit.Assert
import org.mockito.Mockito
import java.util.*

class RequestModelRepositoryTest : AnnotationSpec() {


    companion object {
        private const val URL_EMARSYS = "https://www.emarsys.com"
        private const val REQUEST_ID = "idka"
        private const val TTL: Long = 600
        private val TIMESTAMP = System.currentTimeMillis()
        private const val URL = "https://www.google.com"

        init {
            Mockito.mock(Cursor::class.java)
        }
    }

    private var request: RequestModel? = null
    private var repository: RequestModelRepository? = null
    private var context: Context? = null
    private var concurrentHandlerHolder: ConcurrentHandlerHolder? = null
    private var headers: HashMap<String, String>? = null
    private var payload: HashMap<String, Any?>? = null

    @Before
    fun init() {
        deleteCoreDatabase()
        context = getTargetContext()
        val coreDbHelper = CoreDbHelper(context!!, mutableMapOf())
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        repository = RequestModelRepository(coreDbHelper, concurrentHandlerHolder)
        payload = HashMap()
        payload!!["payload1"] = "payload_value1"
        payload!!["payload2"] = "payload_value2"
        headers = HashMap()
        headers!!["header1"] = "header_value1"
        headers!!["header2"] = "header_value2"
        request =
            RequestModel(URL, RequestMethod.GET, payload, headers!!, TIMESTAMP, TTL, REQUEST_ID)
    }

    @Test
    fun testContentValuesFromItem() {
        val result = repository!!.contentValuesFromItem(request)
        result.getAsString(REQUEST_COLUMN_NAME_REQUEST_ID) shouldBe request!!.id
        result.getAsString(REQUEST_COLUMN_NAME_METHOD) shouldBe request!!.method.name
        result.getAsString(REQUEST_COLUMN_NAME_URL) shouldBe request!!.url.toString()
        Assert.assertArrayEquals(
            SerializationUtils.serializableToBlob(
                request!!.headers
            ), result.getAsByteArray(REQUEST_COLUMN_NAME_HEADERS)
        )
        Assert.assertArrayEquals(
            SerializationUtils.serializableToBlob(
                request!!.payload
            ), result.getAsByteArray(REQUEST_COLUMN_NAME_PAYLOAD)
        )
        Assert.assertEquals(
            request!!.timestamp,
            result.getAsLong(REQUEST_COLUMN_NAME_TIMESTAMP) as Long
        )
        Assert.assertEquals(request!!.ttl, result.getAsLong(REQUEST_COLUMN_NAME_TTL) as Long)
    }

    @Test
    fun testItemFromCursor() {
        val cursor = Mockito.mock(Cursor::class.java)
        whenever(cursor.getColumnIndexOrThrow(REQUEST_COLUMN_NAME_REQUEST_ID)).thenReturn(0)
        whenever(cursor.getString(0)).thenReturn(REQUEST_ID)
        whenever(cursor.getColumnIndexOrThrow(REQUEST_COLUMN_NAME_METHOD)).thenReturn(1)
        whenever(cursor.getString(1)).thenReturn(RequestMethod.GET.name)
        whenever(cursor.getColumnIndexOrThrow(REQUEST_COLUMN_NAME_URL)).thenReturn(2)
        whenever(cursor.getString(2)).thenReturn(URL)
        whenever(cursor.getColumnIndexOrThrow(REQUEST_COLUMN_NAME_HEADERS)).thenReturn(3)
        whenever(cursor.getBlob(3)).thenReturn(SerializationUtils.serializableToBlob(headers))
        whenever(cursor.getColumnIndexOrThrow(REQUEST_COLUMN_NAME_PAYLOAD)).thenReturn(4)
        whenever(cursor.getBlob(4)).thenReturn(SerializationUtils.serializableToBlob(payload))
        whenever(cursor.getColumnIndexOrThrow(REQUEST_COLUMN_NAME_TIMESTAMP)).thenReturn(5)
        whenever(cursor.getLong(5)).thenReturn(TIMESTAMP)
        whenever(cursor.getColumnIndexOrThrow(REQUEST_COLUMN_NAME_TTL)).thenReturn(6)
        whenever(cursor.getLong(6)).thenReturn(TTL)
        val result = repository!!.itemFromCursor(cursor)

        result shouldBe request
    }

    @Test
    @Throws(JSONException::class)
    fun testQuery_shouldFallBack_toEmptyMap_shouldDeserializationFail() {
        initializeDatabaseWithCorrectAndIncorrectData()
        val result = repository!!.query(Everything())
        val model1 =
            RequestModel(URL_EMARSYS, RequestMethod.POST, HashMap(), HashMap(), 100, 300, "id1")
        val model2 = RequestModel(
            URL_EMARSYS,
            RequestMethod.POST,
            createAttribute(),
            HashMap(),
            100,
            300,
            "id2"
        )
        val expected = listOf(model1, model2)
        result shouldBe expected
    }

    @Throws(JSONException::class)
    private fun initializeDatabaseWithCorrectAndIncorrectData() {
        val dbHelper = CoreDbHelper(
            context!!,
            mutableMapOf()
        )
        val db = dbHelper.writableCoreDatabase
        val jsonString = "{'key1': 'value1', 'key2':321}"
        val mapAttribute = createAttribute()
        val record1 = createContentValues("id1", jsonString)
        val record2 = createContentValues("id2", mapAttribute)
        db.insert(DatabaseContract.REQUEST_TABLE_NAME, null, record1)
        db.insert(DatabaseContract.REQUEST_TABLE_NAME, null, record2)
    }

    private fun createAttribute(): HashMap<String, Any?> {
        val mapAttribute = HashMap<String, Any?>()
        mapAttribute["key1"] = "value2"
        mapAttribute["key2"] = false
        mapAttribute["key3"] = 1000
        return mapAttribute
    }

    private fun createContentValues(id: String, attributes: Any): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(REQUEST_COLUMN_NAME_REQUEST_ID, id)
        contentValues.put(REQUEST_COLUMN_NAME_METHOD, RequestMethod.POST.toString())
        contentValues.put(REQUEST_COLUMN_NAME_URL, URL_EMARSYS)
        contentValues.put(
            REQUEST_COLUMN_NAME_HEADERS,
            SerializationUtils.serializableToBlob(HashMap<Any, Any>())
        )
        contentValues.put(
            REQUEST_COLUMN_NAME_PAYLOAD,
            SerializationUtils.serializableToBlob(attributes)
        )
        contentValues.put(REQUEST_COLUMN_NAME_TIMESTAMP, 100)
        contentValues.put(REQUEST_COLUMN_NAME_TTL, 300)
        return contentValues
    }
}