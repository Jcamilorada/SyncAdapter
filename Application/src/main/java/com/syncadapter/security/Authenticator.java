package com.syncadapter.security;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import com.syncadapter.rest.AuthenticationRequest;
import com.syncadapter.rest.AuthenticationResult;
import com.syncadapter.rest.RestConstants;
import com.syncadapter.rest.SecurityService;
import com.syncadapter.sync.SyncUtils;
import retrofit.RestAdapter;

public class Authenticator extends AbstractAccountAuthenticator
{
    private static final String INVALID_AUTH_TOKEN_TYPE_MESSAGE = "invalid auth token type";

    private final Context context;

    public Authenticator(Context context)
    {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle editProperties(
        AccountAuthenticatorResponse accountAuthenticatorResponse, String s)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount(
        AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException
    {
        return null;
    }

    @Override
    public Bundle confirmCredentials(
        AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException
    {
        return null;
    }

    @Override
    public Bundle getAuthToken(
        AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException
    {
        final Bundle result = new Bundle();

        if (!authTokenType.equals(SecurityConstants.AUTHTOKEN_TYPE))
        {
            result.putString(AccountManager.KEY_ERROR_MESSAGE, INVALID_AUTH_TOKEN_TYPE_MESSAGE);
            return result;
        }

        else
        {
            String password = AccountManager.get(context).getPassword(account);

            RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(RestConstants.REST_RESOURCE_URL)
                .build();

            SecurityService service = restAdapter.create(SecurityService.class);
            AuthenticationRequest request = new AuthenticationRequest(account.name, password);
            AuthenticationResult authenticationResult =  service.getAuthToken(request);

            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, SecurityConstants.ACCOUNT_TYPE);
            result.putString(AccountManager.KEY_AUTHTOKEN, authenticationResult.getToken());
        }

        return result;
    }

    @Override
    public String getAuthTokenLabel(String s)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle updateCredentials(
        AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle hasFeatures(
        AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException
    {
        throw new UnsupportedOperationException();
    }
}
