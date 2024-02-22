package com.emarsys.core.request.model.specification

import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.repository.specification.Everything
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.RequestModelRepository
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test


class FilterByUrlPatternTest {


    private lateinit var specification: FilterByUrlPattern
    private lateinit var pattern: String
    private lateinit var repository: RequestModelRepository
    private lateinit var timestampProvider: TimestampProvider
    private lateinit var uuidProvider: UUIDProvider

    @BeforeEach
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()
        pattern = "root/___/_%/event"
        specification = FilterByUrlPattern(pattern)
        timestampProvider = TimestampProvider()
        uuidProvider = UUIDProvider()

        val context = InstrumentationRegistry.getTargetContext().applicationContext
        val coreDbHelper = CoreDbHelper(context, mutableMapOf())
        val concurrentHandlerHolder: ConcurrentHandlerHolder =
            ConcurrentHandlerHolderFactory.create()
        repository = RequestModelRepository(coreDbHelper, concurrentHandlerHolder)
    }

    @Test
    fun testSpecification() {
        with(FilterByUrlPattern(pattern)) {
            isDistinct shouldBe false
            columns shouldBe null
            selection shouldBe "url LIKE ?"
            selectionArgs shouldBe arrayOf(pattern)
            groupBy shouldBe null
            having shouldBe null
            orderBy shouldBe null
            limit shouldBe null
        }
    }

    @Test
    fun testConstructor_patternMustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            FilterByUrlPattern(null)
        }
    }

    @Test
    fun testQueryUsingFilterByUrlPattern() {
        specification = FilterByUrlPattern("https://emarsys.com/%")

        val expectedRequestModel =
            RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/1")
                .build()
        val requestModel1 =
            RequestModel.Builder(timestampProvider, uuidProvider).url("https://google.com/2")
                .build()
        val expectedRequestModel2 =
            RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/3")
                .build()

        runBlocking {
            repository.add(expectedRequestModel)
            repository.add(requestModel1)
            repository.add(expectedRequestModel2)
        }
        val resultList = repository.query(specification)

        resultList.size shouldBe 2
        resultList.shouldContainAll(expectedRequestModel, expectedRequestModel2)
    }

    @Test
    fun testDeleteUsingFilterByUrlPattern() {
        specification = FilterByUrlPattern("https://emarsys.com/%")

        val requestModel1 =
            RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/1")
                .build()
        val expectedRequestModel =
            RequestModel.Builder(timestampProvider, uuidProvider).url("https://google.com/2")
                .build()
        val requestModel2 =
            RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/3")
                .build()
        runBlocking {
            repository.add(requestModel1)
            repository.add(expectedRequestModel)
            repository.add(requestModel2)
            repository.remove(specification)
        }
        val resultList = repository.query(Everything())

        resultList.size shouldBe 1
        resultList shouldBe listOf(expectedRequestModel)
    }
}