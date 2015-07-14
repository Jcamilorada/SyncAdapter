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

package com.example.android.basicsyncadapter.ui;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.basicsyncadapter.R;
import com.example.android.basicsyncadapter.provider.NewsContract;
import com.example.android.basicsyncadapter.sync.SyncUtils;
import com.example.android.common.accounts.GenericAccountService;

import java.text.DateFormat;
import java.util.GregorianCalendar;

public class EntryListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>
{

    private static final String TAG = "EntryListFragment";
    public static final String SORT_ORDER = NewsContract.NewsItemConstants.COLUMN_NAME_PUBLISHED + " desc";

    private SimpleCursorAdapter mAdapter;
    private Object mSyncObserverHandle;
    private Menu mOptionsMenu;

    private static final String[] SELECTION_QUERY_STATEMENT = new String[]
        {
            NewsContract.NewsItemConstants._ID,
            NewsContract.NewsItemConstants.COLUMN_NAME_TITLE,
            NewsContract.NewsItemConstants.COLUMN_NAME_LINK,
            NewsContract.NewsItemConstants.COLUMN_NAME_PUBLISHED
        };

    private static final String[] FROM_COLUMNS = new String[]
        {
            NewsContract.NewsItemConstants.COLUMN_NAME_TITLE,
            NewsContract.NewsItemConstants.COLUMN_NAME_PUBLISHED
        };


    private static final int[] TO_FIELDS = new int[]
        {
            android.R.id.text1,
            android.R.id.text2
        };

    public EntryListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        SyncUtils.CreateSyncAccount(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new SimpleCursorAdapter(
                getActivity(),
                android.R.layout.simple_list_item_activated_2,
                null,
                FROM_COLUMNS,
                TO_FIELDS,
                0);

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == NewsContract.NewsItemConstants.PUBLISHED_COLUMN_INDEX)
                {
                    GregorianCalendar calendar = new GregorianCalendar();
                    calendar.setTimeInMillis(cursor.getLong(columnIndex));

                    String date = DateFormat.getDateInstance(DateFormat.LONG).format(calendar.getTime());
                    ((TextView) view).setText(date);
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });

        setListAdapter(mAdapter);
        setEmptyText(getText(R.string.loading));
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSyncStatusObserver.onStatusChanged(0);

        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                NewsContract.NewsItemConstants.CONTENT_URI,
                SELECTION_QUERY_STATEMENT,
                null,
                null,
                SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        mAdapter.changeCursor(null);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mOptionsMenu = menu;
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_refresh:
                SyncUtils.TriggerRefresh();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        Cursor c = (Cursor) mAdapter.getItem(position);
        String articleUrlString = c.getString(NewsContract.NewsItemConstants.LINK_COLUMN_INDEX);

        if (articleUrlString == null) {
            Log.e(TAG, "Attempt to launch entry with null link");
            return;
        }

        Uri articleURL = Uri.parse(articleUrlString);
        Intent intent = new Intent(Intent.ACTION_VIEW, articleURL);
        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setRefreshActionButtonState(boolean refreshing) {
        if (mOptionsMenu == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return;
        }

        final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
        if (refreshItem != null)
        {
            if (refreshing)
            {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            }
            else
            {
                refreshItem.setActionView(null);
            }
        }
    }

    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver()
    {
        @Override
        public void onStatusChanged(int which) {
            getActivity().runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() {
                    Account account = GenericAccountService.GetAccount(SyncUtils.ACCOUNT_TYPE);
                    if (account == null)
                    {
                        setRefreshActionButtonState(false);
                        return;
                    }

                    boolean syncActive = ContentResolver.isSyncActive(account, NewsContract.CONTENT_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(account, NewsContract.CONTENT_AUTHORITY);
                    setRefreshActionButtonState(syncActive || syncPending);
                }
            });
        }
    };

}