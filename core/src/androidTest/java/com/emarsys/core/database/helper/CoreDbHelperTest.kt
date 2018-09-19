package com.emarsys.core.database.helper;

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.support.test.InstrumentationRegistry
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.TimeoutUtils
import junit.framework.Assert

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CoreDbHelperTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.timeoutRule

    lateinit var dbHelper: CoreDbHelper
    lateinit var db: SQLiteDatabase

    @Before
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()
        dbHelper = CoreDbHelper(
                InstrumentationRegistry.getTargetContext().applicationContext,
                mutableMapOf())
        db = dbHelper.writableCoreDatabase.backingDatabase
        DatabaseTestUtils.dropAllTables(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testDbShouldHaveNoTables_atTheStartOfTheTest() {
        val cursor = dbHelper.readableCoreDatabase.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table'", null)
        cursor.use {
            it.moveToFirst()
            Assert.assertEquals(0, it.getInt(0))
        }
    }

    @Test
    fun testOnUpgrade_from_0_to_1() {
        dbHelper.onUpgrade(db, 0, 1)

        val expectedColumns = setOf(
                ColumnInfo("request_id", "TEXT"),
                ColumnInfo("method", "TEXT"),
                ColumnInfo("url", "TEXT"),
                ColumnInfo("headers", "BLOB"),
                ColumnInfo("payload", "BLOB"),
                ColumnInfo("timestamp", "INTEGER")
        )

        val actualColumns = getTableColumns(db, "request")
        Assert.assertEquals(expectedColumns, actualColumns)
    }

    @Test
    fun testOnUpgrade_from_1_to_2() {
        initializeDatabaseWithVersion(1)

        dbHelper.onUpgrade(db, 1, 2)

        val expectedColumns = setOf(
                ColumnInfo("request_id", "TEXT"),
                ColumnInfo("method", "TEXT"),
                ColumnInfo("url", "TEXT"),
                ColumnInfo("headers", "BLOB"),
                ColumnInfo("payload", "BLOB"),
                ColumnInfo("timestamp", "INTEGER"),
                ColumnInfo("ttl", "INTEGER", defaultValue = Long.MAX_VALUE.toString())
        )

        val actualColumns = getTableColumns(db, "request")

        Assert.assertEquals(expectedColumns, actualColumns)
    }

    @Test
    fun testOnUpgrade_from_2_to_3() {
        initializeDatabaseWithVersion(2)

        dbHelper.onUpgrade(db, 2, 3)

        val expectedRequestColumns = setOf(
                ColumnInfo("request_id", "TEXT"),
                ColumnInfo("method", "TEXT"),
                ColumnInfo("url", "TEXT"),
                ColumnInfo("headers", "BLOB"),
                ColumnInfo("payload", "BLOB"),
                ColumnInfo("timestamp", "INTEGER"),
                ColumnInfo("ttl", "INTEGER", defaultValue = Long.MAX_VALUE.toString())
        )

        val expectedShardColumns = setOf(
                ColumnInfo("type", "TEXT"),
                ColumnInfo("shard_id", "TEXT"),
                ColumnInfo("data", "BLOB"),
                ColumnInfo("timestamp", "INTEGER"),
                ColumnInfo("ttl", "INTEGER")
        )
        val actualRequestColumns = getTableColumns(db, "request")
        val actualShardColumns = getTableColumns(db, "shard")

        Assert.assertEquals(expectedRequestColumns, actualRequestColumns)
        Assert.assertEquals(expectedShardColumns, actualShardColumns)
    }

    @Test
    fun testOnCreate_shouldNotAddIndexesToRequestTable_V3() {
        initializeDatabaseWithVersion(3)

        val indexedColumns = getIndexedColumnsOnTable(db, "request")

        Assert.assertEquals(0, indexedColumns.size)
    }

    @Test
    fun testOnCreate_createsIndexOnShardId_and_Type_database_V3() {
        initializeDatabaseWithVersion(3)

        val indexedColumns = getIndexedColumnsOnTable(db, "shard")
        Assert.assertEquals(2, indexedColumns.size)
        Assert.assertTrue(indexedColumns.contains("shard_id"))
        Assert.assertTrue(indexedColumns.contains("type"))
    }

    @Test
    fun testOnCreate_withDbVersion_V3() {
        initializeDatabaseWithVersion(3)

        val expectedRequestColumns = getTableColumns(db, "request")
        val expectedShardColumns = getTableColumns(db, "shard")

        DatabaseTestUtils.dropAllTables(db)
        dbHelper.onCreate(db)

        val actualRequestColumns = getTableColumns(db, "request")
        val actualShardColumns = getTableColumns(db, "shard")

        Assert.assertEquals(expectedRequestColumns, actualRequestColumns)
        Assert.assertEquals(expectedShardColumns, actualShardColumns)
    }

    private fun getTableColumns(db: SQLiteDatabase, tableName: String): Set<ColumnInfo> {
        return db.rawQuery("PRAGMA table_info($tableName);", null).use {
            val result = mutableSetOf<ColumnInfo>()
            it.moveToFirst()
            while (!it.isAfterLast) {
                val columnName = it.getString(it.getColumnIndex("name"))
                val columnType = it.getString(it.getColumnIndex("type"))
                val primaryKey = it.getInt(it.getColumnIndex("pk")) == 1
                val notNull = it.getInt(it.getColumnIndex("notnull")) == 1
                val defaultValue = it.getString(it.getColumnIndex("dflt_value"))
                result.add(ColumnInfo(columnName, columnType, defaultValue, primaryKey, notNull))
                it.moveToNext()
            }
            result
        }
    }

    private fun getIndexedColumnsOnTable(db: SQLiteDatabase, tableName: String): Set<String> {
        return db.rawQuery("PRAGMA index_list($tableName)", null).use {
            val result = mutableSetOf<String>()
            it.moveToFirst()
            while (!it.isAfterLast) {
                val indexName = it.getString(it.getColumnIndex("name"))
                db.rawQuery("PRAGMA index_info('$indexName');", null).use { indexInfoCursor: Cursor ->
                    indexInfoCursor.moveToFirst()
                    while (!indexInfoCursor.isAfterLast) {
                        result.add(indexInfoCursor.getString(indexInfoCursor.getColumnIndex("name")))
                        indexInfoCursor.moveToNext()
                    }
                }
                it.moveToNext()
            }
            result
        }
    }

    private fun initializeDatabaseWithVersion(version: Int) {
        dbHelper.onUpgrade(db, 0, version)
    }

    data class ColumnInfo(val columnName: String, val columnType: String, val defaultValue: String? = null, val primaryKey: Boolean = false, val notNull: Boolean = false)
}

