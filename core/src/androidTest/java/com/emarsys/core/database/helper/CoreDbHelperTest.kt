package com.emarsys.core.database.helper;

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class CoreDbHelperTest {

    companion object {
        const val LATEST_DB_VERSION = 5
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var dbHelper: CoreDbHelper
    private lateinit var db: SQLiteDatabase

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
    fun testDbVersion_shouldBeTheLatest() {
        LATEST_DB_VERSION shouldBe CoreDbHelper.DATABASE_VERSION
    }

    @Test
    fun testDbShouldHaveNoTables_atTheStartOfTheTest() {
        val cursor = dbHelper.readableCoreDatabase.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table'", null)
        cursor.use {
            it.moveToFirst()
            it.getInt(0) shouldBe 0
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

        actualColumns shouldBe expectedColumns
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

        actualColumns shouldBe expectedColumns
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

        actualRequestColumns shouldBe expectedRequestColumns
        actualShardColumns shouldBe expectedShardColumns
    }

    @Test
    fun testOnUpgrade_from_3_to_4() {
        initializeDatabaseWithVersion(3)

        dbHelper.onUpgrade(db, 3, 4)

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

        val expectedDisplayedIamColumns = setOf(
                ColumnInfo("campaign_id", "TEXT"),
                ColumnInfo("timestamp", "INTEGER")
        )

        val expectedButtonClickedColumns = setOf(
                ColumnInfo("campaign_id", "TEXT"),
                ColumnInfo("button_id", "TEXT"),
                ColumnInfo("timestamp", "INTEGER")
        )
        val actualRequestColumns = getTableColumns(db, "request")
        val actualShardColumns = getTableColumns(db, "shard")
        val actualDisplayedIamColumns = getTableColumns(db, "displayed_iam")
        val actualButtonClickedColumns = getTableColumns(db, "button_clicked")

        actualRequestColumns shouldBe expectedRequestColumns
        actualShardColumns shouldBe expectedShardColumns
        actualDisplayedIamColumns shouldBe expectedDisplayedIamColumns
        actualButtonClickedColumns shouldBe expectedButtonClickedColumns
    }

    @Test
    fun testOnUpgrade_from_4_to_5() {
        initializeDatabaseWithVersion(4)

        dbHelper.onUpgrade(db, 4, 5)

        val expectedColumns = setOf(
                ColumnInfo("hardware_id", "TEXT")
        )


        val actualColumns = getTableColumns(db, "hardware")

        actualColumns shouldBe expectedColumns
    }

    @Test
    fun testOnCreate_indices_withLatestVersion() {
        initializeDatabaseWithVersion(LATEST_DB_VERSION)

        "request".let {
            val indexedColumns = getIndexedColumnsOnTable(db, it)

            indexedColumns.size shouldBe 0
        }

        "shard".let {
            val indexedColumns = getIndexedColumnsOnTable(db, it)

            indexedColumns.size shouldBe 2
            indexedColumns shouldContain "shard_id"
            indexedColumns shouldContain "type"
        }

        "displayed_iam".let {
            val indexedColumns = getIndexedColumnsOnTable(db, it)

            indexedColumns.size shouldBe 0
        }

        "button_clicked".let {
            val indexedColumns = getIndexedColumnsOnTable(db, it)

            indexedColumns.size shouldBe 0
        }

        "hardware".let {
            val indexedColumns = getIndexedColumnsOnTable(db, it)

            indexedColumns.size shouldBe 0
        }
    }

    @Test
    fun testOnCreate_withLatestVersion() {
        initializeDatabaseWithVersion(LATEST_DB_VERSION)

        val expectedRequestColumns = getTableColumns(db, "request")
        val expectedShardColumns = getTableColumns(db, "shard")
        val expectedDisplayedIamColumns = getTableColumns(db, "displayed_iam")
        val expectedButtonClickedColumns = getTableColumns(db, "button_clicked")

        DatabaseTestUtils.dropAllTables(db)
        dbHelper.onCreate(db)

        val actualRequestColumns = getTableColumns(db, "request")
        val actualShardColumns = getTableColumns(db, "shard")
        val actualDisplayedIamColumns = getTableColumns(db, "displayed_iam")
        val actualButtonClickedColumns = getTableColumns(db, "button_clicked")

        actualRequestColumns shouldBe expectedRequestColumns
        actualShardColumns shouldBe expectedShardColumns
        actualDisplayedIamColumns shouldBe expectedDisplayedIamColumns
        actualButtonClickedColumns shouldBe expectedButtonClickedColumns
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

