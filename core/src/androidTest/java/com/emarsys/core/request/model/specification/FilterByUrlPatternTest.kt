package com.emarsys.core.request.model.specification

import android.support.test.InstrumentationRegistry
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.RequestModelRepository
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FilterByUrlPatternTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.timeoutRule

    private lateinit var specification: FilterByUrlPattern
    private lateinit var pattern: String

    @Before
    fun init() {
        pattern = "root/___/_%/event"
        specification = FilterByUrlPattern(pattern)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_patternMustNotBeNull() {
        FilterByUrlPattern(null)
    }

    @Test
    fun testGetSql() {
        specification.selection shouldBe "SELECT * FROM request WHERE url LIKE ?;"
    }

    @Test
    fun testGetArs() {
        specification.selectionArgs shouldBe arrayOf(pattern)
    }

    @Test
    fun testQueryUsingFilterByUrlPattern() {
        specification = FilterByUrlPattern("https://emarsys.com/%")
        val timestampProvider = TimestampProvider()
        val uuidProvider = UUIDProvider()

        val context = InstrumentationRegistry.getTargetContext().applicationContext
        val coreDbHelper = CoreDbHelper(context, mapOf())
        val repository = RequestModelRepository(coreDbHelper)

        val expectedRequestModel = RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/1").build()
        val requestModel1 = RequestModel.Builder(timestampProvider, uuidProvider).url("https://google.com/2").build()
        val expectedRequestModel2 = RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/3").build()

        repository.add(expectedRequestModel)
        repository.add(requestModel1)
        repository.add(expectedRequestModel2)

        val resultList = repository.query(specification)

        resultList.size shouldBe 2
        resultList.shouldContainAll(expectedRequestModel, expectedRequestModel2)
    }
}