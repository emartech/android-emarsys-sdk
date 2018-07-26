package com.emarsys.core.shard;

import org.junit.Test

class ShardModelTest {

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_typeMustNotBeNull() {
        ShardModel(null, mapOf(), 0, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_dataMustNotBeNull() {
        ShardModel("type", null, 0, 0)
    }
}