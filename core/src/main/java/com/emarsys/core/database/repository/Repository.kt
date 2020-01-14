package com.emarsys.core.database.repository

interface Repository<T, S> {
    fun add(item: T)
    fun remove(specification: S)
    fun query(specification: S): List<T>
    fun isEmpty(): Boolean
}