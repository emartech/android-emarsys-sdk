package com.emarsys.mobileengage.iam.model.buttonclicked

import android.content.ContentValues
import android.database.Cursor
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.helper.DbHelper
import com.emarsys.testUtil.DatabaseTestUtils.deleteCoreDatabase
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito
import java.util.*

class ButtonClickedRepositoryTest {
    companion object {
        init {
            Mockito.mock(Cursor::class.java)
        }
    }

    private lateinit var repository: ButtonClickedRepository
    private lateinit var buttonClicked1: ButtonClicked

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun init() {
        deleteCoreDatabase()
        val context = getTargetContext()
        val dbHelper: DbHelper = CoreDbHelper(context, HashMap())
        repository = ButtonClickedRepository(dbHelper)
        buttonClicked1 = ButtonClicked("campaign1", "button1", Date().time)
    }

    @Test
    fun testContentValuesFromItem() {
        val expected = ContentValues()
        expected.put(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_CAMPAIGN_ID, buttonClicked1.campaignId)
        expected.put(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_BUTTON_ID, buttonClicked1.buttonId)
        expected.put(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_TIMESTAMP, buttonClicked1.timestamp)
        val result = repository.contentValuesFromItem(buttonClicked1)

        result shouldBe expected
    }

    @Test
    fun testItemFromCursor() {
        val cursor = Mockito.mock(Cursor::class.java)
        Mockito.`when`(cursor.getColumnIndex(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_CAMPAIGN_ID)).thenReturn(0)
        Mockito.`when`(cursor.getString(0)).thenReturn(buttonClicked1.campaignId)
        Mockito.`when`(cursor.getColumnIndex(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_BUTTON_ID)).thenReturn(1)
        Mockito.`when`(cursor.getString(1)).thenReturn(buttonClicked1.buttonId)
        Mockito.`when`(cursor.getColumnIndex(DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_TIMESTAMP)).thenReturn(2)
        Mockito.`when`(cursor.getLong(2)).thenReturn(buttonClicked1.timestamp)
        val result = repository.itemFromCursor(cursor)
        val expected = buttonClicked1

        result shouldBe expected
    }
}