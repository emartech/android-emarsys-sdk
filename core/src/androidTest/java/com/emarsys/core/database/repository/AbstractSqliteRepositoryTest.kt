package com.emarsys.core.database.repository

import android.content.ContentValues
import android.database.Cursor
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.DatabaseContract.*
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.helper.DbHelper
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.util.serialization.SerializationUtils.serializableToBlob
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.*

class AbstractSqliteRepositoryTest {

    companion object {
        private const val DISTINCT = true
        private const val TABLE_NAME = "table"
        private val COLUMNS = arrayOf("col1", "col2", "col3")
        private const val SELECTION = "selection"
        private val SELECTION_ARGS = arrayOf("arg1")
        private const val GROUP_BY = "group by"
        private const val HAVING = "having condition"
        private const val ORDER_BY = "order by"
        private const val LIMIT = "limit amount"
    }

    private lateinit var repository: AbstractSqliteRepository<Any>
    private lateinit var dbHelperMock: DbHelper
    private lateinit var dbMock: CoreSQLiteDatabase
    private lateinit var dummySpecification: SqlSpecification

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()

        dummySpecification = sqlSpecification(
                DISTINCT,
                COLUMNS,
                SELECTION,
                SELECTION_ARGS,
                GROUP_BY,
                HAVING,
                ORDER_BY,
                LIMIT
        )

        dbMock = mock(CoreSQLiteDatabase::class.java)

        dbHelperMock = mock(DbHelper::class.java).apply {
            whenever(readableCoreDatabase).thenReturn(dbMock)
            whenever(writableCoreDatabase).thenReturn(dbMock)
        }

        repository = (mock(AbstractSqliteRepository::class.java, Mockito.CALLS_REAL_METHODS) as AbstractSqliteRepository<Any>).apply {
            tableName = TABLE_NAME
            dbHelper = dbHelperMock
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_tableName_mustNotBeNull() {
        val tableName: String? = null

        object : AbstractSqliteRepository<Any>(tableName, dbHelperMock) {
            override fun contentValuesFromItem(item: Any?) = null

            override fun itemFromCursor(cursor: Cursor?) = null

        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testAdd_shouldNotAcceptNull() {
        repository.add(null)
    }

    @Test
    fun testAdd_shouldInsertIntoDb() {
        val contentValues = ContentValues().apply {
            put("key", "value")
        }
        whenever(repository.contentValuesFromItem(any())).thenReturn(contentValues)

        val input = Any()

        repository.add(input)

        verify(repository).contentValuesFromItem(input)
        verify(dbMock).beginTransaction()
        verify(dbMock).insert(TABLE_NAME, null, contentValues)
        verify(dbMock).setTransactionSuccessful()
        verify(dbMock).endTransaction()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testQuery_shouldNotAcceptNull() {
        repository.query(null)
    }

    @Test
    fun testQuery_shouldReturnCorrectResult() {
        val cursor = mock(Cursor::class.java).apply {
            whenever(moveToFirst()).thenReturn(true)
            whenever(isAfterLast).thenReturn(false, false, false, true)
        }

        whenever(dbMock.query(
                DISTINCT,
                TABLE_NAME,
                COLUMNS,
                SELECTION,
                SELECTION_ARGS,
                GROUP_BY,
                HAVING,
                ORDER_BY,
                LIMIT)).thenReturn(cursor)

        val item1 = Any()
        val item2 = Any()
        val item3 = Any()

        whenever(repository.itemFromCursor(cursor)).thenReturn(item1, item2, item3)

        val result = repository.query(dummySpecification)

        verify(dbMock).query(
                DISTINCT,
                TABLE_NAME,
                COLUMNS,
                SELECTION,
                SELECTION_ARGS,
                GROUP_BY,
                HAVING,
                ORDER_BY,
                LIMIT)

        result shouldBe arrayOf(item1, item2, item3)
    }

    @Test
    fun testQuery_shouldReturnCorrectResult_whenCursorIsEmpty() {
        val cursor = mock(Cursor::class.java).apply {
            whenever(moveToFirst()).thenReturn(false)
        }

        whenever(dbMock.query(
                DISTINCT,
                TABLE_NAME,
                COLUMNS,
                SELECTION,
                SELECTION_ARGS,
                GROUP_BY,
                HAVING,
                ORDER_BY,
                LIMIT)).thenReturn(cursor)

        val result = repository.query(dummySpecification)

        result shouldBe emptyList()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRemove_shouldNotAcceptNull() {
        repository.remove(null)
    }

    @Test
    fun testRemove_shouldDeleteSpecifiedRow() {
        repository.remove(dummySpecification)

        verify(dbMock).beginTransaction()
        verify(dbMock).delete(TABLE_NAME, dummySpecification.selection, dummySpecification.selectionArgs)
        verify(dbMock).setTransactionSuccessful()
        verify(dbMock).endTransaction()
    }

    @Test
    fun testIsEmpty_shouldReturnFalse_whenThereAreRows() {
        val helper = CoreDbHelper(
                InstrumentationRegistry.getTargetContext(),
                HashMap())

        repository.dbHelper = helper
        repository.tableName = DatabaseContract.REQUEST_TABLE_NAME
        val db = helper.writableCoreDatabase

        listOf("https://google.com", "https://emarsys.com")
                .map(this::requestModel)
                .forEach {
                    db.insert(DatabaseContract.REQUEST_TABLE_NAME, null, contentValuesFrom(it))
                }

        repository.isEmpty shouldBe false
    }

    @Test
    fun testIsEmpty_shouldReturnTrue_whenTableIsEmpty() {
        val helper = CoreDbHelper(
                InstrumentationRegistry.getTargetContext(),
                HashMap())

        repository.dbHelper = helper
        repository.tableName = DatabaseContract.REQUEST_TABLE_NAME
        val db = helper.writableCoreDatabase

        db.execSQL("DELETE FROM request;")

        repository.isEmpty shouldBe true
    }

    private fun contentValuesFrom(item: RequestModel): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(REQUEST_COLUMN_NAME_REQUEST_ID, item.id)
        contentValues.put(REQUEST_COLUMN_NAME_METHOD, item.method.name)
        contentValues.put(REQUEST_COLUMN_NAME_URL, item.url.toString())
        contentValues.put(REQUEST_COLUMN_NAME_HEADERS, serializableToBlob(item.headers))
        contentValues.put(REQUEST_COLUMN_NAME_PAYLOAD, serializableToBlob(item.payload))
        contentValues.put(REQUEST_COLUMN_NAME_TIMESTAMP, item.timestamp)
        contentValues.put(REQUEST_COLUMN_NAME_TTL, item.ttl)
        return contentValues
    }

    private fun sqlSpecification(
            distinct: Boolean,
            columns: Array<String>,
            selection: String,
            selectionArgs: Array<String>,
            groupBy: String,
            having: String,
            orderBy: String,
            limit: String) = object : SqlSpecification {
        override fun isDistinct() = distinct

        override fun getColumns() = columns

        override fun getSelection() = selection

        override fun getSelectionArgs() = selectionArgs

        override fun getGroupBy() = groupBy

        override fun getHaving() = having

        override fun getOrderBy() = orderBy

        override fun getLimit() = limit
    }

    private fun requestModel(url: String) = RequestModel.Builder(TimestampProvider(), UUIDProvider()).url(url).build()
}