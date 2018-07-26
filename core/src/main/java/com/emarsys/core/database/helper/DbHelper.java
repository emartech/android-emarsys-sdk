package com.emarsys.core.database.helper;

import com.emarsys.core.database.CoreSQLiteDatabase;

public interface DbHelper {

    CoreSQLiteDatabase getReadableCoreDatabase();

    CoreSQLiteDatabase getWritableCoreDatabase();

}
