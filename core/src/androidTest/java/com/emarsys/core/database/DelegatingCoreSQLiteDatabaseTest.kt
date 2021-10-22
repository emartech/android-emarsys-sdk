package com.emarsys.core.database

import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerKey
import com.emarsys.core.database.trigger.TriggerType
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class DelegatingCoreSQLiteDatabaseTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var db: DelegatingCoreSQLiteDatabase
    private lateinit var triggerMap: MutableMap<TriggerKey, MutableList<Runnable>>

    @Before
    fun init() {
        val coreDbHelper = CoreDbHelper(InstrumentationRegistry.getTargetContext().applicationContext, mutableMapOf())

        triggerMap = mutableMapOf()

        db = DelegatingCoreSQLiteDatabase(coreDbHelper.writableDatabase, triggerMap)
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