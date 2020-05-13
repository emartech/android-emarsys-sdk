package com.emarsys.core.api

import android.os.Handler
import android.util.Log
import com.emarsys.core.Mockable
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


inline fun <reified T : Any> T.proxyWithHandler(handler: Handler): T {
    return Proxy.newProxyInstance(javaClass.classLoader,
            javaClass.interfaces,
            AsyncProxy(this, handler)) as T
}

class AsyncProxy<T>(private val apiObject: T,
                    private val handler: Handler) : InvocationHandler {

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        var result: Any? = null
        val isVoid = method.returnType == Void.TYPE
        val latch = CountDownLatch(1)
        handler.post {
            result = if (args != null) {
                method.invoke(apiObject, *args)
            } else {
                method.invoke(apiObject)
            }
            if (!isVoid) {
                latch.countDown()
            }
        }
        if (!isVoid) {
            latch.await(10, TimeUnit.SECONDS)
        }
        return result
    }
}