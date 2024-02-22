package com.emarsys.core.database

import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerKey
import com.emarsys.core.database.trigger.TriggerType
import com.emarsys.testUtil.InstrumentationRegistry
import io.kotest.matchers.shouldBe

import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

import org.mockito.Mockito.mock

class DelegatingCoreSQLiteDatabaseTest {



    private lateinit var db: DelegatingCoreSQLiteDatabase
    private lateinit var triggerMap: MutableMap<TriggerKey, MutableList<Runnable>>

    @BeforeEach
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

        triggerMap[triggerKey]?.size shouldBe count
    }
}