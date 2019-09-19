package com.spareio.spynsdk;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SpynDataHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "spyn.db";
    private static final int DATABASE_VERSION = 1;

    public SpynDataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_TODO_TABLE =

                "CREATE TABLE " + spynSDK.SpynPartnerEntry.TABLE_NAME + " (" +
                        spynSDK.SpynPartnerEntry._ID            + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        spynSDK.SpynPartnerEntry.COLUMN_WORKER  + " TEXT NOT NULL);";
        db.execSQL(SQL_CREATE_TODO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + spynSDK.SpynPartnerEntry.TABLE_NAME);
        onCreate(db);
    }
}

