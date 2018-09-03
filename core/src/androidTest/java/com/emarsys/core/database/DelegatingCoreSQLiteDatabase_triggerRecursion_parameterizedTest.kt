package com.emarsys.core.database

import android.content.ContentValues
import android.support.test.InstrumentationRegistry
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerType
import com.emarsys.test.util.DatabaseTestUtils
import com.emarsys.test.util.TimeoutUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito
import org.mockito.Mockito.mock

@RunWith(Parameterized::class)
class DelegatingCoreSQLiteDatabase_triggerRecursion_parameterizedTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.timeoutRule

    @Parameterized.Parameter
    lateinit var triggerType: TriggerType

    @Parameterized.Parameter(1)
    lateinit var triggerEvent: TriggerEvent

    @Parameterized.Parameter(2)
    lateinit var triggerAction: Runnable

    @Before
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()

        val coreDbHelper = CoreDbHelper(InstrumentationRegistry.getTargetContext().applicationContext, mutableMapOf())
        db = DelegatingCoreSQLiteDatabase(coreDbHelper.writableDatabase, mutableMapOf())

        db.backingDatabase.execSQL(CREATE)

        mockRunnable = mock(Runnable::class.java)
    }

    @Test
    fun testRegisterTrigger_doesNotRunInto_recursiveTriggerLoop() {
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

        lateinit var mockRunnable: Runnable
        lateinit var db: DelegatingCoreSQLiteDatabase

        private val contentValues = ContentValues().apply {
            put(COLUMN_1, "value")
            put(COLUMN_2, 1234)
        }

        @JvmStatic
        @Parameterized.Parameters
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
                            })
            )
        }
    }
}
