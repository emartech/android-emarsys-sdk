package com.emarsys.core.database

import android.content.ContentValues
import android.support.test.InstrumentationRegistry
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerType
import com.emarsys.core.testUtil.DatabaseTestUtils
import com.emarsys.core.testUtil.TimeoutUtils
import com.nhaarman.mockito_kotlin.verify
import junit.framework.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito.mock

@RunWith(Parameterized::class)
class DelegatingCoreSQLiteDatabase_registerTrigger_parameterizedTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.getTimeoutRule()

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

    @Before
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()

        val coreDbHelper = CoreDbHelper(InstrumentationRegistry.getTargetContext().applicationContext, mutableMapOf())
        db = DelegatingCoreSQLiteDatabase(coreDbHelper.writableDatabase, mutableMapOf())

        db.backingDatabase.execSQL(CREATE)

        mockRunnable = mock(Runnable::class.java)
    }

    @Test
    fun testTrigger() {
        setup.run()

        db.registerTrigger(tableName, triggerType, triggerEvent, trigger)

        action.run()

        verify(mockRunnable).run()
    }

    companion object {

        private const val TABLE_NAME = "TEST"
        private const val COLUMN_1 = "column1"
        private const val COLUMN_2 = "column2"
        const val CREATE = "CREATE TABLE $TABLE_NAME ($COLUMN_1 TEXT, $COLUMN_2 INTEGER);"

        lateinit var mockRunnable: Runnable
        lateinit var db: DelegatingCoreSQLiteDatabase

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
                                    Assert.assertEquals(0, it.count)
                                }
                                mockRunnable.run()
                            },
                            Runnable {
                                ContentValues().apply {
                                    put(COLUMN_1, "value")
                                    put(COLUMN_2, 1234)
                                }.let {
                                    db.insert(TABLE_NAME, null, it)
                                }
                            }),
                    arrayOf(
                            TABLE_NAME,
                            TriggerType.AFTER,
                            TriggerEvent.INSERT,
                            Runnable {

                            },
                            Runnable {
                                db.backingDatabase.rawQuery("SELECT * FROM $TABLE_NAME", emptyArray()).let {
                                    Assert.assertEquals(1, it.count)
                                }
                                mockRunnable.run()
                            },
                            Runnable {
                                ContentValues().apply {
                                    put(COLUMN_1, "value")
                                    put(COLUMN_2, 1234)
                                }.let {
                                    db.insert(TABLE_NAME, null, it)
                                }
                            }),
                    arrayOf(
                            TABLE_NAME,
                            TriggerType.BEFORE,
                            TriggerEvent.DELETE,
                            Runnable {
                                ContentValues().apply {
                                    put(COLUMN_1, "value")
                                    put(COLUMN_2, 1234)
                                }.let {
                                    db.insert(TABLE_NAME, null, it)
                                }
                            },
                            Runnable {
                                db.backingDatabase.rawQuery("SELECT * FROM $TABLE_NAME", emptyArray()).let {
                                    Assert.assertEquals(1, it.count)
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
                                ContentValues().apply {
                                    put(COLUMN_1, "value")
                                    put(COLUMN_2, 1234)
                                }.let {
                                    db.insert(TABLE_NAME, null, it)
                                }
                            },
                            Runnable {
                                db.backingDatabase.rawQuery("SELECT * FROM $TABLE_NAME", emptyArray()).let {
                                    Assert.assertEquals(0, it.count)
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
