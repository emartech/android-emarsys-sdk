package com.emarsys.core.database.repository.specification

import android.support.test.InstrumentationRegistry
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.RequestModelRepository
import com.emarsys.core.request.model.specification.QueryNewestRequestModel
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class QueryNewestRequestModelTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.timeoutRule

    private lateinit var specification: QueryNewestRequestModel

    @Before
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()
        specification = QueryNewestRequestModel()
    }

    @Test
    fun testGetSql() {
        val expected = "SELECT * FROM request ORDER BY ROWID ASC LIMIT 1;"
        val result = specification.sql

        result shouldBe expected
    }

    @Test
    fun testGetArgs() {
        specification.args shouldBe null
    }

    @Test
    fun testQueryUsingQueryNewestRequestModel() {
        specification = QueryNewestRequestModel()
        val timestampProvider = TimestampProvider()
        val uuidProvider = UUIDProvider()

        val context = InstrumentationRegistry.getTargetContext().applicationContext
        val coreDbHelper = CoreDbHelper(context, mapOf())
        val repository = RequestModelRepository(coreDbHelper)

        val expectedRequestModel = RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/1").build()
        val requestModel1 = RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/2").build()
        val requestModel2 = RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/3").build()

        repository.add(expectedRequestModel)
        repository.add(requestModel1)
        repository.add(requestModel2)

        val resultList = repository.query(specification)

        resultList[0] shouldBe expectedRequestModel
    }
}