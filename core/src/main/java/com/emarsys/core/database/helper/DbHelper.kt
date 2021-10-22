package com.emarsys.core.database.helper

import com.emarsys.core.database.CoreSQLiteDatabase

interface DbHelper {
    val readableCoreDatabase: CoreSQLiteDatabase
    val writableCoreDatabase: CoreSQLiteDatabase
}