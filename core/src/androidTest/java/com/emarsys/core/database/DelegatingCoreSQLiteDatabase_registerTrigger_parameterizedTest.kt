package com.emarsys.core.database

import android.content.ContentValues
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerKey
import com.emarsys.core.database.trigger.TriggerType
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class DelegatingCoreSQLiteDatabase_registerTrigger_parameterizedTest {

    private lateinit var registeredTriggerMap: MutableMap<TriggerKey, MutableList<Runnable>>

    @BeforeEach
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()

        val coreDbHelper = CoreDbHelper(
            InstrumentationRegistry.getTargetContext().applicationContext,
            mutableMapOf()
        )
        registeredTriggerMap = mutableMapOf()
        db = DelegatingCoreSQLiteDatabase(coreDbHelper.writableDatabase, registeredTriggerMap)

        db.backingDatabase.execSQL(CREATE)

        mockRunnable = mock()
    }

    @ParameterizedTest
    @MethodSource("data")
    fun testTrigger(
        tableName: String,
        triggerType: TriggerType,
        triggerEvent: TriggerEvent,
        setup: Runnable,
        trigger: Runnable,
        action: Runnable
    ) {
        setup.run()

        val unusedTriggerMap = createUnusedTriggerMap(tableName, triggerEvent)
        unusedTriggerMap.forEach { (key, trigger) ->
            db.registerTrigger(key.tableName, key.triggerType, key.triggerEvent, trigger)
        }
        db.registerTrigger(tableName, triggerType, triggerEvent, trigger)

        action.run()

        unusedTriggerMap.values.forEach { verifyNoInteractions(it) }
        verify(mockRunnable).run()
    }

    private fun createUnusedTriggerMap(
        tableName: String,
        triggerEvent: TriggerEvent
    ): Map<TriggerKey, Runnable> {
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
                    put(it, mock())
                }
            }
        }
    }

    companion object {

        private const val TABLE_NAME = "TEST"
        private const val COLUMN_1 = "column1"
        private const val COLUMN_2 = "column2"
        const val CREATE = "CREATE TABLE $TABLE_NAME ($COLUMN_1 TEXT, $COLUMN_2 INTEGER);"

        private lateinit var mockRunnable: Runnable
        private lateinit var db: DelegatingCoreSQLiteDatabase

        private val contentValues = ContentValues().apply {
            put(COLUMN_1, "value")
            put(COLUMN_2, 1234)
        }

        @JvmStatic
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
                            it.count shouldBe 0
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
                            it.count shouldBe 1
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
                            it.count shouldBe 1
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
                            it.count shouldBe 0
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