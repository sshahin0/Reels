package com.sunfun.slideshow.pojo;

public class Songfeatures
{
    private Bottom_cat bottom_cat;

    private Top_cat top_cat;

    public Bottom_cat getBottom_cat ()
    {
        return bottom_cat;
    }

    public void setBottom_cat (Bottom_cat bottom_cat)
    {
        this.bottom_cat = bottom_cat;
    }

    public Top_cat getTop_cat ()
    {
        return top_cat;
    }

    public void setTop_cat (Top_cat top_cat)
    {
        this.top_cat = top_cat;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [bottom_cat = "+bottom_cat+", top_cat = "+top_cat+"]";
    }
}