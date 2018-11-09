package com.emarsys.core.database.repository.specification

import android.support.test.InstrumentationRegistry
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.RequestModelRepository
import com.emarsys.core.request.model.specification.FilterByRequestId
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

class FilterByRequestIdTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.timeoutRule

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

        requestModel1 = RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/1").build()
        requestModel2 = RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/2").build()
        requestModel3 = RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/3").build()
        requestModel4 = RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/4").build()

        val coreDbHelper = CoreDbHelper(InstrumentationRegistry.getTargetContext(), HashMap())
        repository = RequestModelRepository(coreDbHelper)

        repository.add(requestModel3)
        repository.add(requestModel4)
        repository.add(requestModel1)
        repository.add(requestModel2)
    }

    @Test
    fun testExecution_withRequestModel() {
        val expected = Arrays.asList<RequestModel>(requestModel1, requestModel3, requestModel4)

        repository.remove(FilterByRequestId(requestModel2))

        val actual = repository.query(QueryAll(DatabaseContract.REQUEST_TABLE_NAME))

        actual shouldBe expected
    }

    @Test
    fun testExecution_withCompositeRequestModel() {
        val expected = Arrays.asList(requestModel2)

        val originalRequestIds = arrayOf(requestModel1.id, requestModel3.id, requestModel4.id)

        val composite = CompositeRequestModel(
                "https://emarsys.com",
                RequestMethod.POST,
                null,
                HashMap(),
                System.currentTimeMillis(),
                10000,
                originalRequestIds)

        repository.remove(FilterByRequestId(composite))

        val actual = repository.query(QueryAll(DatabaseContract.REQUEST_TABLE_NAME))

        actual shouldBe expected
    }

}