package com.emarsys.core.database.repository.specification

import com.emarsys.core.request.model.specification.QueryNewestRequestModel
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class QueryNewestRequestModelTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.timeoutRule

    private lateinit var specification: QueryNewestRequestModel

    @Before
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()
        specification = QueryNewestRequestModel()
    }

    @Test
    fun testGetSql() {
        val expected = "SELECT * FROM request ORDER BY ROWID ASC LIMIT 1;"
        val result = specification.sql

        result shouldBe expected
    }

    @Test
    fun testGetArgs() {
        specification.args shouldBe null
    }
}