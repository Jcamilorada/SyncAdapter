package com.syncadapter.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.syncadapter.R;
import com.syncadapter.provider.NewsConstants;
import com.syncadapter.security.SecurityConstants;
import com.syncadapter.sync.SyncUtils;
import com.syncadapter.security.SyncAccountService;

import java.text.DateFormat;
import java.util.GregorianCalendar;

public class EntryListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String SORT_ORDER = NewsConstants.NewsItemConstants.COLUMN_NAME_PUBLISHED + " desc";

    /* User name and password, change for ui controls that retrieves information. */
    private static final String USER_NAME = "USER_NAME";
    private static final String PASSWORD = "PASSWORD";

    private SimpleCursorAdapter mAdapter;
    private Object mSyncObserverHandle;
    private Menu mOptionsMenu;

    private static final String[] SELECTION_QUERY_STATEMENT = new String[]
        {
            NewsConstants.NewsItemConstants._ID,
            NewsConstants.NewsItemConstants.COLUMN_NAME_TITLE,
            NewsConstants.NewsItemConstants.COLUMN_NAME_LINK,
            NewsConstants.NewsItemConstants.COLUMN_NAME_PUBLISHED
        };

    private static final String[] FROM_COLUMNS = new String[]
        {
            NewsConstants.NewsItemConstants.COLUMN_NAME_TITLE,
            NewsConstants.NewsItemConstants.COLUMN_NAME_PUBLISHED
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
        SyncUtils.CreateSyncAccount(activity, USER_NAME);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Account account = SyncAccountService.GetAccount(USER_NAME, SecurityConstants.ACCOUNT_TYPE);
        AccountManager.get(getActivity()).setPassword(account, PASSWORD);

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
                if (columnIndex == NewsConstants.NewsItemConstants.PUBLISHED_COLUMN_INDEX)
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
                NewsConstants.NewsItemConstants.CONTENT_URI,
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
                SyncUtils.syncNow(USER_NAME);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        Cursor c = (Cursor) mAdapter.getItem(position);
        String articleUrlString = c.getString(NewsConstants.NewsItemConstants.LINK_COLUMN_INDEX);

        if (articleUrlString == null)
        {
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
                    Account account = SyncAccountService.GetAccount(USER_NAME, SecurityConstants.ACCOUNT_TYPE);

                    if (account == null)
                    {
                        setRefreshActionButtonState(false);
                        return;
                    }

                    boolean syncActive = ContentResolver.isSyncActive(account, NewsConstants.CONTENT_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(account, NewsConstants.CONTENT_AUTHORITY);
                    setRefreshActionButtonState(syncActive || syncPending);
                }
            });
        }
    };

}