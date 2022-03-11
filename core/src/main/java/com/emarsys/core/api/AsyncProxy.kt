package com.emarsys.core.api

import com.emarsys.core.handler.ConcurrentHandlerHolder
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy


inline fun <reified T : Any> T.proxyWithHandler(
    handlerHolder: ConcurrentHandlerHolder,
): T {
    return Proxy.newProxyInstance(
        javaClass.classLoader,
        javaClass.interfaces,
        AsyncProxy(this, handlerHolder)
    ) as T
}

class AsyncProxy<T>(
    private val apiObject: T,
    private val handlerHolder: ConcurrentHandlerHolder,
) : InvocationHandler {

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        EmarsysIdlingResources.increment()

        val isVoid = method.returnType == Void.TYPE

        val result =
            if (isOnCoreSdkThread()) {
                invokeMethod(method, args)
            } else {
                runBlocking(handlerHolder.sdkScope.coroutineContext) {
                    schedule(isVoid) {
                        invokeMethod(method, args)
                    }
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

    private suspend fun schedule(isVoid: Boolean, callable: () -> Any?) =
        coroutineScope {
            val future = async {
                callable()
            }
            if (!isVoid) {
                future.await()
            } else {
                Unit
            }
        }
}