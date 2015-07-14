package com.example.android.basicsyncadapter.sync;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.example.android.basicsyncadapter.provider.NewsContract;

import com.example.android.basicsyncadapter.rest.NewsItemBean;
import com.example.android.basicsyncadapter.rest.NewsService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

import java.util.ArrayList;
import java.util.List;

class SyncAdapter extends AbstractThreadedSyncAdapter
{
    public static final String TAG = "SyncAdapter";
    public static final String REST_RESOURCE_URL = "https://demo5496544.mockable.io/";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private final ContentResolver mContentResolver;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs)
    {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult)
    {
        Log.i(TAG, "Beginning network synchronization");
        try
        {
            updateLocalInformation(syncResult);
        }

        catch (RemoteException e)
        {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        }

        catch (OperationApplicationException e)
        {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        }
        Log.i(TAG, "Network synchronization complete");
    }

    public void updateLocalInformation(final SyncResult syncResult) throws RemoteException, OperationApplicationException
    {
        Gson gson = new GsonBuilder()
            .setDateFormat(DATE_FORMAT)
            .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint(REST_RESOURCE_URL)
            .setConverter(new GsonConverter(gson))
            .build();

        NewsService service = restAdapter.create(NewsService.class);
        List<NewsItemBean> news = service.getNews();

        ArrayList<ContentProviderOperation> batch = new ArrayList<>(news.size());
        for (NewsItemBean bean : news)
        {
            batch.add(ContentProviderOperation.newInsert(NewsContract.NewsItemConstants.CONTENT_URI)
                .withValue(NewsContract.NewsItemConstants.COLUMN_NAME_ENTRY_ID, bean.getId())
                .withValue(NewsContract.NewsItemConstants.COLUMN_NAME_TITLE, bean.getTitle())
                .withValue(NewsContract.NewsItemConstants.COLUMN_NAME_LINK, bean.getLink())
                .withValue(NewsContract.NewsItemConstants.COLUMN_NAME_PUBLISHED, bean.getPublished())
                .build());

            syncResult.stats.numInserts++;
        }

        mContentResolver.applyBatch(NewsContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(NewsContract.NewsItemConstants.CONTENT_URI, null, false);
    }
}
