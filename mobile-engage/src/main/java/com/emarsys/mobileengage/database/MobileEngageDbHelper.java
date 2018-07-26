package com.emarsys.mobileengage.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.emarsys.core.database.helper.AbstractDbHelper;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedContract;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamContract;


public class MobileEngageDbHelper extends AbstractDbHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "EmarsysMobileEngage.db";

    public MobileEngageDbHelper(Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DisplayedIamContract.SQL_CREATE_TABLE);
        db.execSQL(ButtonClickedContract.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
