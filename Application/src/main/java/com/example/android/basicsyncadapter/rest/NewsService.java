package com.example.android.basicsyncadapter.rest;


import retrofit.http.GET;

import java.util.List;

public interface NewsService
{
    @GET("/knews")
    List<NewsItemBean> getNews();
}