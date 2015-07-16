package com.example.basicsyncadapter.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;

import com.example.basicsyncadapter.provider.NewsContract;

import com.example.basicsyncadapter.rest.NewsItem;
import com.example.basicsyncadapter.rest.NewsService;
import com.example.basicsyncadapter.security.GenericAccountService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class SyncAdapter extends AbstractThreadedSyncAdapter
{
    public static final String REST_RESOURCE_URL = "https://demo5496544.mockable.io/";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private final Context context;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        this.context = context;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs)
    {
        super(context, autoInitialize, allowParallelSyncs);
        this.context = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult)
    {
        try
        {
            String authtoken = null;
            try
            {
                authtoken = AccountManager.get(context).blockingGetAuthToken(account, GenericAccountService.AUTHTOKEN_TYPE, true);
            }
            catch (OperationCanceledException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (AuthenticatorException e)
            {
                e.printStackTrace();
            }

            updateLocalInformation(syncResult, authtoken);
        }

        catch (RemoteException e)
        {
            syncResult.databaseError = true;
            return;
        }

        catch (OperationApplicationException e)
        {
            syncResult.databaseError = true;
            return;
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public void updateLocalInformation(
        final SyncResult syncResult, final String authtoken) throws RemoteException, OperationApplicationException
    {
        Gson gson = new GsonBuilder()
            .setDateFormat(DATE_FORMAT)
            .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint(REST_RESOURCE_URL)
            .setConverter(new GsonConverter(gson))
            .build();

        NewsService service = restAdapter.create(NewsService.class);
        List<NewsItem> news = service.getNews();

        ArrayList<ContentProviderOperation> batch = new ArrayList<>(news.size());
        for (NewsItem bean : news)
        {
            batch.add(ContentProviderOperation.newInsert(NewsContract.NewsItemConstants.CONTENT_URI)
                .withValue(NewsContract.NewsItemConstants.COLUMN_NAME_ENTRY_ID, bean.getId())
                .withValue(NewsContract.NewsItemConstants.COLUMN_NAME_TITLE, bean.getTitle())
                .withValue(NewsContract.NewsItemConstants.COLUMN_NAME_LINK, bean.getLink())
                .withValue(NewsContract.NewsItemConstants.COLUMN_NAME_PUBLISHED, bean.getPublished())
                .build());

            syncResult.stats.numInserts++;
        }

        //syncResult.delayUntil = 25000;
        //syncResult.delayUntil = (long) ((System.currentTimeMillis() / 1000) + (60 * 1));

        context.getContentResolver().applyBatch(NewsContract.CONTENT_AUTHORITY, batch);
        context.getContentResolver().notifyChange(NewsContract.NewsItemConstants.CONTENT_URI, null, false);
    }
}
