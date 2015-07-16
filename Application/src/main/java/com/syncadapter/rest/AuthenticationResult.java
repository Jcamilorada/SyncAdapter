package com.syncadapter.rest;

/**
 *
 * Authentication response payload.
 *
 */
public class AuthenticationResult
{
    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    private String token;
}
