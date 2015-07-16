package com.example.basicsyncadapter.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class NewsDatabase extends SQLiteOpenHelper
{

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "news.db";

    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INTEGER = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + NewsContract.NewsItemConstants.TABLE_NAME + " (" +
                    NewsContract.NewsItemConstants._ID + " INTEGER PRIMARY KEY," +
                    NewsContract.NewsItemConstants.COLUMN_NAME_ENTRY_ID + TYPE_TEXT + COMMA_SEP +
                    NewsContract.NewsItemConstants.COLUMN_NAME_TITLE    + TYPE_TEXT + COMMA_SEP +
                    NewsContract.NewsItemConstants.COLUMN_NAME_LINK + TYPE_TEXT + COMMA_SEP +
                    NewsContract.NewsItemConstants.COLUMN_NAME_PUBLISHED + TYPE_INTEGER + ")";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + NewsContract.NewsItemConstants.TABLE_NAME;

    public NewsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
