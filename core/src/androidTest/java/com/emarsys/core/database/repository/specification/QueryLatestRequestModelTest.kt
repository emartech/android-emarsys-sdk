package com.emarsys.core.database.repository.specification

import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.RequestModelRepository
import com.emarsys.core.request.model.specification.QueryLatestRequestModel
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class QueryLatestRequestModelTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.timeoutRule

    private lateinit var specification: QueryLatestRequestModel

    @Before
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()
        specification = QueryLatestRequestModel()
    }

    @Test
    fun testSpecification() {
        with(QueryLatestRequestModel()) {
            isDistinct shouldBe false
            columns shouldBe null
            selection shouldBe null
            selectionArgs shouldBe null
            groupBy shouldBe null
            having shouldBe null
            orderBy shouldBe "ROWID ASC"
            limit shouldBe "1"
        }
    }

    @Test
    fun testQueryUsingQueryNewestRequestModel() {
        specification = QueryLatestRequestModel()
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

        resultList.size shouldBe 1
        resultList[0] shouldBe expectedRequestModel
    }
}