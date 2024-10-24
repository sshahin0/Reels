package com.sunfun.slideshow.pojo;

public class NewSubscription
{
    private String boxTitle;

    private Subtitle subtitle;

    private Title title;

    public String getBoxTitle ()
    {
        return boxTitle;
    }

    public void setBoxTitle (String boxTitle)
    {
        this.boxTitle = boxTitle;
    }

    public Subtitle getSubtitle ()
    {
        return subtitle;
    }

    public void setSubtitle (Subtitle subtitle)
    {
        this.subtitle = subtitle;
    }

    public Title getTitle ()
    {
        return title;
    }

    public void setTitle (Title title)
    {
        this.title = title;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [boxTitle = "+boxTitle+", subtitle = "+subtitle+", title = "+title+"]";
    }
}

