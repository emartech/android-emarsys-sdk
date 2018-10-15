package com.emarsys.testUtil.mockito

import org.mockito.Mockito.`when`
import org.mockito.stubbing.OngoingStubbing

object MockitoTestUtils {

    @JvmStatic
    fun <T> whenever(mock: T): OngoingStubbing<T> = `when`(mock)

}
