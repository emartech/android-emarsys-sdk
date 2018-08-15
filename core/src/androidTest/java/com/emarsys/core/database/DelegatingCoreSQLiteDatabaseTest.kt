package com.emarsys.core.database

import android.support.test.InstrumentationRegistry
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.trigger.TriggerEvent
import com.emarsys.core.database.trigger.TriggerType
import com.emarsys.core.testUtil.TimeoutUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DelegatingCoreSQLiteDatabaseTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.getTimeoutRule()

    lateinit var db: DelegatingCoreSQLiteDatabase

    @Before
    fun init() {
        val coreDbHelper = CoreDbHelper(InstrumentationRegistry.getTargetContext().applicationContext, mutableMapOf())
        db = DelegatingCoreSQLiteDatabase(coreDbHelper.writableDatabase, mutableMapOf())
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
}