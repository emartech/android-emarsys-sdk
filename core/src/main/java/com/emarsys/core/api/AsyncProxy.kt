package com.emarsys.core.api

import com.emarsys.core.handler.ConcurrentHandlerHolder
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


inline fun <reified T : Any> T.proxyWithHandler(
    handlerHolder: ConcurrentHandlerHolder,
    timeout: Long = 10
): T {
    return Proxy.newProxyInstance(
        javaClass.classLoader,
        javaClass.interfaces,
        AsyncProxy(this, handlerHolder, timeout)
    ) as T
}

class AsyncProxy<T>(
    private val apiObject: T,
    private val handlerHolder: ConcurrentHandlerHolder,
    private val timeout: Long
) : InvocationHandler {

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        EmarsysIdlingResources.increment()
        var result: Any? = null
        val isVoid = method.returnType == Void.TYPE
        if (method.returnType.isPrimitive) {
            result = when (method.returnType) {
                Boolean::class.java -> false
                Char::class.java -> Char(0)
                else -> 0
            }
        }
        val latch = CountDownLatch(1)
        handlerHolder.coreHandler.post {
            result = if (args != null) {
                method.invoke(apiObject, *args)
            } else {
                method.invoke(apiObject)
            }
            if (!isVoid) {
                latch.countDown()
            }
            EmarsysIdlingResources.decrement()
        }
        if (!isVoid) {
            latch.await(timeout, TimeUnit.SECONDS)
        }
        return result
    }
}