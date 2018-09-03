package com.emarsys.core.shard;

import com.emarsys.testUtil.TimeoutUtils
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldEqualTo
import org.junit.Rule
import org.junit.Test

class ShardModelTest {

    companion object {
        const val ID = "shard_id"
        const val TYPE = "type"
    }

    @Rule
    @JvmField
    val timeout = TimeoutUtils.timeoutRule

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_idMustNotBeNull() {
        ShardModel(null, TYPE, mapOf(), 0, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_typeMustNotBeNull() {
        ShardModel(ID, null, mapOf(), 0, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_dataMustNotBeNull() {
        ShardModel(ID, TYPE, null, 0, 0)
    }

    @Test
    fun testConstructor_fieldsMustBeInitialised() {
        val shard = ShardModel(ID, TYPE, mapOf(), 0, 0)
        shard.id shouldBeEqualTo ID
        shard.type shouldBeEqualTo TYPE
        shard.data shouldEqual mapOf()
        shard.timestamp shouldEqualTo 0
        shard.ttl shouldEqualTo 0
    }
}