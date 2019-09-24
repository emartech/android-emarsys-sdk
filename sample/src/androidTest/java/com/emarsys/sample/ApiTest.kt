package com.emarsys.sample

import com.emarsys.sample.testutils.TimeoutUtils

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class ApiTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun test() {
    }

}