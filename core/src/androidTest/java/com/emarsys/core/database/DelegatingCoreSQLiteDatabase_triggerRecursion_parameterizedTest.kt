package com.emarsys.core.database

import android.content.ContentValues
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerType
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.Mockito.mock

class DelegatingCoreSQLiteDatabase_triggerRecursion_parameterizedTest {

    private lateinit var mockRunnable: Runnable
    private lateinit var db: DelegatingCoreSQLiteDatabase

    @BeforeEach
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()

        val coreDbHelper = CoreDbHelper(
            InstrumentationRegistry.getTargetContext().applicationContext,
            mutableMapOf()
        )
        db = DelegatingCoreSQLiteDatabase(coreDbHelper.writableDatabase, mutableMapOf())

        db.backingDatabase.execSQL(CREATE)

        mockRunnable = mock(Runnable::class.java)
    }

    @ParameterizedTest
    @MethodSource("data")
    fun testRegisterTrigger_doesNotRunInto_recursiveTriggerLoop(
        triggerType: TriggerType,
        triggerEvent: TriggerEvent,
        triggerAction: Runnable
    ) {
        val trigger = Runnable {
            triggerAction.run()
            mockRunnable.run()
        }

        db.registerTrigger(TABLE_NAME, triggerType, triggerEvent, trigger)

        triggerAction.run()

        Mockito.verify(mockRunnable).run()
    }

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

    fun data(): Collection<Array<Any>> {
        return listOf(
            arrayOf(
                TriggerType.BEFORE,
                TriggerEvent.INSERT,
                Runnable {
                    db.insert(TABLE_NAME, null, contentValues)
                }),
            arrayOf(
                TriggerType.AFTER,
                TriggerEvent.INSERT,
                Runnable {
                    db.insert(TABLE_NAME, null, contentValues)
                }),
            arrayOf(
                TriggerType.BEFORE,
                TriggerEvent.DELETE,
                Runnable {
                    db.delete(TABLE_NAME, null, null)
                }),
            arrayOf(
                TriggerType.AFTER,
                TriggerEvent.DELETE,
                Runnable {
                    db.delete(TABLE_NAME, null, null)
                }),
            arrayOf(
                TriggerType.BEFORE,
                TriggerEvent.UPDATE,
                Runnable {
                    db.update(TABLE_NAME, contentValues, null, null)
                }),
            arrayOf(
                TriggerType.AFTER,
                TriggerEvent.UPDATE,
                Runnable {
                    db.update(TABLE_NAME, contentValues, null, null)
                })
        )
    }
}