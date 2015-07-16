package com.syncadapter.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;


import com.syncadapter.provider.NewsConstants;
import com.syncadapter.security.SecurityConstants;
import com.syncadapter.security.SyncAccountService;

public class SyncUtils {
    private static final long SYNC_FREQUENCY_SECONDS = 1;
    private static final String CONTENT_AUTHORITY = NewsConstants.CONTENT_AUTHORITY;
    private static final String PREF_SETUP_COMPLETE = "setup_complete";

    public static void CreateSyncAccount(Context context, String accountName)
    {
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

        Account account = SyncAccountService.GetAccount(accountName, SecurityConstants.ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        if (accountManager.addAccountExplicitly(account, null, null))
        {
            setSyncPeriod(account);
            newAccount = true;
        }

        if (newAccount || !setupComplete)
        {
            syncNow(accountName);
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    private static void setSyncPeriod(Account account)
    {
        ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
        ContentResolver.addPeriodicSync(account, CONTENT_AUTHORITY, Bundle.EMPTY, SYNC_FREQUENCY_SECONDS);
    }

    public static void syncNow(String accountName)
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(SyncAccountService.GetAccount(accountName, SecurityConstants.ACCOUNT_TYPE),
            NewsConstants.CONTENT_AUTHORITY,
            bundle);
    }
}
