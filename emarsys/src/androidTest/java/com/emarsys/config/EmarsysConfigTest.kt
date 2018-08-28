package com.emarsys.config

import org.junit.Test

class EmarsysConfigTest {
    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_mobileEngageInternal_shouldNotBeNull() {
        EmarsysConfig(null)
    }
}