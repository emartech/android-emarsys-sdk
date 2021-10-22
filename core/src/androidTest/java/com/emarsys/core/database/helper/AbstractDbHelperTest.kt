package com.emarsys.core.database.helper

import android.content.Context
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext

import android.database.sqlite.SQLiteDatabase
import com.emarsys.core.database.trigger.TriggerKey
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.lang.IllegalArgumentException
import java.util.HashMap

class AbstractDbHelperTest {
    private class DummyDbHelper(
            context: Context,
            databaseName: String,
            databaseVersion: Int,
            triggerMap: MutableMap<TriggerKey, MutableList<Runnable>>) : AbstractDbHelper(context, databaseName, databaseVersion, triggerMap) {
        override fun onCreate(db: SQLiteDatabase) {}
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }

    private lateinit var context: Context
    private lateinit var dbHelper: AbstractDbHelper
    private lateinit var triggerMap: MutableMap<TriggerKey, MutableList<Runnable>>

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Before
    fun init() {
        context = getTargetContext()
        triggerMap = mutableMapOf()
        dbHelper = DummyDbHelper(
                context,
                "name",
                1,
                triggerMap
        )
    }

    @Test
    fun testGetReadableCoreDatabase_returnsWrappedDatabase() {
        val db = dbHelper.readableCoreDatabase
        val expected = dbHelper.readableDatabase
        val result = db.backingDatabase
        Assert.assertEquals(expected, result)
    }

    @Test
    fun testGetWritableCoreDatabase_returnsWrappedDatabase() {
        val db = dbHelper.writableCoreDatabase
        val expected = dbHelper.writableDatabase
        val result = db.backingDatabase
        Assert.assertEquals(expected, result)
    }
}