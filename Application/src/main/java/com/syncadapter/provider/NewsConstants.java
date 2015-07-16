package com.syncadapter.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class NewsConstants
{
    /* Class cannot be instantiated. */
    private NewsConstants()
    {
    }

    public static final String CONTENT_AUTHORITY = "com.syncadapter";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String PATH_ENTRIES = "entries";


    public static class NewsItemConstants implements BaseColumns
    {

        /* Data types that content provider handle entries/entry */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";


        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ENTRIES).build();

        public static final String TABLE_NAME = "entry";

        /* Columns names */
        public static final String COLUMN_NAME_ENTRY_ID = "entry_id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_LINK = "link";
        public static final String COLUMN_NAME_PUBLISHED = "published";

        /* Columns index */
        public static final int ID_COLUMN_INDEX = 0;
        public static final int NAME_COLUMN_INDEX = 1;
        public static final int LINK_COLUMN_INDEX = 2;
        public static final int PUBLISHED_COLUMN_INDEX = 3;
    }
}