package com.emarsys.core.database

import android.content.ContentValues
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerKey
import com.emarsys.core.database.trigger.TriggerType
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class DelegatingCoreSQLiteDatabase_registerTrigger_parameterizedTest : AnnotationSpec() {

    companion object {

        private const val TABLE_NAME = "TEST"
        private const val COLUMN_1 = "column1"
        private const val COLUMN_2 = "column2"
        const val CREATE = "CREATE TABLE $TABLE_NAME ($COLUMN_1 TEXT, $COLUMN_2 INTEGER);"
        const val DROP = "DROP TABLE $TABLE_NAME IF EXISTS;"

        private lateinit var mockRunnable: Runnable
        private lateinit var db: DelegatingCoreSQLiteDatabase

        private val contentValues = ContentValues().apply {
            put(COLUMN_1, "value")
            put(COLUMN_2, 1234)
        }
    }

    private lateinit var registeredTriggerMap: MutableMap<TriggerKey, MutableList<Runnable>>

    @Before
    fun setUp() {
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

    @Test
    fun testTrigger() {
        table(
            headers("tableName", "triggerType", "triggerEvent", "setup", "trigger", "action"),
            row(
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
            row(
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
            row(
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
            row(
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
        ).forAll { tableName, triggerType, triggerEvent, setup, trigger, action ->
            setUp()
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

}