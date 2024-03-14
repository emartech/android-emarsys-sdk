package com.emarsys.core.database

import android.content.ContentValues
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerType
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import io.kotest.data.forAll
import io.kotest.data.row
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class DelegatingCoreSQLiteDatabase_triggerRecursion_parameterizedTest : AnnotationSpec() {

    companion object {
        private const val TABLE_NAME = "TEST"
        private const val COLUMN_1 = "column1"
        private const val COLUMN_2 = "column2"
        const val CREATE = "CREATE TABLE $TABLE_NAME ($COLUMN_1 TEXT, $COLUMN_2 INTEGER);"

        private val contentValues = ContentValues().apply {
            put(COLUMN_1, "value")
            put(COLUMN_2, 1234)
        }
    }

    private lateinit var mockRunnable: Runnable
    private lateinit var db: DelegatingCoreSQLiteDatabase

    @Before
    fun setUp() {
        DatabaseTestUtils.deleteCoreDatabase()

        val coreDbHelper = CoreDbHelper(
            InstrumentationRegistry.getTargetContext().applicationContext,
            mutableMapOf()
        )
        db = DelegatingCoreSQLiteDatabase(coreDbHelper.writableDatabase, mutableMapOf())

        db.backingDatabase.execSQL(CREATE)

        mockRunnable = mock()
    }

    @Test
    fun testRegisterTrigger_doesNotRunInto_recursiveTriggerLoop() = runBlocking {
        forAll(
            row(
                TriggerType.BEFORE,
                TriggerEvent.INSERT,
                Runnable {
                    db.insert(TABLE_NAME, null, contentValues)
                }),
            row(
                TriggerType.AFTER,
                TriggerEvent.INSERT,
                Runnable {
                    db.insert(TABLE_NAME, null, contentValues)
                }),
            row(
                TriggerType.BEFORE,
                TriggerEvent.DELETE,
                Runnable {
                    db.delete(TABLE_NAME, null, null)
                }),
            row(
                TriggerType.AFTER,
                TriggerEvent.DELETE,
                Runnable {
                    db.delete(TABLE_NAME, null, null)
                }),
            row(
                TriggerType.BEFORE,
                TriggerEvent.UPDATE,
                Runnable {
                    db.update(TABLE_NAME, contentValues, null, null)
                }),
            row(
                TriggerType.AFTER,
                TriggerEvent.UPDATE,
                Runnable {
                    db.update(TABLE_NAME, contentValues, null, null)
                })
        ) { triggerType, triggerEvent, triggerAction ->
            setUp()
            val trigger = Runnable {
                triggerAction.run()
                mockRunnable.run()
            }

            db.registerTrigger(TABLE_NAME, triggerType, triggerEvent, trigger)

            triggerAction.run()

            verify(mockRunnable).run()
        }
    }
}