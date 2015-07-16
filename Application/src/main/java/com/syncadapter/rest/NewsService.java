package com.syncadapter.rest;


import retrofit.http.GET;

import java.util.List;

public interface NewsService
{
    @GET("/knews")
    List<NewsItem> getNews();
}