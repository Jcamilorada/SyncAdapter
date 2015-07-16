package com.syncadapter.rest;

import retrofit.http.Body;
import retrofit.http.POST;

public interface SecurityService
{
    @POST("/authtoken")
    AuthenticationResult getAuthToken(@Body AuthenticationRequest request);

}
