package com.emarsys.mobileengage.iam.model.buttonclicked

import android.content.ContentValues
import android.database.Cursor
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.helper.DbHelper
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.testUtil.DatabaseTestUtils.deleteCoreDatabase
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.Date

class ButtonClickedRepositoryTest  {
    private lateinit var repository: ButtonClickedRepository
    private lateinit var buttonClicked1: ButtonClicked
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder

    @Before
    fun setUp() {
        deleteCoreDatabase()
        val context = getTargetContext()
        val dbHelper: DbHelper = CoreDbHelper(context, HashMap())
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        repository = ButtonClickedRepository(dbHelper, concurrentHandlerHolder)
        buttonClicked1 = ButtonClicked("campaign1", "button1", Date().time)
    }

    @Test
    fun testContentValuesFromItem() {
        val expected = ContentValues()
        expected.put(
            DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_CAMPAIGN_ID,
            buttonClicked1.campaignId
        )
        expected.put(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_BUTTON_ID, buttonClicked1.buttonId)
        expected.put(
            DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_TIMESTAMP,
            buttonClicked1.timestamp
        )
        val result = repository.contentValuesFromItem(buttonClicked1)

        result shouldBe expected
    }

    @Test
    fun testItemFromCursor() {
        val cursor: Cursor = mock()
        whenever(cursor.getColumnIndexOrThrow(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_CAMPAIGN_ID)).thenReturn(
            0
        )
        whenever(cursor.getString(0)).thenReturn(buttonClicked1.campaignId)
        whenever(cursor.getColumnIndexOrThrow(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_BUTTON_ID)).thenReturn(
            1
        )
        whenever(cursor.getString(1)).thenReturn(buttonClicked1.buttonId)
        whenever(cursor.getColumnIndexOrThrow(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_TIMESTAMP)).thenReturn(
            2
        )
        whenever(cursor.getLong(2)).thenReturn(buttonClicked1.timestamp)

        val result = repository.itemFromCursor(cursor)
        val expected = buttonClicked1

        result shouldBe expected
    }
}