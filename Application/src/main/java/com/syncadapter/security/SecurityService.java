package com.syncadapter.security;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SecurityService extends Service
{
    private Authenticator authenticator;

    public static Account GetAccount(String accountName, String accountType)
    {
        return new Account(accountName, accountType);
    }

    @Override
    public void onCreate()
    {
        authenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }

}

