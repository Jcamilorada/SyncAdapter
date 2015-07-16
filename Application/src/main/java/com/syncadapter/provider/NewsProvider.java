/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.syncadapter.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.syncadapter.provider.db.SelectionBuilder;

public class NewsProvider extends ContentProvider {
    private NewsDatabase mDatabaseHelper;

    private static final String AUTHORITY = NewsConstants.CONTENT_AUTHORITY;

    /* Routes  IDs*/
    public static final int MULTIPLES_RECORD_ID = 1;
    public static final int SINGLE_RECORDS_ID = 2;


    /* decode incoming URIs */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, "entries", MULTIPLES_RECORD_ID);
        sUriMatcher.addURI(AUTHORITY, "entries/*", SINGLE_RECORDS_ID);
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new NewsDatabase(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MULTIPLES_RECORD_ID:
                return NewsConstants.NewsItemConstants.CONTENT_TYPE;
            case SINGLE_RECORDS_ID:
                return NewsConstants.NewsItemConstants.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException(uri.toString());
        }
    }

    @Override
    public Cursor query(
        Uri uri,
        String[] projection,
        String selection,
        String[] selectionArgs,
        String sortOrder)
    {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case SINGLE_RECORDS_ID:
                String id = uri.getLastPathSegment();
                builder.where(NewsConstants.NewsItemConstants._ID + "=?", id);
            case MULTIPLES_RECORD_ID:
                builder.table(NewsConstants.NewsItemConstants.TABLE_NAME).where(selection, selectionArgs);
                Cursor cursor = builder.query(db, projection, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            default:
                throw new UnsupportedOperationException(uri.toString());
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        switch (match) {
            case MULTIPLES_RECORD_ID:
                long id = db.insertOrThrow(NewsConstants.NewsItemConstants.TABLE_NAME, null, values);
                result = Uri.parse(NewsConstants.NewsItemConstants.CONTENT_URI + "/" + id);
                break;
            case SINGLE_RECORDS_ID:
                throw new UnsupportedOperationException(uri.toString());
            default:
                throw new UnsupportedOperationException(uri.toString());
        }

        notifyChange(uri);
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match)
        {
            case MULTIPLES_RECORD_ID:
                count = builder.table(NewsConstants.NewsItemConstants.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case SINGLE_RECORDS_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(NewsConstants.NewsItemConstants.TABLE_NAME)
                       .where(NewsConstants.NewsItemConstants._ID + "=?", id)
                       .where(selection, selectionArgs)
                       .delete(db);
                break;
            default:
                throw new UnsupportedOperationException(uri.toString());
        }

        notifyChange(uri);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case MULTIPLES_RECORD_ID:
                count = builder.table(NewsConstants.NewsItemConstants.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case SINGLE_RECORDS_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(NewsConstants.NewsItemConstants.TABLE_NAME)
                        .where(NewsConstants.NewsItemConstants._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        notifyChange(uri);
        return count;
    }

    // Send broadcast to registered ContentObservers, to refresh UI.
    private void notifyChange(Uri uri)
    {
        Context ctx = getContext();
        ctx.getContentResolver().notifyChange(uri, null, false);
    }
}
