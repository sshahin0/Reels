package com.sunfun.slideshow.pojo;

import java.util.ArrayList;

public class Bottom_cat
{
    private String nug;

    private ArrayList<Contents> contents;

    public String getNug ()
    {
        return nug;
    }

    public void setNug (String nug)
    {
        this.nug = nug;
    }

    public ArrayList<Contents> getContents ()
    {
        return contents;
    }

    public void setContents (ArrayList<Contents> contents)
    {
        this.contents = contents;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [nug = "+nug+", contents = "+contents+"]";
    }
}
