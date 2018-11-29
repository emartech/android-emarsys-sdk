package com.emarsys.core.database

import android.content.ContentValues
import androidx.test.InstrumentationRegistry
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerKey
import com.emarsys.core.database.trigger.TriggerType
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito.*

@RunWith(Parameterized::class)
class DelegatingCoreSQLiteDatabase_registerTrigger_parameterizedTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.timeoutRule

    @Parameterized.Parameter
    lateinit var tableName: String

    @Parameterized.Parameter(1)
    lateinit var triggerType: TriggerType

    @Parameterized.Parameter(2)
    lateinit var triggerEvent: TriggerEvent

    @Parameterized.Parameter(3)
    lateinit var setup: Runnable

    @Parameterized.Parameter(4)
    lateinit var trigger: Runnable

    @Parameterized.Parameter(5)
    lateinit var action: Runnable

    lateinit var registeredTriggerMap: MutableMap<TriggerKey, MutableList<Runnable>>

    @Before
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()

        val coreDbHelper = CoreDbHelper(InstrumentationRegistry.getTargetContext().applicationContext, mutableMapOf())
        registeredTriggerMap = mutableMapOf()
        db = DelegatingCoreSQLiteDatabase(coreDbHelper.writableDatabase, registeredTriggerMap)

        db.backingDatabase.execSQL(CREATE)

        mockRunnable = mock(Runnable::class.java)
    }

    @Test
    fun testTrigger() {
        setup.run()

        val unusedTriggerMap = createUnusedTriggerMap()
        unusedTriggerMap.forEach { (key, trigger) ->
            db.registerTrigger(key.tableName, key.triggerType, key.triggerEvent, trigger)
        }
        db.registerTrigger(tableName, triggerType, triggerEvent, trigger)

        action.run()

        unusedTriggerMap.values.forEach { verifyZeroInteractions(it) }
        verify(mockRunnable).run()
    }

    private fun createUnusedTriggerMap(): Map<TriggerKey, Runnable> {
        return TriggerType.values().flatMap { type ->
            TriggerEvent.values().map { event ->
                type to event
            }
        }.map { (type, event) ->
            TriggerKey(tableName, type, event)
        }.filter {
            it.triggerEvent != triggerEvent
        }.let { triggerKeys ->
            HashMap<TriggerKey, Runnable>().apply {
                triggerKeys.forEach {
                    put(it, mock(Runnable::class.java))
                }
            }
        }
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
                            TABLE_NAME,
                            TriggerType.BEFORE,
                            TriggerEvent.INSERT,
                            Runnable {

                            },
                            Runnable {
                                db.backingDatabase.rawQuery("SELECT * FROM $TABLE_NAME", emptyArray()).let {
                                    assertEquals(0, it.count)
                                }
                                mockRunnable.run()
                            },
                            Runnable {
                                db.insert(TABLE_NAME, null, contentValues)
                            }),
                    arrayOf(
                            TABLE_NAME,
                            TriggerType.AFTER,
                            TriggerEvent.INSERT,
                            Runnable {

                            },
                            Runnable {
                                db.backingDatabase.rawQuery("SELECT * FROM $TABLE_NAME", emptyArray()).let {
                                    assertEquals(1, it.count)
                                }
                                mockRunnable.run()
                            },
                            Runnable {
                                db.insert(TABLE_NAME, null, contentValues)
                            }),
                    arrayOf(
                            TABLE_NAME,
                            TriggerType.BEFORE,
                            TriggerEvent.DELETE,
                            Runnable {
                                db.insert(TABLE_NAME, null, contentValues)
                            },
                            Runnable {
                                db.backingDatabase.rawQuery("SELECT * FROM $TABLE_NAME", emptyArray()).let {
                                    assertEquals(1, it.count)
                                }
                                mockRunnable.run()
                            },
                            Runnable {
                                db.delete(TABLE_NAME, null, null)
                            }),
                    arrayOf(
                            TABLE_NAME,
                            TriggerType.AFTER,
                            TriggerEvent.DELETE,
                            Runnable {
                                db.insert(TABLE_NAME, null, contentValues)
                            },
                            Runnable {
                                db.backingDatabase.rawQuery("SELECT * FROM $TABLE_NAME", emptyArray()).let {
                                    assertEquals(0, it.count)
                                }
                                mockRunnable.run()
                            },
                            Runnable {
                                db.delete(TABLE_NAME, null, null)
                            })
            )
        }
    }
}
