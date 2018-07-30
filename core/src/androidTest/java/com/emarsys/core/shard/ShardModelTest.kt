package com.emarsys.core.shard;

import com.emarsys.core.testUtil.TimeoutUtils
import org.junit.Rule
import org.junit.Test

class ShardModelTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.getTimeoutRule()

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_typeMustNotBeNull() {
        ShardModel(null, mapOf(), 0, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_dataMustNotBeNull() {
        ShardModel("type", null, 0, 0)
    }
}