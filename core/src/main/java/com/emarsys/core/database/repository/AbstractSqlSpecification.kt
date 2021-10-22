package com.emarsys.core.database.repository

abstract class AbstractSqlSpecification : SqlSpecification {
    override val isDistinct: Boolean
        get() = false
    override val columns: Array<String>?
        get() = null
    override val selection: String?
        get() = null
    override val selectionArgs: Array<String>?
        get() = null
    override val groupBy: String?
        get() = null
    override val having: String?
        get() = null
    override val orderBy: String?
        get() = null
    override val limit: String?
        get() = null
}