package com.emarsys.core.api

import com.emarsys.core.util.log.Logger.Companion.error
import com.emarsys.core.util.log.entry.CrashLog
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy


inline fun <reified T : Any> T.proxyWithLogExceptions(): T {
    return Proxy.newProxyInstance(
        javaClass.classLoader,
        javaClass.interfaces,
        LogExceptionProxy(this)
    ) as T
}

class LogExceptionProxy<T>(private val apiObject: T) : InvocationHandler {

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        var result: Any? = null
        if (method.returnType.isPrimitive) {
            result = when (method.returnType) {
                Boolean::class.java -> false
                Char::class.java -> Char(0)
                else -> 0
            }
        }
        try {
            result = if (args != null) {
                method.invoke(apiObject, *args)
            } else {
                method.invoke(apiObject)
            }
        } catch (exception: Exception) {
            if (exception is InvocationTargetException && exception.cause != null) {
                if (exception.cause!!.cause != null) {
                    error(CrashLog(exception.cause!!.cause!!))
                } else {
                    error(CrashLog(exception.cause!!))
                }
            } else {
                error(CrashLog(exception))
            }
        }
        return result
    }
}
