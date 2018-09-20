package com.emarsys.core.database

import android.support.test.InstrumentationRegistry
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerKey
import com.emarsys.core.database.trigger.TriggerType
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class DelegatingCoreSQLiteDatabaseTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.timeoutRule

    lateinit var db: DelegatingCoreSQLiteDatabase
    lateinit var triggerMap: MutableMap<TriggerKey, List<Runnable>>

    @Before
    fun init() {
        val coreDbHelper = CoreDbHelper(InstrumentationRegistry.getTargetContext().applicationContext, mutableMapOf())

        triggerMap = mutableMapOf()

        db = DelegatingCoreSQLiteDatabase(coreDbHelper.writableDatabase, triggerMap)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_backingDatabase_mustNotBeNull() {
        DelegatingCoreSQLiteDatabase(null, emptyMap())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_triggerMap_mustNotBeNull() {
        DelegatingCoreSQLiteDatabase(db.backingDatabase, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRegisterTrigger_table_mustNotBeNull() {
        db.registerTrigger(null, TriggerType.AFTER, TriggerEvent.INSERT) {}
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRegisterTrigger_triggerType_mustNotBeNull() {
        db.registerTrigger("table", null, TriggerEvent.INSERT) {}
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRegisterTrigger_triggerEvent_mustNotBeNull() {
        db.registerTrigger("table", TriggerType.AFTER, null) {}
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRegisterTrigger_trigger_mustNotBeNull() {
        db.registerTrigger("table", TriggerType.AFTER, TriggerEvent.INSERT, null)
    }

    @Test
    fun testRegisterTrigger_shouldRegisterMultipleTriggers() {
        val triggerKey = TriggerKey("table", TriggerType.AFTER, TriggerEvent.DELETE)
        val count = 3

        repeat(count) {
            db.registerTrigger(triggerKey.tableName, triggerKey.triggerType, triggerKey.triggerEvent, mock(Runnable::class.java))
        }

        Assert.assertEquals(count, triggerMap[triggerKey]?.size)
    }
}