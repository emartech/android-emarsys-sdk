package com.emarsys.core.di


object Container {
    @JvmStatic
    val dependencies: MutableMap<String, Any?> = mutableMapOf()

    inline fun <reified T> getDependency(key: String = ""): T {
        return dependencies[T::class.java.name + key] as T
    }

    inline fun <reified T> addDependency(dependency: T, key: String = "") {
        dependencies[T::class.java.name + key] = dependency
    }
}