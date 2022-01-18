package com.emarsys.core.database.repository

interface Repository<T, S> {
    suspend fun add(item: T)
    suspend fun update(item: T, specification: SqlSpecification): Int
    suspend fun remove(specification: S)
    fun query(specification: S): List<T>
    fun isEmpty(): Boolean
}