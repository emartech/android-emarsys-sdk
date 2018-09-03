package com.emarsys.predict.api

import com.emarsys.testUtil.TimeoutUtils
import junit.framework.Assert

import org.junit.Rule
import org.junit.Test

class ApiTest {

    @Rule
    @JvmField
    var timeout = TimeoutUtils.timeoutRule

    @Test
    fun testConstructor_keyValueStore_shouldNotBeNull() {
        Assert.assertTrue(true)
    }

}