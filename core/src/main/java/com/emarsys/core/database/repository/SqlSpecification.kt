package com.emarsys.core.database.repository

interface SqlSpecification {
    val isDistinct: Boolean
    val columns: Array<String>?
    val selection: String?
    val selectionArgs: Array<String>?
    val groupBy: String?
    val having: String?
    val orderBy: String?
    val limit: String?
}