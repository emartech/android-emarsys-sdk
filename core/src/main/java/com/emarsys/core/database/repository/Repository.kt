package com.emarsys.core.database.repository

interface Repository<T, S> {
    fun add(item: T)
    fun update(item: T, specification: SqlSpecification): Int
    fun remove(specification: S)
    fun query(specification: S): List<T>
    fun isEmpty(): Boolean
}