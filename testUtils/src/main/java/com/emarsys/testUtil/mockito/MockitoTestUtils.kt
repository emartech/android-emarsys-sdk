package com.emarsys.testUtil.mockito

import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.stubbing.OngoingStubbing

fun <T> whenever(mock: T): OngoingStubbing<T> = `when`(mock)

fun <T> anyNotNull(): T {
    Mockito.any<T>()
    return uninitialized()
}

@Suppress("UNCHECKED_CAST")
private fun <T> uninitialized(): T {
    return null as T
}

