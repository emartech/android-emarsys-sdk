package com.emarsys.test.util

import java.util.*

object RandomTestUtils {

    @JvmStatic
    fun randomBool(): Boolean {
        return Random().nextBoolean()
    }

    @JvmStatic
    fun randomInt(): Int {
        return Random().nextInt()
    }

    @JvmStatic
    fun randomLong(): Long {
        return Random().nextLong()
    }

    @JvmStatic
    fun randomString(): String {
        return UUID.randomUUID().toString()
    }

    @JvmStatic
    fun randomNumberString(): String {
        return java.lang.Long.toString(randomLong())
    }

    @JvmStatic
    fun randomMap(): Any {
        val map = HashMap<String, Any>()
        map[randomString()] = randomInt()
        map[randomString()] = randomBool()
        map[randomString()] = randomString()
        return map
    }
}
