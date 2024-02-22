package com.emarsys.mobileengage.iam.model.displayediam

import android.content.ContentValues
import android.database.Cursor
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.DatabaseContract.DISPLAYED_IAM_COLUMN_NAME_CAMPAIGN_ID
import com.emarsys.core.database.DatabaseContract.DISPLAYED_IAM_COLUMN_NAME_TIMESTAMP
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.helper.DbHelper
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.testUtil.DatabaseTestUtils.deleteCoreDatabase
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class DisplayedIamRepositoryTest {

    private lateinit var iamRepository: DisplayedIamRepository
    private lateinit var displayedIam1: DisplayedIam
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder


    @BeforeEach
    fun setUp() {
        deleteCoreDatabase()
        val context = getTargetContext()
        val dbHelper: DbHelper = CoreDbHelper(context, mutableMapOf())
        concurrentHandlerHolder =
            ConcurrentHandlerHolderFactory.create()
        iamRepository = DisplayedIamRepository(dbHelper, concurrentHandlerHolder)
        displayedIam1 = DisplayedIam("campaign1", Date().time)
    }

    @Test
    fun testContentValuesFromItem() {
        val expected = ContentValues()
        expected.put(DISPLAYED_IAM_COLUMN_NAME_CAMPAIGN_ID, displayedIam1.campaignId)
        expected.put(DISPLAYED_IAM_COLUMN_NAME_TIMESTAMP, displayedIam1.timestamp)
        val result = iamRepository.contentValuesFromItem(displayedIam1)

        result shouldBe expected
    }

    @Test
    fun testItemFromCursor() {
        val mockCursor: Cursor = mock()
        whenever(mockCursor.getColumnIndexOrThrow(DISPLAYED_IAM_COLUMN_NAME_CAMPAIGN_ID))
            .thenReturn(0)
        whenever(mockCursor.getString(0)).thenReturn(displayedIam1.campaignId)
        whenever(mockCursor.getColumnIndexOrThrow(DISPLAYED_IAM_COLUMN_NAME_TIMESTAMP))
            .thenReturn(1)
        whenever(mockCursor.getLong(1)).thenReturn(displayedIam1.timestamp)
        val result = iamRepository.itemFromCursor(mockCursor)
        val expected = displayedIam1
        result shouldBe expected
    }
}