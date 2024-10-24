package com.sunfun.slideshow.pojo;

import java.util.ArrayList;

public class Top_cat
{
    private ArrayList<Contents> contents;

    private String title;

    public ArrayList<Contents> getContents ()
    {
        return contents;
    }

    public void setContents (ArrayList<Contents> contents)
    {
        this.contents = contents;
    }

    public String getTitle ()
    {
        return title;
    }

    public void setTitle (String title)
    {
        this.title = title;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [contents = "+contents+", title = "+title+"]";
    }
}