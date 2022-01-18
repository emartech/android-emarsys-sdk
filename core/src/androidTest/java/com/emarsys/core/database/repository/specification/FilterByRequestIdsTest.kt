package com.emarsys.core.database.repository.specification

import android.os.Handler
import android.os.Looper
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.RequestModelRepository
import com.emarsys.core.request.model.specification.FilterByRequestIds
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.*

class FilterByRequestIdsTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var repository: RequestModelRepository
    private lateinit var requestModel1: RequestModel
    private lateinit var requestModel2: RequestModel
    private lateinit var requestModel3: RequestModel
    private lateinit var requestModel4: RequestModel

    @Before
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()
        val timestampProvider = TimestampProvider()
        val uuidProvider = UUIDProvider()

        requestModel1 =
            RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/1")
                .build()
        requestModel2 =
            RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/2")
                .build()
        requestModel3 =
            RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/3")
                .build()
        requestModel4 =
            RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/4")
                .build()

        val coreDbHelper = CoreDbHelper(InstrumentationRegistry.getTargetContext(), HashMap())
        val uiHandler = Handler(Looper.getMainLooper())
        val concurrentHandlerHolder = ConcurrentHandlerHolderFactory(uiHandler).create()
        repository = RequestModelRepository(coreDbHelper, concurrentHandlerHolder)
        runBlocking {
            repository.add(requestModel1)
            repository.add(requestModel2)
            repository.add(requestModel3)
            repository.add(requestModel4)
        }
    }

    @Test
    fun testDelete_withRequestModel() {
        val expected = arrayOf(requestModel1, requestModel3, requestModel4)
        runBlocking {
            repository.remove(FilterByRequestIds(arrayOf(requestModel2.id)))
        }

        val actual = repository.query(Everything())

        actual shouldBe expected
    }

    @Test
    fun testDelete_withCompositeRequestModel() {
        val expected = arrayOf(requestModel2)

        val originalRequestIds = arrayOf(requestModel1.id, requestModel3.id, requestModel4.id)
        runBlocking {
            repository.remove(FilterByRequestIds(originalRequestIds))
        }

        val actual = repository.query(Everything())

        actual shouldBe expected
    }

    @Test
    fun testQuery_withRequestModel() {
        val expected = arrayOf(requestModel2)

        val result = repository.query(FilterByRequestIds(arrayOf(requestModel2.id)))

        result shouldBe expected
    }

}