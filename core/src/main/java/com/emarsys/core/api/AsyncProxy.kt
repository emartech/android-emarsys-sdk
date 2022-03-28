package com.emarsys.core.api

import com.emarsys.core.handler.ConcurrentHandlerHolder
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


inline fun <reified T : Any> T.proxyWithHandler(
    handlerHolder: ConcurrentHandlerHolder,
    timeout: Long = 5,
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
        if (isOnCoreSdkThread()) {
            result = invokeMethod(method, args)
        } else {
            val latch = CountDownLatch(1)
            handlerHolder.post {
                result = invokeMethod(method, args)
                if (!isVoid) {
                    latch.countDown()
                }
            }
            if (!isVoid) {
                latch.await(timeout, TimeUnit.SECONDS)
            }
        }

        EmarsysIdlingResources.decrement()
        return result

    }

    private fun isOnCoreSdkThread() =
        Thread.currentThread() == handlerHolder.coreHandler.handler.looper.thread

    private fun invokeMethod(method: Method, args: Array<out Any>?): Any? {
        return if (args != null) {
            method.invoke(apiObject, *args)
        } else {
            method.invoke(apiObject)
        }
    }

}