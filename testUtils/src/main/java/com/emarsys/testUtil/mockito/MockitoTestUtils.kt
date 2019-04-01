package com.emarsys.testUtil.mockito

import org.mockito.Mockito.`when`
import org.mockito.stubbing.OngoingStubbing

fun <T> whenever(mock: T): OngoingStubbing<T> = `when`(mock)

