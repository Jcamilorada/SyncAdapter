package com.example.android.basicsyncadapter.rest;

public class NewsItemBean
{
    private String id;
    private String title;
    private String link;
    private long published;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getLink()
    {
        return link;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    public long getPublished()
    {
        return published;
    }

    public void setPublished(long published)
    {
        this.published = published;
    }
}
